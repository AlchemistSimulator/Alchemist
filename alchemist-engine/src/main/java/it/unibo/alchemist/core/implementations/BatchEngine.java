/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations;

import com.google.common.collect.Sets;
import it.unibo.alchemist.boundary.interfaces.OutputMonitor;
import it.unibo.alchemist.core.interfaces.BatchedScheduler;
import it.unibo.alchemist.core.interfaces.Status;
import it.unibo.alchemist.model.interfaces.Actionable;
import it.unibo.alchemist.model.interfaces.Environment;
import it.unibo.alchemist.model.interfaces.Position;
import it.unibo.alchemist.model.interfaces.Time;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.unibo.alchemist.core.interfaces.Status.TERMINATED;

/**
 * This class implements a simulation. It offers a wide number of static
 * factories to ease the creation process.
 *
 * @param <T> concentration type
 * @param <P> {@link Position} type
 */
public final class BatchEngine<T, P extends Position<? extends P>> extends Engine<T, P> {

    private final int batchSize;
    private final ExecutorService executorService;
    private final OutputReplayStrategy outputReplayStrategy;

    @Deprecated
    public BatchEngine(final Environment<T, P> e) {
        super(e);
        this.batchSize = 1;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    @Deprecated
    public BatchEngine(final Environment<T, P> e, final long maxSteps) {
        super(e, maxSteps);
        this.batchSize = 1;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    @Deprecated
    public BatchEngine(final Environment<T, P> e, final long maxSteps, final Time t) {
        super(e, maxSteps, t);
        this.batchSize = 1;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    @Deprecated
    public BatchEngine(final Environment<T, P> e, final Time t) {
        super(e, t);
        this.batchSize = 1;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    public BatchEngine(final Environment<T, P> e, final long maxSteps, final Time t, final int batchSize, final OutputReplayStrategy outputReplayStrategy) {
        super(e, maxSteps, t, new ArrayIndexedPriorityBatchedQueue<>());
        this.batchSize = batchSize;
        this.executorService = Executors.newFixedThreadPool(batchSize);
        this.outputReplayStrategy = outputReplayStrategy;
    }

    @Override
    protected void doStep() {
        final BatchedScheduler<T> batchedScheduler = (ArrayIndexedPriorityBatchedQueue<T>) scheduler;

        final List<Actionable<T>> nextEvents = batchedScheduler.getNext(batchSize);
        if (nextEvents.isEmpty()) {
            this.newStatus(TERMINATED);
            LOGGER.info("No more reactions.");
            return;
        }

        final List<Actionable<T>> sortededNextEvents = nextEvents.stream().sorted(Comparator.comparing(Actionable::getTau)).collect(Collectors.toList());
        final Time minSlidingWindowTime = sortededNextEvents.get(0).getTau();
        final Time maxSlidingWindowTime = sortededNextEvents.get(sortededNextEvents.size() - 1).getTau();

        final Function<Actionable<T>, Callable<TaskResult>> taskMapper = event -> () -> doEvent(event, minSlidingWindowTime);
        final List<Callable<TaskResult>> tasks = nextEvents.stream().map(taskMapper).collect(Collectors.toList());

        try {
            final var futureResults = executorService.invokeAll(tasks);
            currentStep += batchSize;
            final var resultsOrderedByTime = futureResults.stream()
                .map(this::unwrapFutureUnsafe)
                .sorted(Comparator.comparing(result -> result.eventTime))
                .collect(Collectors.toList());
            currentTime = maxSlidingWindowTime;
            doStepDoneAllMonitors(resultsOrderedByTime);
        } catch (InterruptedException e) {
            LOGGER.error("{}", e);
            //TODO do something...
        }
    }

    private <V> V unwrapFutureUnsafe(final Future<V> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("Expected future to be completed successfully", e);
        }
    }

    private void doStepDoneAllMonitors(final List<TaskResult> resultsOrderedByTime) {
        monitorLock.acquireUninterruptibly();
        if (this.outputReplayStrategy == OutputReplayStrategy.REPLAY) {
            resultsOrderedByTime.forEach(this::doStepDoneAllMonitors);
        } else {
            final var lastResult = resultsOrderedByTime.get(resultsOrderedByTime.size() - 1);
            doStepDoneAllMonitors(lastResult);
        }
        monitorLock.release();
    }

    private void doStepDoneAllMonitors(final TaskResult result) {
        for (final OutputMonitor<T, P> monitor : monitors) {
            monitor.stepDone(environment, result.event, result.event.getTau(), currentStep);
        }
    }

    private TaskResult doEvent(final Actionable<T> nextEvent, final Time slidingWindowTime) {
        validateEventExecutionTime(nextEvent, slidingWindowTime);
        final Time currentLocalTime = nextEvent.getTau();
        if (nextEvent.canExecute()) {
            /*
             * This must be taken before execution, because the reaction
             * might remove itself (or its node) from the environment.
             */
            nextEvent.getConditions().forEach(it.unibo.alchemist.model.interfaces.Condition::reactionReady);
            safeExecuteEvent(nextEvent);
            Set<Actionable<T>> toUpdate = dependencyGraph.outboundDependencies(nextEvent);
            if (!afterExecutionUpdates.isEmpty()) {
                afterExecutionUpdates.forEach(Update::performChanges);
                afterExecutionUpdates.clear();
                toUpdate = Sets.union(toUpdate, dependencyGraph.outboundDependencies(nextEvent));
            }
            toUpdate.forEach(this::updateReaction);
        }
        nextEvent.update(currentLocalTime, true, environment);
        scheduler.updateReaction(nextEvent);
        if (environment.isTerminated()) {
            newStatus(TERMINATED);
            LOGGER.info("Termination condition reached.");
        }
        return new TaskResult(nextEvent, currentLocalTime);
    }

    private void validateEventExecutionTime(final Actionable<T> nextEvent, final Time slidingWindowTime) {
        final Time scheduledTime = nextEvent.getTau();
        if (!isEventTimeScheduledInFirstBatch(scheduledTime) && isEventScheduledBeforeCurrentTime(scheduledTime, slidingWindowTime)) {
            throw new IllegalStateException(
                nextEvent + " is scheduled in the past at time " + scheduledTime
                    + ", current time is " + currentTime
                    + ". Problem occurred at step " + currentStep
            );
        }
    }

    private boolean isEventTimeScheduledInFirstBatch(final Time scheduledTime) {
        return scheduledTime.toDouble() == 0.0;
    }

    private boolean isEventScheduledBeforeCurrentTime(final Time scheduledTime, final Time slidingWindowTime) {
        return scheduledTime.compareTo(slidingWindowTime) < 0;
    }

    private synchronized void safeExecuteEvent(final Actionable<T> event) {
        event.execute();
    }

    @Override
    protected void updateReaction(final Actionable<T> r) {
        synchronized (this) {
            super.updateReaction(r);
        }
    }

    @Override
    protected synchronized void newStatus(final Status next) {
        synchronized (this) {
            super.newStatus(next);
        }
    }

    public enum OutputReplayStrategy {
        REPLAY,
        AGGREGATE
    }

    private class TaskResult {
        public final Actionable<T> event;
        public final Time eventTime;

        private TaskResult(final Actionable<T> event, final Time eventTime) {
            this.event = event;
            this.eventTime = eventTime;
        }
    }
}
