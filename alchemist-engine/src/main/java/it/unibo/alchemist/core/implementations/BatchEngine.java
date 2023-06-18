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
import java.util.concurrent.TimeUnit;
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

    private final int workersNum;
    private final ExecutorService executorService;
    private final OutputReplayStrategy outputReplayStrategy;

    private final Object executeLock = new Object();
    private final Object updateLock = new Object();

    public BatchEngine(final Environment<T, P> e) {
        super(e);
        this.workersNum = 1;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    public BatchEngine(final Environment<T, P> e, final long maxSteps) {
        super(e, maxSteps);
        this.workersNum = 1;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    public BatchEngine(final Environment<T, P> e, final long maxSteps, final Time t) {
        super(e, maxSteps, t);
        this.workersNum = 1;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    public BatchEngine(final Environment<T, P> e, final Time t) {
        super(e, t);
        this.workersNum = 1;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.outputReplayStrategy = OutputReplayStrategy.AGGREGATE;
    }

    public BatchEngine(
        final Environment<T, P> e,
        final long maxSteps,
        final Time t,
        final int workersNum,
        final OutputReplayStrategy outputReplayStrategy,
        final BatchedScheduler<T> scheduler
    ) {
        super(e, maxSteps, t, scheduler);
        this.workersNum = workersNum;
        this.executorService = Executors.newFixedThreadPool(workersNum);
        this.outputReplayStrategy = outputReplayStrategy;
    }

    @Override
    protected void doStep() {
        final var batchedScheduler = (BatchedScheduler<T>) scheduler;

        final var nextEvents = batchedScheduler.getNextBatch();

        if (nextEvents.isEmpty()) {
            this.newStatus(TERMINATED);
            LOGGER.info("No more reactions.");
            return;
        }

        final var sortededNextEvents = nextEvents.stream().sorted(Comparator.comparing(Actionable::getTau)).collect(Collectors.toList());
        final var minSlidingWindowTime = sortededNextEvents.get(0).getTau();
        final var maxSlidingWindowTime = sortededNextEvents.get(sortededNextEvents.size() - 1).getTau();

        final Function<Actionable<T>, Callable<TaskResult>> taskMapper = event -> () -> doEvent(event, minSlidingWindowTime);
        final var tasks = nextEvents.stream().map(taskMapper).collect(Collectors.toList());

        try {
            final var futureResults = executorService.invokeAll(tasks);
            currentStep += workersNum;
            final var resultsOrderedByTime = futureResults.stream()
                .map(this::unwrapFutureUnsafe)
                .sorted(Comparator.comparing(result -> result.eventTime))
                .collect(Collectors.toList());
            currentTime = maxSlidingWindowTime;
            doStepDoneAllMonitors(resultsOrderedByTime);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    private <V> V unwrapFutureUnsafe(final Future<V> future) {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new IllegalStateException("Expected future to be completed successfully", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted");
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
            safeExecuteEvent(nextEvent);
            safeUpdateEvent(nextEvent);
        }
        nextEvent.update(currentLocalTime, true, environment);
        scheduler.updateReaction(nextEvent);
        if (environment.isTerminated()) {
            this.newStatus(TERMINATED);
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

    private void safeExecuteEvent(final Actionable<T> event) {
        synchronized (executeLock) {
            /*
             * This must be taken before execution, because the reaction
             * might remove itself (or its node) from the environment.
             */
            event.getConditions().forEach(it.unibo.alchemist.model.interfaces.Condition::reactionReady);
            event.execute();
        }
    }

    private void safeUpdateEvent(final Actionable<T> event) {
        synchronized (updateLock) {
            Set<Actionable<T>> toUpdate = dependencyGraph.outboundDependencies(event);
            if (!afterExecutionUpdates.isEmpty()) {
                afterExecutionUpdates.forEach(Update::performChanges);
                afterExecutionUpdates.clear();
                toUpdate = Sets.union(toUpdate, dependencyGraph.outboundDependencies(event));
            }
            toUpdate.forEach(this::updateReaction);
        }
    }

    @Override
    protected void newStatus(final Status next) {
        synchronized (this) {
            super.newStatus(next);
        }
    }

    @Override
    protected void afterRun() {
        try {
            this.executorService.shutdownNow();
            if (!this.executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("Executor failed to terminate");
            }
        } catch (IllegalStateException e) {
            LOGGER.error("Unable to gracefully shudown simulation executor: {}", e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error("Unable to gracefully shudown simulation executor: InterruptedException");
            Thread.currentThread().interrupt();
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
