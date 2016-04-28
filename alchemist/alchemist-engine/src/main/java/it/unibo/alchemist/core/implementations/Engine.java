/*
 * Copyright (C) 2010-2014, Danilo Pianini and contributors
 * listed in the project's pom.xml file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of
 * the GNU General Public License, with a linking exception, as described
 * in the file LICENSE in the Alchemist distribution's top directory.
 */
/**
 * 
 */
package it.unibo.alchemist.core.implementations;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.danilopianini.concurrency.FastReadWriteLock;
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
import it.unibo.alchemist.core.interfaces.Command;

/**
 * This class implements a simulation. It offers a wide number of static
 * factories to ease the creation process.
 * 
 * @param <T>
 */
public class Engine<T> implements Simulation<T> {

    private static final ConcurrentMap<Environment<?>, Engine<?>> MAP = new MapMaker().weakKeys().weakValues()
            .makeMap();

    private static final Logger L = LoggerFactory.getLogger(Engine.class);

    private volatile Status status = Status.INIT;
    private final Lock statusLock = new ReentrantLock();
    private final Condition statusCondition = statusLock.newCondition();

    private final BlockingQueue<Command<T>> commands = new LinkedBlockingQueue<>();

    private final Environment<T> env;
    private final DependencyGraph<T> dg;
    private final Map<Reaction<T>, DependencyHandler<T>> handlers = new LinkedHashMap<>();
    private final ReactionManager<T> ipq;
    private final ExecutorService ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private final Time finalTime;

    private final FastReadWriteLock monitorLock = new FastReadWriteLock();
    private final List<OutputMonitor<T>> monitors = new LinkedList<OutputMonitor<T>>();

    private final boolean threaded;

    private Reaction<T> mu;
    private final long steps;
    private Time currentTime = new DoubleTime();
    private long curStep;

    private final class Updater implements Runnable {

        private final CountDownLatch b;
        private final Reaction<T> r;
        private final Time t;
        private final Environment<T> env;

        Updater(final Environment<T> e, final Reaction<T> re, final Time curTime, final CountDownLatch barrier) {
            r = re;
            t = curTime;
            b = barrier;
            env = e;
        }

        @Override
        public void run() {
            r.update(t, false, env);
            ipq.updateReaction(r);
            b.countDown();
        }
    }

