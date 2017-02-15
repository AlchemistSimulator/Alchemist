/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

import org.danilopianini.concurrency.FastReadWriteLock;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;

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
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;

/**
 * This class implements a simulation. It offers a wide number of static
 * factories to ease the creation process.
 * 
 * @param <T>
 */
public class Engine<T> implements Simulation<T> {

    private static final Logger L = LoggerFactory.getLogger(Engine.class);

    private volatile Status status = Status.INIT;
    private final Lock statusLock = new ReentrantLock();
    private final Condition statusCondition = statusLock.newCondition();

    private final BlockingQueue<CheckedRunnable> commands = new LinkedBlockingQueue<>();

    private final Environment<T> env;
    private final DependencyGraph<T> dg;
    private final Map<Reaction<T>, DependencyHandler<T>> handlers = new LinkedHashMap<>();
    private final ReactionManager<T> ipq;
    private final Time finalTime;

    private final FastReadWriteLock monitorLock = new FastReadWriteLock();
    private final List<OutputMonitor<T>> monitors = new LinkedList<OutputMonitor<T>>();

    private Reaction<T> mu;
    private final long steps;
    private Time currentTime = DoubleTime.ZERO_TIME;
    private long curStep;
    private Optional<Throwable> error = Optional.empty();

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
    public Engine(final Environment<T> e, final Time t) {
        this(e, Long.MAX_VALUE, t);
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
     */
    public Engine(final Environment<T> e, final long maxSteps) {
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
    public Engine(final Environment<T> e, final long maxSteps, final Time t) {
        env = e;
        env.setSimulation(this);
        dg = new MapBasedDependencyGraph<T>(env, handlers);
        ipq = new ArrayIndexedPriorityQueue<>();
        this.steps = maxSteps;
        this.finalTime = t;
    }

    @Override
    public void addOutputMonitor(final OutputMonitor<T> op) {
            monitorLock.write();
            monitors.add(op);
            monitorLock.release();
    }


    @Override
    public void removeOutputMonitor(final OutputMonitor<T> op) {
        new Thread(() -> {
            monitorLock.write();
            monitors.remove(op);
            monitorLock.release();
        }).start();
    }

    private void finalizeConstructor() {
        for (final Node<T> n : env.getNodes()) {
            for (final Reaction<T> r : n.getReactions()) {
                scheduleReaction(r);
            }
        }
    }

    private void scheduleReaction(final Reaction<T> r) {
        final DependencyHandler<T> rh = new DependencyHandlerImpl<>(r);
        dg.createDependencies(rh);
        r.initializationComplete(currentTime, env);
        ipq.addReaction(r);
        handlers.put(r, rh);
    }

    /**
     * @return the dependency graph
     */
    public DependencyGraph<T> getDependencyGraph() {
        return dg;
    }

    @Override
    public Environment<T> getEnvironment() {
        return env;
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


    private int compareStatuses(final Status o) {
        if ((status == Status.RUNNING || status == Status.PAUSED) && (o == Status.RUNNING || o == Status.PAUSED)) {
            return 0;
        } else {
            return status.compareTo(o);
        }
    }

    @Override
    public void run() {
        synchronized (env) {
            finalizeConstructor();
            status = Status.READY;
            monitorLock.read();
            for (final OutputMonitor<T> m : monitors) {
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
                commands.clear();
                monitorLock.read();
                for (final OutputMonitor<T> m : monitors) {
                    m.finished(env, currentTime, curStep);
                }
                monitorLock.release();
            }
        }
    }

    private void idleProcessSingleCommand() throws Throwable {
        CheckedRunnable nextCommand = null;
        // This is for spurious wakeups. Blame Java.
        while (nextCommand == null) {
            try {
                nextCommand = commands.take();
                nextCommand.run();
            } catch (InterruptedException e) {
                L.debug("Look! A spurious wakeup! :-)");
            }
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
            monitorLock.read();
            for (final OutputMonitor<T> m : monitors) {
                m.stepDone(env, mu, currentTime, curStep);
            }
            monitorLock.release();
        }
        curStep++;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " t: " + getTime() + ", s: " + getStep();
    }

    @Override
    public synchronized void play() {
        newStatus(Status.RUNNING);
    }

    @Override
    public synchronized void pause() {
        newStatus(Status.PAUSED);
    }

    @Override
    public synchronized void terminate() {
        newStatus(Status.TERMINATED);
    }

    @Override
    public Optional<Throwable> getError() {
        return error;
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
    public synchronized void goToStep(final long step) {
        runUntil(() -> getStep() < step);
    }

    @Override
    public synchronized void goToTime(final Time t) {
        runUntil(() -> getTime().compareTo(t) < 0);
    }

    @Override
    public synchronized void schedule(final CheckedRunnable r) {
        if (getStatus().equals(Status.TERMINATED)) {
            throw new IllegalStateException("This simulation is terminated and can not get resumed.");
        }
        commands.add(r);
    }

    /**
     * @param <T>
     *            The format of the concentrations
     * @param env
     *            The environment to be simulated
     * @return A simulation ready to be run, the environment structures get
     *         updated coherently
     */
    public static <T> Engine<T> buildSimulation(final Environment<T> env) {
        return buildSimulation(env, Long.MAX_VALUE);
    }

    /**
     * @param <T>
     *            The format of the concentrations
     * @param env
     *            The environment to be simulated
     * @param t
     *            The final simulation time to reach
     * @return A simulation ready to be run, the environment structures get
     *         updated coherently
     */
    public static <T> Engine<T> buildSimulation(final Environment<T> env, final Time t) {
        return buildSimulation(env, Long.MAX_VALUE, t);
    }

    /**
     * @param <T>
     *            The format of the concentrations
     * @param env
     *            The environment to be simulated
     * @param steps
     *            The number of steps to run
     * @return A simulation ready to be run, the environment structures get
     *         updated coherently
     */
    public static <T> Engine<T> buildSimulation(final Environment<T> env, final long steps) {
        return buildSimulation(env, steps, new DoubleTime(Double.MAX_VALUE));
    }

    /**
     * @param env
     *            The environment to be simulated
     * @param steps
     *            The number of steps to run
     * @param t
     *            The final simulation time to reach
     * @param <T>
     *            The format of the concentrations
     * @return A simulation ready to be run, the environment structures get
     *         updated coherently
     */
    public static <T> Engine<T> buildSimulation(final Environment<T> env, final long steps, final Time t) {
        return new Engine<T>(env, steps, t);
    }

    /**
     * @param env
     *            the environment
     * @param node
     *            first node
     * @param n
     *            second node
     * @param <T>
     *            Type for concentrations
     */
    public void neighborAdded(final Node<T> node, final Node<T> n) {
        dg.addNeighbor(node, n);
        updateNeighborhood(node);
        /*
         * This is necessary, see bug #43
         */
        updateNeighborhood(n);
    }

    /**
     * @param env
     *            the environment
     * @param node
     *            first node
     * @param n
     *            second node
     * @param <T>
     *            Type for concentrations
     */
    public void neighborRemoved(final Environment<T> env, final Node<T> node, final Node<T> n) {
        dg.removeNeighbor(node, n);
        updateNeighborhood(node);
        updateNeighborhood(n);
    }

    /**
     * @param env
     *            the environment
     * @param node
     *            the node
     * @param <T>
     *            Type for concentrations
     */
    public void nodeMoved(final Environment<T> env, final Node<T> node) {
        for (final Reaction<T> r : node.getReactions()) {
            updateReaction(handlers.get(r));
        }
    }

    /**
     * This method provide a facility for adding all the reactions of a node to
     * the current simulation, creating also the dependencies. This method must
     * be called only when it is possible for the environment to successfully
     * compute the neighborhood for the new node.
     * 
     * @param env
     *            the environment
     * @param node
     *            the freshly added node
     * @param <T>
     *            Type for concentrations
     */
    public void nodeAdded(final Node<T> node) {
        if (status != Status.INIT) {
            for (final Reaction<T> r : node.getReactions()) {
                scheduleReaction(r);
            }
            updateDependenciesForOperationOnNode(env.getNeighborhood(node));
        }
    }

    /**
     * This method provide a facility for removing all the reactions of a node
     * from the current simulation, along with their dependencies. This method
     * must be called when it is still possible for the environment to
     * successfully compute the neighborhood for the removed node.
     * 
     * @param env
     *            the environment
     * @param node
     *            the freshly removed node
     * @param oldNeighborhood
     *            the neighborhood of the node as it was before it was removed
     *            (used to calculate reverse dependencies)
     * @param <T>
     *            Type for concentrations
     */
    public void nodeRemoved(final Node<T> node, final Neighborhood<T> oldNeighborhood) {
        for (final Reaction<T> r : node.getReactions()) {
            removeReaction(r);
        }
    }

    private void removeReaction(final Reaction<T> r) {
        final DependencyHandler<T> rh = Objects.requireNonNull(handlers.get(r), "The reaction was not part of the simulation: " + r);
        dg.removeDependencies(rh);
        ipq.removeReaction(r);
        handlers.remove(r);
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

}