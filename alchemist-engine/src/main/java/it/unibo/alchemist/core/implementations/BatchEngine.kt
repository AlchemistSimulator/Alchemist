/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core.implementations

import com.google.common.collect.Sets
import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.interfaces.BatchedScheduler
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Time
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

/**
 * This class implements a simulation. It offers a wide number of static
 * factories to ease the creation process.
 *
 * @param <T> concentration type
 * @param <P> [Position] type
</P></T> */
class BatchEngine<T, P : Position<out P>> :
    Engine<T, P> {

    private val workersNum: Int
    private val outputReplayStrategy: OutputReplayStrategy
    private val executeLock = Any()
    private val updateLock = Any()

    constructor(e: Environment<T, P>?) : super(e) {
        workersNum = 1
        outputReplayStrategy = OutputReplayStrategy.AGGREGATE
    }

    constructor(e: Environment<T, P>?, maxSteps: Long) : super(e, maxSteps) {
        workersNum = 1
        outputReplayStrategy = OutputReplayStrategy.AGGREGATE
    }

    constructor(e: Environment<T, P>?, maxSteps: Long, t: Time?) : super(e, maxSteps, t) {
        workersNum = 1
        outputReplayStrategy = OutputReplayStrategy.AGGREGATE
    }

    constructor(e: Environment<T, P>?, t: Time?) : super(e, t) {
        workersNum = 1
        outputReplayStrategy = OutputReplayStrategy.AGGREGATE
    }

    constructor(
        e: Environment<T, P>?,
        maxSteps: Long,
        t: Time?,
        workersNum: Int,
        outputReplayStrategy: OutputReplayStrategy,
        scheduler: BatchedScheduler<T>?,
    ) : super(e, maxSteps, t, scheduler) {
        this.workersNum = workersNum
        this.outputReplayStrategy = outputReplayStrategy
    }

    override fun doStep() {
        val batchedScheduler = scheduler as BatchedScheduler<T>
        val nextEvents = batchedScheduler.nextBatch
        val batchSize = nextEvents.size
        if (nextEvents.isEmpty()) {
            newStatus(Status.TERMINATED)
            LOGGER.info("No more reactions.")
            return
        }
        val sortededNextEvents =
            nextEvents.stream().sorted(Comparator.comparing(Actionable<T>::tau)).collect(Collectors.toList())
        val minSlidingWindowTime = sortededNextEvents[0].tau
        val maxSlidingWindowTime = sortededNextEvents[sortededNextEvents.size - 1].tau
        runBlocking {
            val taskMapper =
                Function { event: Actionable<T> ->
                    async {
                        doEvent(
                            event,
                            minSlidingWindowTime,
                        )
                    }
                }
            val tasks = nextEvents.stream().map(taskMapper).collect(Collectors.toList())
            try {
                val futureResults = tasks.awaitAll()
                currentStep += batchSize.toLong()
                val resultsOrderedByTime = futureResults
                    .sortedWith(Comparator.comparing { result: TaskResult -> result.eventTime })
                currentTime = maxSlidingWindowTime
                doStepDoneAllMonitors(resultsOrderedByTime)
            } catch (e: InterruptedException) {
                LOGGER.error(e.message, e)
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun doStepDoneAllMonitors(resultsOrderedByTime: List<TaskResult>) {
        monitorLock.acquireUninterruptibly()
        if (outputReplayStrategy == OutputReplayStrategy.REPLAY) {
            resultsOrderedByTime.forEach(
                Consumer { result: TaskResult ->
                    this.doStepDoneAllMonitors(
                        result,
                    )
                },
            )
        } else {
            val lastResult = resultsOrderedByTime[resultsOrderedByTime.size - 1]
            doStepDoneAllMonitors(lastResult)
        }
        monitorLock.release()
    }

    private fun doStepDoneAllMonitors(result: TaskResult) {
        for (monitor: OutputMonitor<T, P> in monitors) {
            monitor.stepDone(environment, result.event, result.event.tau, currentStep)
        }
    }

    private fun doEvent(nextEvent: Actionable<T>, slidingWindowTime: Time): TaskResult {
        validateEventExecutionTime(nextEvent, slidingWindowTime)
        val currentLocalTime = nextEvent.tau
        if (nextEvent.canExecute()) {
            safeExecuteEvent(nextEvent)
            safeUpdateEvent(nextEvent)
        }
        nextEvent.update(currentLocalTime, true, environment)
        scheduler.updateReaction(nextEvent)
        if (environment.isTerminated) {
            newStatus(Status.TERMINATED)
            LOGGER.info("Termination condition reached.")
        }
        return TaskResult(nextEvent, currentLocalTime)
    }

    private fun validateEventExecutionTime(nextEvent: Actionable<T>, slidingWindowTime: Time) {
        val scheduledTime = nextEvent.tau
        if (!isEventTimeScheduledInFirstBatch(scheduledTime) && isEventScheduledBeforeCurrentTime(
                scheduledTime,
                slidingWindowTime,
            )
        ) {
            throw IllegalStateException(
                nextEvent.toString() + " is scheduled in the past at time " + scheduledTime +
                    ", current time is " + currentTime +
                    ". Problem occurred at step " + currentStep,
            )
        }
    }

    private fun isEventTimeScheduledInFirstBatch(scheduledTime: Time): Boolean {
        return scheduledTime.toDouble() == 0.0
    }

    private fun isEventScheduledBeforeCurrentTime(scheduledTime: Time, slidingWindowTime: Time): Boolean {
        return scheduledTime < slidingWindowTime
    }

    private fun safeExecuteEvent(event: Actionable<T>) {
        synchronized(executeLock) {
            /*
             * This must be taken before execution, because the reaction
             * might remove itself (or its node) from the environment.
             */
            event.conditions.forEach {
                it.reactionReady()
            }
            event.execute()
        }
    }

    private fun safeUpdateEvent(event: Actionable<T>) {
        synchronized(updateLock) {
            var toUpdate: Set<Actionable<T>> = dependencyGraph.outboundDependencies(event)
            if (!afterExecutionUpdates.isEmpty()) {
                afterExecutionUpdates.forEach(Consumer<Update> { obj: Update -> obj.performChanges() })
                afterExecutionUpdates.clear()
                toUpdate = Sets.union(
                    toUpdate,
                    dependencyGraph.outboundDependencies(event),
                )
            }
            toUpdate.forEach(Consumer { r: Actionable<T>? -> updateReaction(r) })
        }
    }

    override fun newStatus(next: Status) {
        synchronized(this) { super.newStatus(next) }
    }

    enum class OutputReplayStrategy {
        REPLAY,
        AGGREGATE,
    }

    private inner class TaskResult(val event: Actionable<T>, val eventTime: Time)
}
