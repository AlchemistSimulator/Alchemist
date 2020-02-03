/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations;

import com.google.common.collect.ImmutableMap;
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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static it.unibo.alchemist.core.interfaces.Status.PAUSED;
import static it.unibo.alchemist.core.interfaces.Status.RUNNING;
import static it.unibo.alchemist.core.interfaces.Status.TERMINATED;

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
    private final Lock statusLock = new ReentrantLock();
    private final ImmutableMap<Status, SynchBox> statusLocks = Arrays.stream(Status.values())
            .collect(ImmutableMap.toImmutableMap(Function.identity(), it -> new SynchBox()));
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

    private <R> R doOnStatus(final Supplier<R> fun) {
        try {
            statusLock.lock();
            return fun.get();
        } finally {
            statusLock.unlock();
        }
    }

    private void doOnStatus(final Runnable fun) {
        doOnStatus(() -> {
            fun.run();
            return 0;
        });
    }

    private void doStep() {
        final Reaction<T> mu = ipq.getNext();
        if (mu == null) {
            this.newStatus(TERMINATED);
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
            newStatus(TERMINATED);
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
    public void goToStep(final long step) {
        pauseWhen(() -> getStep() >= step);
    }

    @Override
    public void goToTime(final Time t) {
        pauseWhen(() -> getTime().compareTo(t) >= 0);
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

    private void newStatus(final Status next) {
        schedule(() -> doOnStatus(() -> {
            if (next.isReachableFrom(status)) {
                status = next;
                statusLocks.get(next).releaseAll();
            }
        }));
    }

    @Override
    public void nodeAdded(final Node<T> node) {
        checkCaller();
        afterExecutionUpdates.add(new NodeAddition(node));
    }

    @Override
    public void nodeMoved(final Node<T> node) {
        checkCaller();
        afterExecutionUpdates.add(new Movement(node));
    }

    @Override
    public void nodeRemoved(final Node<T> node, final Neighborhood<T> oldNeighborhood) {
        checkCaller();
        afterExecutionUpdates.add(new NodeRemoval(node));
    }

    @Override
    public void reactionAdded(final Reaction<T> reaction) {
        checkCaller();
        afterExecutionUpdates.add(new ReactionAddition(reaction));
    }

    @Override
    public void reactionRemoved(final Reaction<T> reaction) {
        checkCaller();
        afterExecutionUpdates.add(new ReactionRemoval(reaction));
    }

    @Override
    public void pause() {
        newStatus(PAUSED);
    }

    @Override
    public void play() {
        newStatus(RUNNING);
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
                while (status != TERMINATED && currentStep < finalStep && currentTime.compareTo(finalTime) < 0) {
                    while (!commands.isEmpty()) {
                        processCommand(commands.poll());
                    }
                    if (status.equals(RUNNING)) {
                        doStep();
                    }
                    while (status.equals(PAUSED)) {
                        idleProcessSingleCommand();
                    }
                }
            } catch (Throwable e) { // NOPMD: forced by CheckedRunnable
                error = Optional.of(e);
                L.error("The simulation engine crashed.", e);
            } finally {
                status = TERMINATED;
                commands.clear();
                monitorLock.read();
                for (final OutputMonitor<T, P> m : monitors) {
                    m.finished(env, currentTime, currentStep);
                }
                monitorLock.release();
            }
        }
    }

    private void pauseWhen(final BooleanSupplier condition) {
        addOutputMonitor(new OutputMonitor<T, P>() {
            @Override
            public void finished(final Environment<T, P> env, final Time time, final long step) {
            }
            @Override
            public void initialized(final Environment<T, P> env) {
                if (condition.getAsBoolean()) {
                    pause();
                }
            }
            @Override
            public void stepDone(final Environment<T, P> env, final Reaction<T> r, final Time time, final long step) {
                initialized(env);
            }
        });
    }

    @Override
    public void schedule(final CheckedRunnable r) {
        if (getStatus().equals(TERMINATED)) {
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
    public void terminate() {
        newStatus(TERMINATED);
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
    public Status waitFor(final Status next, final long timeout, final TimeUnit tu) {
        return statusLocks.get(next).waitFor(next, timeout, tu);
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

    private final class NodeRemoval extends Update {

        private NodeRemoval(final Node<T> source) {
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

    private final class NodeAddition extends Update {
        private NodeAddition(final Node<T> source) {
            super(source);
        }
        @Override
        public void performChanges() {
            getSource().getReactions().forEach(Engine.this::scheduleReaction);
        }
    }

    private final class ReactionAddition extends Update {
        private Reaction<T> reaction;

        private ReactionAddition(final Reaction<T> reaction) {
            super(reaction.getNode());
            this.reaction = reaction;
        }

        @Override
        public void performChanges() {
            Engine.this.scheduleReaction(reaction);
        }
    }

    //TODO: implement performChanges();
    private final class ReactionRemoval extends Update {
        private Reaction<T> reaction;

        private ReactionRemoval(final Reaction<T> reaction) {
            super(reaction.getNode());
            this.reaction = reaction;
        }

        @Override
        public void performChanges() {
            //TODO
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

    private final class SynchBox {
        private final AtomicInteger queueLength = new AtomicInteger();
        private final Condition statusReached = statusLock.newCondition();
        private final Condition allReleased = statusLock.newCondition();
        public Status waitFor(final Status next, final long timeout, final TimeUnit tu) {
            return doOnStatus(() -> {
                boolean notTimedOut = true;
                while (notTimedOut && next != status && next.isReachableFrom(status)) {
                    try {
                        queueLength.getAndIncrement();
                        notTimedOut = statusReached.await(timeout, tu);
                        queueLength.getAndDecrement();
                    } catch (InterruptedException e) {
                        L.info("Spurious wakeup?", e);
                    }
                }
                if (queueLength.get() == 0) {
                    allReleased.signal();
                }
                return status;
            });
        }
        public void releaseAll() {
            doOnStatus(() -> {
                while (queueLength.get() != 0) {
                    statusReached.signalAll();
                    allReleased.awaitUninterruptibly();
                }
            });
        }
    }

}
