/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.DependencyGraph;
import it.unibo.alchemist.core.interfaces.Scheduler;
import it.unibo.alchemist.core.interfaces.Simulation;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.Dependency;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Actionable;
import it.unibo.alchemist.model.interfaces.Neighborhood;
import it.unibo.alchemist.model.interfaces.Node;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Reaction;
import it.unibo.alchemist.model.interfaces.Time;
import org.jooq.lambda.fi.lang.CheckedRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import java.util.concurrent.Semaphore;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(Engine.class);
    private static final int ALL_PERMITS = Integer.MAX_VALUE;
    private final Lock statusLock = new ReentrantLock();
    private final ImmutableMap<Status, SynchBox> statusLocks = Arrays.stream(Status.values())
            .collect(ImmutableMap.toImmutableMap(Function.identity(), it -> new SynchBox()));
    private final BlockingQueue<CheckedRunnable> commands = new LinkedBlockingQueue<>();
    private final Queue<Update> afterExecutionUpdates = new ArrayDeque<>();
    private final Environment<T, P> environment;
    private final DependencyGraph<T> dependencyGraph;
    private final Scheduler<T> scheduler;
    private final Time finalTime;
    private final Semaphore monitorLock = new Semaphore(ALL_PERMITS);
    private final List<OutputMonitor<T, P>> monitors = new LinkedList<>();
    private final long finalStep;
    private volatile Status status = Status.INIT;
    private Optional<Throwable> error = Optional.empty();
    private Time currentTime = Time.ZERO;
    private long currentStep;
    private Thread simulationThread;

    /**
     * Builds a simulation for a given environment. By default, it uses a
     * DependencyGraph and an IndexedPriorityQueue internally. If you want to
     * use your own implementations of {@link DependencyGraph} and
     * {@link Scheduler} interfaces, don't use this constructor.
     *
     * @param e
     *            the environment at the initial time
     */
    public Engine(final Environment<T, P> e) {
        this(e, Time.INFINITY);
    }

    /**
     * Builds a simulation for a given environment. By default, it uses a
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
        this(e, maxSteps, Time.INFINITY);
    }

    /**
     * Builds a simulation for a given environment. By default, it uses a
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
    @SuppressFBWarnings(
        value = { "EI_EXPOSE_REP", "MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR" },
        justification = "The environment is stored intentionally, and this class is final"
    )
    public Engine(final Environment<T, P> e, final long maxSteps, final Time t) {
        LOGGER.trace("Engine created");
        environment = e;
        environment.setSimulation(this);
        dependencyGraph = new JGraphTDependencyGraph<>(environment);
        scheduler = new ArrayIndexedPriorityQueue<>();
        this.finalStep = maxSteps;
        this.finalTime = t;
    }

    /**
     * Builds a simulation for a given environment. By default, it uses a
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
        monitorLock.acquireUninterruptibly(ALL_PERMITS);
        monitors.add(op);
        monitorLock.release(ALL_PERMITS);
    }

    private void checkCaller() {
        if (!Thread.currentThread().equals(simulationThread)) {
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
        final Actionable<T> nextEvent = scheduler.getNext();
        if (nextEvent == null) {
            this.newStatus(TERMINATED);
            LOGGER.info("No more reactions.");
        } else {
            final Time scheduledTime = nextEvent.getTau();
            if (scheduledTime.compareTo(currentTime) < 0) {
                throw new IllegalStateException(
                    nextEvent + " is scheduled in the past at time " + scheduledTime
                        + ", current time is " + currentTime
                        + ". Problem occurred at step " + currentStep
                );
            }
            currentTime = scheduledTime;
            if (nextEvent.canExecute()) {
                /*
                 * This must be taken before execution, because the reaction
                 * might remove itself (or its node) from the environment.
                 */
                nextEvent.getConditions().forEach(it.unibo.alchemist.model.interfaces.Condition::reactionReady);
                nextEvent.execute();
                Set<Actionable<T>> toUpdate = dependencyGraph.outboundDependencies(nextEvent);
                if (!afterExecutionUpdates.isEmpty()) {
                    afterExecutionUpdates.forEach(Update::performChanges);
                    afterExecutionUpdates.clear();
                    toUpdate = Sets.union(toUpdate, dependencyGraph.outboundDependencies(nextEvent));
                }
                toUpdate.forEach(this::updateReaction);
            }
            nextEvent.update(currentTime, true, environment);
            scheduler.updateReaction(nextEvent);
            monitorLock.acquireUninterruptibly();
            for (final OutputMonitor<T, P> monitor : monitors) {
                monitor.stepDone(environment, nextEvent, currentTime, currentStep);
            }
            monitorLock.release();
        }
        if (environment.isTerminated()) {
            newStatus(TERMINATED);
            LOGGER.info("Termination condition reached.");
        }
        currentStep++;
    }

    private void finalizeConstructor() {
        this.environment.getGlobalReactions().forEach(this::reactionAdded);
        for (final Node<T> n : environment) {
            for (final Reaction<T> r : n.getReactions()) {
                scheduleReaction(r);
            }
        }
    }

    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Environment<T, P> getEnvironment() {
        return environment;
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
                LOGGER.debug("Look! A spurious wakeup! :-)");
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
                lockForStatus(next).releaseAll();
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
    public void pause() {
        newStatus(PAUSED);
    }

    @Override
    public void play() {
        newStatus(RUNNING);
    }

    @Override
    public void reactionAdded(final Actionable<T> reactionToAdd) {
        reactionChanged(new ReactionAddition(reactionToAdd));
    }

    @Override
    public void reactionRemoved(final Actionable<T> reactionToRemove) {
        reactionChanged(new ReactionRemoval(reactionToRemove));
    }

    private void reactionChanged(final UpdateOnReaction update) {
        checkCaller();
        afterExecutionUpdates.add(update);
    }

    private Stream<? extends Actionable<T>> reactionsToUpdateAfterExecution() {
        return afterExecutionUpdates.stream()
            .flatMap(Update::getReactionsToUpdate)
            .distinct();
    }

    private void processCommand(final CheckedRunnable command) throws Throwable {
        command.run();
        // Update all reactions before applying dependency graph updates
        final Set<Actionable<T>> updated = new LinkedHashSet<>();
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
        monitorLock.acquireUninterruptibly(ALL_PERMITS);
        monitors.remove(op);
        monitorLock.release(ALL_PERMITS);
    }

    @Override
    public void run() {
        synchronized (environment) {
            try {
                simulationThread = Thread.currentThread();
                finalizeConstructor();
                status = Status.READY;
                final long currentThread = Thread.currentThread().getId();
                LOGGER.trace("Thread {} started running.", currentThread);
                monitorLock.acquireUninterruptibly();
                for (final OutputMonitor<T, P> m : monitors) {
                    m.initialized(environment);
                }
                monitorLock.release();
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
                LOGGER.error("The simulation engine crashed.", e);
            } finally {
                status = TERMINATED;
                commands.clear();
                monitorLock.acquireUninterruptibly();
                try {
                    for (final OutputMonitor<T, P> m : monitors) {
                        m.finished(environment, currentTime, currentStep);
                    }
                } catch (Throwable e) { //NOPMD: we need to catch everything
                    error.ifPresentOrElse(
                        error -> error.addSuppressed(e),
                        () -> error = Optional.of(e)
                    );
                } finally {
                    monitorLock.release();
                }
            }
        }
    }

    private void pauseWhen(final BooleanSupplier condition) {
        addOutputMonitor(new OutputMonitor<>() {
            @Override
            public void finished(@Nonnull final Environment<T, P> environment, @Nonnull final Time time, final long step) {
            }

            @Override
            public void initialized(@Nonnull final Environment<T, P> environment) {
                if (condition.getAsBoolean()) {
                    pause();
                }
            }

            @Override
            public void stepDone(
                    @Nonnull final Environment<T, P> environment,
                    @Nullable final Actionable<T> reaction,
                    @Nonnull final Time time,
                    final long step
            ) {
                initialized(environment);
            }
        });
    }

    @Override
    public void schedule(final CheckedRunnable runnable) {
        if (getStatus().equals(TERMINATED)) {
            throw new IllegalStateException("This simulation is terminated and can not get resumed.");
        }
        commands.add(runnable);
    }

    private void scheduleReaction(final Actionable<T> reaction) {
        dependencyGraph.createDependencies(reaction);
        reaction.initializationComplete(currentTime, environment);
        scheduler.addReaction(reaction);
    }

    @Override
    public void terminate() {
        newStatus(TERMINATED);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " t: " + getTime() + ", s: " + getStep();
    }

    private void updateReaction(final Actionable<T> r) {
        final Time t = r.getTau();
        r.update(currentTime, false, environment);
        if (!r.getTau().equals(t)) {
            scheduler.updateReaction(r);
        }
    }

    private SynchBox lockForStatus(final Status status) {
        final var statusLock = statusLocks.get(status);
        if (statusLock == null) {
            throw new IllegalStateException(
                    "Inconsistent state, the Alchemist engine tried to synchronize on a non-existing lock"
                            + "searching for status: " + status + ", available locks: " + statusLocks
            );
        }
        return statusLock;
    }

    @Override
    public Status waitFor(final Status next, final long timeout, final TimeUnit tu) {
        return lockForStatus(next).waitFor(next, timeout, tu);
    }

    // CHECKSTYLE: FinalClassCheck OFF
    private class Update {

        public void performChanges() { }

        public Stream<? extends Actionable<T>> getReactionsToUpdate() {
            return Stream.empty();
        }
    }

    private final class Movement extends Update {

        private final @Nonnull Node<T> sourceNode;

        private Movement(final @Nonnull Node<T> sourceNode) {
            this.sourceNode = sourceNode;
        }

        @Override
        public Stream<? extends Actionable<T>> getReactionsToUpdate() {
            return getReactionsRelatedTo(this.sourceNode, environment.getNeighborhood(this.sourceNode))
                .filter(it ->
                    it.getInboundDependencies().stream()
                        .anyMatch(dependency -> dependency.dependsOn(Dependency.MOVEMENT))
                );
        }

        private Stream<? extends Actionable<T>> getReactionsRelatedTo(
            final Node<T> source,
            final Neighborhood<T> neighborhood
        ) {
            return Stream.of(
                    source.getReactions().stream(),
                    neighborhood.getNeighbors().stream()
                        .flatMap(node -> node.getReactions().stream())
                        .filter(it -> it.getInputContext() == Context.NEIGHBORHOOD),
                    dependencyGraph.globalInputContextReactions().stream())
                .reduce(Stream.empty(), Stream::concat);
        }
    }

    private class UpdateOnNode extends Update {

        private final Function<Reaction<T>, Update> reactionLevelOperation;

        private final Node<T> sourceNode;

        private UpdateOnNode(final Node<T> sourceNode, final Function<Reaction<T>, Update> reactionLevelOperation) {
            this.sourceNode = sourceNode;
            this.reactionLevelOperation = reactionLevelOperation;
        }

        @Override
        public final void performChanges() {
            this.sourceNode.getReactions().stream()
                    .map(reactionLevelOperation)
                    .forEach(Update::performChanges);
        }
    }

    private final class NodeRemoval extends UpdateOnNode {
        private NodeRemoval(final Node<T> sourceNode) {
            super(sourceNode, ReactionRemoval::new);
        }
    }

    private final class NodeAddition extends UpdateOnNode {
        private NodeAddition(final Node<T> sourceNode) {
            super(sourceNode, ReactionAddition::new);
        }
    }

    private abstract class UpdateOnReaction extends Update {

        private final Actionable<T> sourceActionable;

        private UpdateOnReaction(final Actionable<T> sourceActionable) {
            this.sourceActionable = sourceActionable;
        }

        @Override
        public final Stream<? extends Actionable<T>> getReactionsToUpdate() {
            return Stream.of(sourceActionable);
        }

        protected Actionable<T> getSourceReaction() {
            return sourceActionable;
        }
    }

    private final class ReactionRemoval extends UpdateOnReaction {

        private ReactionRemoval(final Actionable<T> source) {
            super(source);
        }

        @Override
        public void performChanges() {
            dependencyGraph.removeDependencies(getSourceReaction());
            scheduler.removeReaction(getSourceReaction());
        }
    }

    private final class ReactionAddition extends UpdateOnReaction {

        private ReactionAddition(final Actionable<T> source) {
            super(source);
        }

        @Override
        public void performChanges() {
            Engine.this.scheduleReaction(getSourceReaction());
        }
    }

    private class NeighborhoodChanged extends Update {

        private final Node<T> sourceNode;
        private final Node<T> targetNode;

        private NeighborhoodChanged(final Node<T> sourceNode, final Node<T> targetNode) {
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
        }

        public Node<T> getTargetNode() {
            return targetNode;
        }

        public Node<T> getSourceNode() {
            return sourceNode;
        }

        @Override
        public Stream<? extends Actionable<T>> getReactionsToUpdate() {
            return Stream.of(
                    Stream.concat(
                            // source, target, and all their neighbors are candidates.
                            Stream.of(sourceNode, targetNode),
                            Stream.of(environment.getNeighborhood(sourceNode), environment.getNeighborhood(getTargetNode()))
                                    .flatMap(it -> it.getNeighbors().stream()))
                            .distinct()
                            .flatMap(it -> it.getReactions().stream())
                            .filter(it -> it.getInputContext() == Context.NEIGHBORHOOD),
                    // Global reactions
                    dependencyGraph.globalInputContextReactions().stream())
                    .reduce(Stream.empty(), Stream::concat);
        }
    }

    private final class NeigborAdded extends NeighborhoodChanged {

        private NeigborAdded(final Node<T> source, final Node<T> target) {
            super(source, target);
        }

        @Override
        public void performChanges() {
            dependencyGraph.addNeighbor(getSourceNode(), getTargetNode());
        }
    }

    private final class NeigborRemoved extends NeighborhoodChanged {

        private NeigborRemoved(final Node<T> source, final Node<T> target) {
            super(source, target);
        }

        @Override
        public void performChanges() {
            dependencyGraph.removeNeighbor(getSourceNode(), getTargetNode());
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
                        LOGGER.info("Spurious wakeup?", e);
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