    /**
     * Statically searches for the simulation running some environment, and adds
     * an OutputMonitor to it.
     * 
     * @param env
     *            The current environment
     * @param monitor
     *            The monitor to add
     * @param <T>
     *            The format of the concentrations
     */
    public static <T> void addOutputMonitor(final Environment<T> env, final OutputMonitor<T> monitor) {
        final Simulation<T> sim = fromEnvironment(env);
        if (sim != null) {
            sim.addOutputMonitor(monitor);
        } else {
            L.warn("Cannot connect the simulation: have you already shut it down?");
        }
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
     * Given an environment, returns the corresponding simulation.
     * 
     * @param env
     *            the environment
     * @param <T>
     *            concentrations
     * @return the simulation
     */
    @SuppressWarnings("unchecked")
    public static <T> Engine<T> fromEnvironment(final Environment<T> env) {
        return (Engine<T>) MAP.get(env);
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
    public static <T> void neighborAdded(final Environment<T> env, final Node<T> node, final Node<T> n) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null) {
            sim.dg.addNeighbor(node, n);
            updateNeighborhood(sim, node);
            /*
             * This is necessary, see bug #43
             */
            updateNeighborhood(sim, n);
        }
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
    public static <T> void neighborRemoved(final Environment<T> env, final Node<T> node, final Node<T> n) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null) {
            sim.dg.removeNeighbor(node, n);
            updateNeighborhood(sim, node);
            updateNeighborhood(sim, n);
        }
    }

    /**
     * @param env
     *            the environment
     * @param node
     *            the node
     * @param <T>
     *            Type for concentrations
     */
    public static <T> void nodeMoved(final Environment<T> env, final Node<T> node) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null) {
            for (final Reaction<T> r : node.getReactions()) {
                updateReaction(sim.handlers.get(r), sim.currentTime, sim.ipq, sim.env);
            }
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
    public static <T> void nodeAdded(final Environment<T> env, final Node<T> node) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null && sim.status != Status.INIT) {
            for (final Reaction<T> r : node.getReactions()) {
                final DependencyHandler<T> rh = new DependencyHandlerImpl<>(r);
                sim.dg.createDependencies(rh);
                r.update(sim.currentTime, true, sim.env);
                sim.ipq.addReaction(rh.getReaction());
                sim.handlers.put(r, rh);
            }
            updateDependenciesForOperationOnNode(sim, env, env.getNeighborhood(node));
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
    public static <T> void nodeRemoved(final Environment<T> env, final Node<T> node,
            final Neighborhood<T> oldNeighborhood) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null) {
            for (final Reaction<T> r : node.getReactions()) {
                removeReaction(sim, r);
            }
        }
    }

    /**
     * @param env
     *            the environment
     * @param r
     *            the removed reaction
     * @param <T>
     *            Type for concentrations
     */
    public static <T> void removeReaction(final Environment<T> env, final Reaction<T> r) {
        final Engine<T> sim = fromEnvironment(env);
        if (sim != null) {
            removeReaction(sim, r);
        }
    }

    private static <T> void removeReaction(final Engine<T> sim, final Reaction<T> r) {
        final DependencyHandler<T> rh = sim.handlers.get(r);
        if (rh != null) {
            sim.dg.removeDependencies(rh);
            sim.ipq.removeReaction(r);
            sim.handlers.remove(r);
        }
    }

    private static <T> void updateDependenciesForOperationOnNode(final Engine<T> sim, final Environment<T> env,
            final Neighborhood<T> oldNeighborhood) {
        /*
         * A reaction in the neighborhood may have changed due to the content of
         * this new node. Must check.
         */
        for (final Node<T> n : oldNeighborhood) {
            for (final Reaction<T> r : n.getReactions()) {
                if (r.getInputContext().equals(Context.NEIGHBORHOOD)) {
                    updateReaction(sim.handlers.get(r), sim.currentTime, sim.ipq, sim.env);
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
                    updateReaction(sim.handlers.get(r), sim.currentTime, sim.ipq, sim.env);
                }
            }
        }
    }

    private static <T> void updateNeighborhood(final Engine<T> sim, final Node<T> n) {
        for (final Reaction<T> r : n.getReactions()) {
            if (r.getInputContext().equals(Context.NEIGHBORHOOD)) {
                updateReaction(sim.handlers.get(r), sim.currentTime, sim.ipq, sim.env);
            }
        }
    }

    private static <T> void updateReaction(final DependencyHandler<T> rh, final Time curTime,
            final ReactionManager<T> rm, final Environment<T> env) {
        final Reaction<T> r = rh.getReaction();
        final Time t = r.getTau();
        r.update(curTime, false, env);
        if (!r.getTau().equals(t)) {
            rm.updateReaction(rh.getReaction());
        }
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param t
     *            the maximum time to reach
     */
    public Engine(final Environment<T> e, final Time t) {
        this(e, Long.MAX_VALUE, t, false);
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param t
     *            the maximum time to reach
     * @param multiThread
     *            if true, the simulation will try to parallelize the update
     *            phase
     */
    public Engine(final Environment<T> e, final Time t, final boolean multiThread) {
        this(e, Long.MAX_VALUE, t, multiThread);
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     */
    public Engine(final Environment<T> e, final long maxSteps) {
        this(e, maxSteps, new DoubleTime(Double.POSITIVE_INFINITY), false);
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     * @param multiThread
     *            if true, the simulation will try to parallelize the update
     *            phase
     */
    public Engine(final Environment<T> e, final long maxSteps, final boolean multiThread) {
        this(e, maxSteps, new DoubleTime(Double.POSITIVE_INFINITY), multiThread);
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     * @param t
     *            the maximum time to reach
     */
    public Engine(final Environment<T> e, final long maxSteps, final Time t) {
        this(e, maxSteps, t, false);
    }

    /**
     * Builds a simulation for a given environment. By default it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of DependencyGraph and ReactionManager
     * interfaces, don't use this constructor.
     * 
     * @param e
     *            the environment at the initial time
     * @param maxSteps
     *            the maximum number of steps to do
     * @param t
     *            the maximum time to reach
     * @param multiThread
     *            if true, the simulation will try to parallelize the update
     *            phase
     */
    public Engine(final Environment<T> e, final long maxSteps, final Time t, final boolean multiThread) {
        env = e;
        dg = new MapBasedDependencyGraph<T>(env, handlers);
        ipq = new ArrayIndexedPriorityQueue<>();
        this.steps = maxSteps;
        this.finalTime = t;
        MAP.put(env, this);
        threaded = multiThread;
    }

    @Override
    public void addOutputMonitor(final OutputMonitor<T> op) {
            monitorLock.write();
            monitors.add(op);
            monitorLock.release();
            /*
             * Make sure the environment has been initialized
             */
            monitorLock.read();
            op.stepDone(env, mu, currentTime, curStep);
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
                r.update(r.getTau(), true, env);
                final DependencyHandler<T> rh = new DependencyHandlerImpl<>(r);
                ipq.addReaction(r);
                dg.createDependencies(rh);
                handlers.put(r, rh);
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

    @Override
    public void addCommand(final Command<T> comm) {
        if (comm != null) {
            commands.add(comm);
        }
    }

    private void newStatus(final Status s) {
        if (this.compareStatuses(s) > 0) {
            L.error("Attempt to enter in an illegal status: " + s);
        } else {
            statusLock.lock(); 
            try {
                this.status = s;
                statusCondition.signalAll();
            } finally {
                statusLock.unlock();
            }
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
            while (status.equals(Status.READY)) {
                idleProcessSingleCommand();
            }
            try {
                while (status != Status.STOPPED && curStep < steps && currentTime.compareTo(finalTime) < 0) {
                    if (status.equals(Status.RUNNING)) {
                        while (!commands.isEmpty()) {
                            commands.poll().execute(this);
                        }
                        doStep();
                    }
                    while (status.equals(Status.PAUSED)) {
                        idleProcessSingleCommand();
                    }
                }
            } catch (RuntimeException | InterruptedException | ExecutionException | Error e) {
                L.error("The simulation engine crashed.", e);
            } finally {
                status = Status.STOPPED;
                L.info("Now in STOPPED status");
                commands.clear();
                ex.shutdownNow();
                monitorLock.read();
                for (final OutputMonitor<T> m : monitors) {
                    m.finished(env, currentTime, curStep);
                }
                monitorLock.release();
                MAP.remove(env);
            }
        }
    }

    private void idleProcessSingleCommand() {
        Command<T> nextCommand = null;
        // This is for spurious wakeups. Blame Java.
        while (nextCommand == null) {
            try {
                nextCommand = commands.take();
                nextCommand.execute(this);
            } catch (InterruptedException e) {
                L.debug("Look! A spurious wakeup! :-)");
            }
        }
    }

    private void doStep() throws InterruptedException, ExecutionException {
        final Reaction<T> root = ipq.getNext();
        if (root == null) {
            this.newStatus(Status.STOPPED);
            L.info("No more reactions.");
        } else {
            mu = root;
            final Time t = mu.getTau();
            if (t.compareTo(currentTime) < 0) {
                L.error(mu + "\nis scheduled in the past at time " + t + ", current time is " + currentTime
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
                if (threaded) {
                    final CountDownLatch barrier = new CountDownLatch(deps.size());
                    for (final DependencyHandler<T> r : deps) {
                        final Future<?> res = ex.submit(new Updater(env, r.getReaction(), t, barrier));
                        assert res != null;
                    }
                    mu.update(currentTime, true, env);
                    ipq.updateReaction(root);
                    barrier.await();
                } else {
                    for (final DependencyHandler<T> r : deps) {
                        updateReaction(r, t, ipq, env);
                    }
                    mu.update(currentTime, true, env);
                    ipq.updateReaction(root);
                }
            } else {
                mu.update(currentTime, true, env);
                ipq.updateReaction(root);
            }
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

    private static class SimCommand {

        private final Status state;

        SimCommand(final Status status) {
            state = status;
        }

        private void newState(final Engine<?> s) {
            s.newStatus(state);
        }
    }

    /**
     * This class provides a flexible Builder to create a new {@link Command}
     * whose aim is to change the status of a Engine.
     * 
     * @param <T> concentration
     */
    public static class StateCommand<T> {

        private boolean isValid;
        private Status status;

        /**
         * Sets the desired status to {@link Status#RUNNING}.
         * Calling this method overrides an eventual previous call to 
         * {@link #pause()} and\or {@link #stop()}.
         * 
         * @return the current builder with the updated status
         */
        public StateCommand<T> run() {
            this.isValid = true;
            this.status = Status.RUNNING;
            return this;
        }

        /**
         * Sets the desired status to {@link Status#PAUSED}.
         * Calling this method overrides an eventual previous call to 
         * {@link #run()} and\or {@link #stop()}.
         * 
         * @return the current builder with the updated status
         */
        public StateCommand<T> pause() {
            this.isValid = true;
            this.status = Status.PAUSED;
            return this;
        }

        /**
         * Sets the desired status to {#@link Status#STOPPED}.
         * Calling this method overrides an eventual previous call to 
         * {@link #pause()} and\or {@link #run()}.
         * 
         * @return the current builder with the updated status
         */
        public StateCommand<T> stop() {
            this.isValid = true;
            this.status = Status.STOPPED;
            return this;
        }

        /**
         * Performs the building of an {@link Command} according to the previously called methods.
         * 
         * @return the generated {@link Command}
         */
        public Command<T> build() {
            if (isValid) {
                return s -> new SimCommand(status).newState((Engine<?>) s);
            } else {
                return s -> { };
            }
        }
    }

}
