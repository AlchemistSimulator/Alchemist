/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.core.implementations;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

import org.danilopianini.util.concurrent.FastReadWriteLock;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.DependencyGraph;
import it.unibo.alchemist.core.interfaces.DependencyHandler;
import it.unibo.alchemist.core.interfaces.ReactionManager;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

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
    private volatile Status status = Status.INIT;
    private final Lock statusLock = new ReentrantLock();
    private final Condition statusCondition = statusLock.newCondition();
    private final BlockingQueue<CheckedRunnable> commands = new LinkedBlockingQueue<>();
    private final Environment<T, P> env;
    private final DependencyGraph<T> dg;
    private final Map<Reaction<T>, DependencyHandler<T>> handlers = new LinkedHashMap<>();
    private final ReactionManager<T> ipq;
    private final Time finalTime;
    private final FastReadWriteLock monitorLock = new FastReadWriteLock();
    private final List<OutputMonitor<T, P>> monitors = new LinkedList<OutputMonitor<T, P>>();
    private final long steps;
    private Optional<Throwable> error = Optional.empty();
    private Time currentTime = DoubleTime.ZERO_TIME;
    private Reaction<T> mu;
    private long curStep;


    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link ReactionManager} interfaces, don't use this constructor.
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
     * {@link ReactionManager} interfaces, don't use this constructor.
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
        dg = new MapBasedDependencyGraph<T>(env, handlers);
        ipq = new ArrayIndexedPriorityQueue<>();
        this.steps = maxSteps;
        this.finalTime = t;
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link ReactionManager} interfaces, don't use this constructor.
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
        if (!Thread.holdsLock(env)) {
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

    private void doStep() throws InterruptedException, ExecutionException {
        final Reaction<T> root = ipq.getNext();
        if (root == null) {
            this.newStatus(Status.TERMINATED);
            L.info("No more reactions.");
        } else {
            mu = root;
            final Time t = mu.getTau();
            if (t.compareTo(currentTime) < 0) {
                throw new IllegalStateException(mu + "\nis scheduled in the past at time " + t + ", current time is " + currentTime
                        + "\nProblem occurred at step " + curStep);
            }
            currentTime = t;
            if (mu.canExecute()) {
                /*
                 * This must be taken before execution, because the reaction
                 * might remove itself (or its node) from the environment.
                 */
                final List<DependencyHandler<T>> deps = handlers.get(mu).influences();
                mu.execute();
                for (final DependencyHandler<T> r : deps) {
                    updateReaction(r);
                }
            }
            mu.update(currentTime, true, env);
            ipq.updateReaction(root);
            updateMonitors();
        }
        if (env.isTerminated()) {
            newStatus(Status.TERMINATED);
            L.info("Termination condition reached.");
        }
        curStep++;
    }

    private void updateMonitors() {
        monitorLock.read();
        for (final OutputMonitor<T, P> m : monitors) {
            m.stepDone(env, mu, currentTime, curStep);
        }
        monitorLock.release();
    }

    private void finalizeConstructor() {
        for (final Node<T> n : env.getNodes()) {
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
        return steps;
    }

    @Override
    public Time getFinalTime() {
        return finalTime;
    }


    /**
     * @return The IPQ
     */
    public ReactionManager<T> getReactionManager() {
        return ipq;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public long getStep() {
        return curStep;
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
                nextCommand.run();
                updateMonitors();
            } catch (InterruptedException e) {
                L.debug("Look! A spurious wakeup! :-)");
            }
        }
    }

    @Override
    public void neighborAdded(final Node<T> node, final Node<T> n) {
        checkCaller();
        dg.addNeighbor(node, n);
        updateNeighborhood(node);
        /*
         * This is necessary, see bug #43
         */
        updateNeighborhood(n);
    }

    @Override
    public void neighborRemoved(final Node<T> node, final Node<T> n) {
        checkCaller();
        dg.removeNeighbor(node, n);
        updateNeighborhood(node);
        updateNeighborhood(n);
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
        if (status != Status.INIT) {
            for (final Reaction<T> r : node.getReactions()) {
                scheduleReaction(r);
            }
            updateDependenciesForOperationOnNode(env.getNeighborhood(node));
        }
    }

    @Override
    public void nodeMoved(final Node<T> node) {
        checkCaller();
        for (final Reaction<T> r : node.getReactions()) {
            updateReaction(handlers.get(r));
        }
    }

    @Override
    public void nodeRemoved(final Node<T> node, final Neighborhood<T> oldNeighborhood) {
        checkCaller();
        for (final Reaction<T> r : node.getReactions()) {
            removeReaction(r);
        }
    }

    @Override
    public synchronized void pause() {
        newStatus(Status.PAUSED);
    }

    @Override
    public synchronized void play() {
        newStatus(Status.RUNNING);
    }

    @Override
    public void removeOutputMonitor(final OutputMonitor<T, P> op) {
        new Thread(() -> {
            monitorLock.write();
            monitors.remove(op);
            monitorLock.release();
        }).start();
    }

    private void removeReaction(final Reaction<T> r) {
        final DependencyHandler<T> rh = Objects.requireNonNull(handlers.get(r), "The reaction was not part of the simulation: " + r);
        dg.removeDependencies(rh);
        ipq.removeReaction(r);
        handlers.remove(r);
    }

    @Override
    public void run() {
        synchronized (env) {
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
                while (status != Status.TERMINATED && curStep < steps && currentTime.compareTo(finalTime) < 0) {
                    while (!commands.isEmpty()) {
                        commands.poll().run();
                        updateMonitors();
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
                    m.finished(env, currentTime, curStep);
                }
                monitorLock.release();
            }
        }
    }

    private void runUntil(final BooleanSupplier condition) {
        play();
        schedule(() -> {
            try { 
                while (status == Status.RUNNING && condition.getAsBoolean()) {
                    doStep();
                }
            } catch (ExecutionException | InterruptedException e) {
                terminate();
                error = Optional.of(e);
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
        final DependencyHandler<T> rh = new DependencyHandlerImpl<>(r);
        dg.createDependencies(rh);
        r.initializationComplete(currentTime, env);
        ipq.addReaction(r);
        handlers.put(r, rh);
    }

    @Override
    public synchronized void terminate() {
        newStatus(Status.TERMINATED);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " t: " + getTime() + ", s: " + getStep();
    }

    private void updateDependenciesForOperationOnNode(final Neighborhood<T> oldNeighborhood) {
        /*
         * A reaction in the neighborhood may have changed due to the content of
         * this new node. Must check.
         */
        for (final Node<T> n : oldNeighborhood) {
            for (final Reaction<T> r : n.getReactions()) {
                if (r.getInputContext().equals(Context.NEIGHBORHOOD)) {
                    updateReaction(handlers.get(r));
                }
            }
        }
        /*
         * It is possible that some global reaction is changed due to the
         * creation of a new node. Checking.
         */
        for (final Node<T> n : env) {
            for (final Reaction<T> r : n.getReactions()) {
                if (r.getInputContext().equals(Context.GLOBAL)) {
                    updateReaction(handlers.get(r));
                }
            }
        }
    }

    private void updateNeighborhood(final Node<T> n) {
        for (final Reaction<T> r : n.getReactions()) {
            if (r.getInputContext().equals(Context.NEIGHBORHOOD)) {
                updateReaction(handlers.get(r));
            }
        }
    }

    private void updateReaction(final DependencyHandler<T> rh) {
        final Reaction<T> r = rh.getReaction();
        final Time t = r.getTau();
        r.update(currentTime, false, env);
        if (!r.getTau().equals(t)) {
            ipq.updateReaction(rh.getReaction());
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
                            exit = false;
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
}
