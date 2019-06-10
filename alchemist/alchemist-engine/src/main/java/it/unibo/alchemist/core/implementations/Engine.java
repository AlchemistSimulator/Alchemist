/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations;

import com.google.common.collect.Sets;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.DependencyGraph;
import it.unibo.alchemist.core.interfaces.Scheduler;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import org.danilopianini.util.concurrent.FastReadWriteLock;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

/**
 * This class implements a simulation. It offers a wide number of static
 * factories to ease the creation process.
 *
 * @param <T>
 *            concentration type
 * @param <P>
 *            {@link Position} type
 */
public final class Engine<T, P extends Position<? extends P>> implements Simulation<T, P> {

    private static final Logger L = LoggerFactory.getLogger(Engine.class);
    private static final double NANOS_TO_SEC = 1000000000.0;
    private final Lock statusLock = new ReentrantLock();
    private final Condition statusCondition = statusLock.newCondition();
    private final BlockingQueue<CheckedRunnable> commands = new LinkedBlockingQueue<>();
    private final Queue<Update> afterExecutionUpdates = new ArrayDeque<>();
    private final Environment<T, P> env;
    private final DependencyGraph<T> dg;
    private final Scheduler<T> ipq;
    private final Time finalTime;
    private final FastReadWriteLock monitorLock = new FastReadWriteLock();
    private final List<OutputMonitor<T, P>> monitors = new LinkedList<>();
    private final long finalStep;
    private volatile Status status = Status.INIT;
    private Optional<Throwable> error = Optional.empty();
    private Time currentTime = DoubleTime.ZERO_TIME;
    private long currentStep;
    private Thread myThread;


    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link Scheduler} interfaces, don't use this constructor.
     *
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     */
    public Engine(final Environment<T, P> e, final long maxSteps) {
        this(e, maxSteps, new DoubleTime(Double.POSITIVE_INFINITY));
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link Scheduler} interfaces, don't use this constructor.
     *
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     * @param t
     *            the maximum time to reach
     */
    public Engine(final Environment<T, P> e, final long maxSteps, final Time t) {
        L.trace("Engine created");
        env = e;
        env.setSimulation(this);
        dg = new JGraphTDependencyGraph<>(env);
        ipq = new ArrayIndexedPriorityQueue<>();
        this.finalStep = maxSteps;
        this.finalTime = t;
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link Scheduler} interfaces, don't use this constructor.
     *
     * @param e
     *            the environment at the initial time
     * @param t
     *            the maximum time to reach
     */
    public Engine(final Environment<T, P> e, final Time t) {
        this(e, Long.MAX_VALUE, t);
    }

    @Override
    public void addOutputMonitor(final OutputMonitor<T, P> op) {
        monitorLock.write();
        monitors.add(op);
        monitorLock.release();
    }

    private void checkCaller() {
        if (Thread.currentThread() != myThread) {
            throw new IllegalMonitorStateException("This method must get called from the simulation thread.");
        }
    }

    private int compareStatuses(final Status o) {
        if ((status == Status.RUNNING || status == Status.PAUSED) && (o == Status.RUNNING || o == Status.PAUSED)) {
            return 0;
        } else {
            return status.compareTo(o);
        }
    }

    private void doStep() {
        final Reaction<T> mu = ipq.getNext();
        if (mu == null) {
            this.newStatus(Status.TERMINATED);
            L.info("No more reactions.");
        } else {
            final Time t = mu.getTau();
            if (t.compareTo(currentTime) < 0) {
                throw new IllegalStateException(mu + "\nis scheduled in the past at time " + t
                        + ", current time is " + currentTime
                        + ". Problem occurred at step " + currentStep);
            }
            currentTime = t;
            if (mu.canExecute()) {
                /*
                 * This must be taken before execution, because the reaction
                 * might remove itself (or its node) from the environment.
                 */
                mu.getConditions().forEach(it.unibo.alchemist.model.interfaces.Condition::reactionReady);
                mu.execute();
                Set<Reaction<T>> toUpdate = dg.outboundDependencies(mu);
                if (!afterExecutionUpdates.isEmpty()) {
                    afterExecutionUpdates.forEach(Update::performChanges);
                    afterExecutionUpdates.clear();
                    toUpdate = Sets.union(toUpdate, dg.outboundDependencies(mu));
                }
                toUpdate.forEach(this::updateReaction);
            }
            mu.update(currentTime, true, env);
            ipq.updateReaction(mu);
            monitorLock.read();
            for (final OutputMonitor<T, P> m : monitors) {
                m.stepDone(env, mu, currentTime, currentStep);
            }
            monitorLock.release();
        }
        if (env.isTerminated()) {
            newStatus(Status.TERMINATED);
            L.info("Termination condition reached.");
        }
        currentStep++;
    }

    private void finalizeConstructor() {
        for (final Node<T> n : env) {
            for (final Reaction<T> r : n.getReactions()) {
                scheduleReaction(r);
            }
        }
    }

    /**
     * @return the dependency graph
     */
    public DependencyGraph<T> getDependencyGraph() {
        return dg;
    }

    @Override
    public Environment<T, P> getEnvironment() {
        return env;
    }

    @Override
    public Optional<Throwable> getError() {
        return error;
    }

    @Override
    public long getFinalStep() {
        return finalStep;
    }

    @Override
    public Time getFinalTime() {
        return finalTime;
    }


    /**
     * @return The IPQ
     */
    public Scheduler<T> getReactionManager() {
        return ipq;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public long getStep() {
        return currentStep;
    }

    @Override
    public Time getTime() {
        return currentTime;
    }

    @Override
    public synchronized void goToStep(final long step) {
        runUntil(() -> getStep() < step);
    }

    @Override
    public synchronized void goToTime(final Time t) {
        runUntil(() -> getTime().compareTo(t) < 0);
    }

    private void idleProcessSingleCommand() throws Throwable {
        CheckedRunnable nextCommand = null;
        // This is for spurious wakeups. Blame Java.
        while (nextCommand == null) {
            try {
                nextCommand = commands.take();
                processCommand(nextCommand);
            } catch (InterruptedException e) {
                L.debug("Look! A spurious wakeup! :-)");
            }
        }
    }

    @Override
    public void neighborAdded(final Node<T> node, final Node<T> n) {
        checkCaller();
        afterExecutionUpdates.add(new NeigborAdded(node, n));
    }

    @Override
    public void neighborRemoved(final Node<T> node, final Node<T> n) {
        checkCaller();
        afterExecutionUpdates.add(new NeigborRemoved(node, n));
    }

    private void newStatus(final Status s) {
        if (this.compareStatuses(s) > 0) {
            L.error("Attempt to enter in an illegal status: " + s);
        } else {
            schedule(() -> {
                statusLock.lock();
                try {
                    this.status = s;
                    statusCondition.signalAll();
                } finally {
                    statusLock.unlock();
                }
            });
        }
    }

    @Override
    public void nodeAdded(final Node<T> node) {
        checkCaller();
        afterExecutionUpdates.add(new Addition(node));
    }

    @Override
    public void nodeMoved(final Node<T> node) {
        checkCaller();
        afterExecutionUpdates.add(new Movement(node));
    }

    @Override
    public void nodeRemoved(final Node<T> node, final Neighborhood<T> oldNeighborhood) {
        checkCaller();
        afterExecutionUpdates.add(new Removal(node));
    }

    @Override
    public synchronized void pause() {
        newStatus(Status.PAUSED);
    }

    @Override
    public synchronized void play() {
        newStatus(Status.RUNNING);
    }

    private Stream<Reaction<T>> reactionsToUpdateAfterExecution() {
        return afterExecutionUpdates.stream()
            .flatMap(Update::getReactionsToUpdate)
            .distinct();
    }

    private void processCommand(final CheckedRunnable command) throws Throwable {
        command.run();
        // Update all reactions before applying dependency graph updates
        final Set<Reaction<T>> updated = new LinkedHashSet<>();
        reactionsToUpdateAfterExecution().forEach(r -> {
            updated.add(r);
            updateReaction(r);
        });
        // Now update the dependency graph as needed
        afterExecutionUpdates.forEach(Update::performChanges);
        afterExecutionUpdates.clear();
        // Now update the new reactions
        reactionsToUpdateAfterExecution().forEach(r -> {
            if (!updated.contains(r)) {
                updateReaction(r);
            }
        });
    }

    @Override
    public void removeOutputMonitor(final OutputMonitor<T, P> op) {
        new Thread(() -> {
            monitorLock.write();
            monitors.remove(op);
            monitorLock.release();
        }).start();
    }

    @Override
    public void run() {
        synchronized (env) {
            myThread = Thread.currentThread();
            finalizeConstructor();
            status = Status.READY;
            final long currentThread = Thread.currentThread().getId();
            final long startExecutionTime = System.nanoTime();
            L.trace("Thread {} started running.", currentThread);
            monitorLock.read();
            for (final OutputMonitor<T, P> m : monitors) {
                m.initialized(env);
            }
            monitorLock.release();
            try {
                while (status.equals(Status.READY)) {
                    idleProcessSingleCommand();
                }
                while (status != Status.TERMINATED
                        && currentStep < finalStep
                        && currentTime.compareTo(finalTime) < 0) {
                    while (!commands.isEmpty()) {
                        processCommand(commands.poll());
                    }
                    if (status.equals(Status.RUNNING)) {
                        doStep();
                    }
                    while (status.equals(Status.PAUSED)) {
                        idleProcessSingleCommand();
                    }
                }
            } catch (Throwable e) { // NOPMD: forced by CheckedRunnable
                error = Optional.of(e);
                L.error("The simulation engine crashed.", e);
            } finally {
                status = Status.TERMINATED;
                L.trace("Thread {} execution time: {}", currentThread, (System.nanoTime() - startExecutionTime) / NANOS_TO_SEC);
                commands.clear();
                monitorLock.read();
                for (final OutputMonitor<T, P> m : monitors) {
                    m.finished(env, currentTime, currentStep);
                }
                monitorLock.release();
            }
        }
    }

    private void runUntil(final BooleanSupplier condition) {
        play();
        schedule(() -> {
            while (status == Status.RUNNING && condition.getAsBoolean()) {
                doStep();
            }
        });
        pause();
    }

    @Override
    public synchronized void schedule(final CheckedRunnable r) {
        if (getStatus().equals(Status.TERMINATED)) {
            throw new IllegalStateException("This simulation is terminated and can not get resumed.");
        }
        commands.add(r);
    }

    private void scheduleReaction(final Reaction<T> r) {
        dg.createDependencies(r);
        r.initializationComplete(currentTime, env);
        ipq.addReaction(r);
    }

    @Override
    public synchronized void terminate() {
        newStatus(Status.TERMINATED);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " t: " + getTime() + ", s: " + getStep();
    }

    private void updateReaction(final Reaction<T> r) {
        final Time t = r.getTau();
        r.update(currentTime, false, env);
        if (!r.getTau().equals(t)) {
            ipq.updateReaction(r);
        }
    }

    @Override
    public Status waitFor(final Status s, final long timeout, final TimeUnit tu) {
        if (this.compareStatuses(s) > 0) {
            L.error("Attempt to wait for an illegal status: " + s + " (current state is: " + getStatus() + ")");
        } else {
            statusLock.lock();
            try {
                if (!getStatus().equals(s)) {
                    boolean exit = false;
                    while (!exit) {
                        try {
                            if (timeout > 0) {
                                final boolean isOnTime = statusCondition.await(timeout, tu);
                                if (!isOnTime || getStatus().equals(s)) {
                                    exit = true;
                                }
                            } else {
                                statusCondition.awaitUninterruptibly();
                                if (getStatus().equals(s)) {
                                    exit = true;
                                }
                            }
                        } catch (InterruptedException e) {
                            L.info("A wild spurious wakeup appears! Go, Catch Block! (wild 8-bit music rushes in background)");
                        }
                    }
                }
            } finally {
                statusLock.unlock();
            }
        }
        return getStatus();
    }

    // CHECKSTYLE: FinalClassCheck OFF
    private class Update {
        private final Node<T> source;

        private Update(final Node<T> source) {
            this.source = source;
        }

        public final Stream<Reaction<T>> getReactionsRelatedTo(final Node<T> source, final Neighborhood<T> neighborhood) {
            return Stream.of(
                    source.getReactions().stream(),
                    neighborhood.getNeighbors().stream()
                            .flatMap(node -> node.getReactions().stream())
                            .filter(it -> it.getInputContext() == Context.NEIGHBORHOOD),
                    dg.globalInputContextReactions().stream())
                .reduce(Stream.empty(), Stream::concat);
        }

        public Stream<Reaction<T>> getReactionsToUpdate() {
            return Stream.empty();
        }

        public void performChanges() { }

        protected final Node<T> getSource() {
            return source;
        }
    }

    private final class Movement extends Update {

        private Movement(final Node<T> source) {
            super(source);
        }

        @Override
        public Stream<Reaction<T>> getReactionsToUpdate() {
            return getReactionsRelatedTo(getSource(), env.getNeighborhood(getSource()))
                    .filter(it -> it.getInboundDependencies().stream()
                            .anyMatch(dependency -> dependency.dependsOn(Dependency.MOVEMENT)));
        }

    }

    private final class Removal extends Update {

        private Removal(final Node<T> source) {
            super(source);
        }

        @Override
        public void performChanges() {
            for (final Reaction<T> r : getSource().getReactions()) {
                dg.removeDependencies(r);
                ipq.removeReaction(r);
            }
        }
    }

    private final class Addition extends Update {
        private Addition(final Node<T> source) {
            super(source);
        }
        @Override
        public void performChanges() {
            getSource().getReactions().forEach(Engine.this::scheduleReaction);
        }
    }

    private class NeighborhoodChanged extends Update {

        private final Node<T> target;

        private NeighborhoodChanged(final Node<T> source, final Node<T> target) {
            super(source);
            this.target = target;
        }

        public Node<T> getTarget() {
            return target;
        }

        @Override
        public Stream<Reaction<T>> getReactionsToUpdate() {
            return Stream.of(
                    Stream.concat(
                            // source, target, and all their neighbors are candidates.
                            Stream.of(getSource(), target),
                            Stream.of(env.getNeighborhood(getSource()), env.getNeighborhood(getTarget()))
                                .flatMap(it -> it.getNeighbors().stream()))
                        .distinct()
                        .flatMap(it -> it.getReactions().stream())
                        .filter(it -> it.getInputContext() == Context.NEIGHBORHOOD),
                        // Global reactions
                    dg.globalInputContextReactions().stream())
                .reduce(Stream.empty(), Stream::concat);
        }
    }

    private final class NeigborAdded extends NeighborhoodChanged {

        private NeigborAdded(final Node<T> source, final Node<T> target) {
            super(source, target);
        }

        @Override
        public void performChanges() {
            dg.addNeighbor(getSource(), getTarget());
        }
    }

    private final class NeigborRemoved extends NeighborhoodChanged {

        private NeigborRemoved(final Node<T> source, final Node<T> target) {
            super(source, target);
        }

        @Override
        public void performChanges() {
            dg.removeNeighbor(getSource(), getTarget());
        }
    }
}
