/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import com.google.common.collect.Sets
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
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

    private val outputReplayStrategy: OutputReplayStrategy
    private val executeLock = Any()
    private val updateLock = Any()

    constructor(e: Environment<T, P>?) : super(e) {
        outputReplayStrategy = OutputReplayStrategy.Aggregate
    }

    constructor(e: Environment<T, P>?, maxSteps: Long) : super(e, maxSteps) {
        outputReplayStrategy = OutputReplayStrategy.Aggregate
    }

    constructor(e: Environment<T, P>?, maxSteps: Long, t: Time?) : super(e, maxSteps, t) {
        outputReplayStrategy = OutputReplayStrategy.Aggregate
    }

    constructor(e: Environment<T, P>?, t: Time?) : super(e, t) {
        outputReplayStrategy = OutputReplayStrategy.Aggregate
    }

    constructor(
        e: Environment<T, P>?,
        maxSteps: Long,
        t: Time?,
        outputReplayStrategy: OutputReplayStrategy,
        scheduler: BatchedScheduler<T>?,
    ) : super(e, maxSteps, t, scheduler) {
        this.outputReplayStrategy = outputReplayStrategy
    }

    /**
     * Performs the next simulation step.
     */
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
                val newStep = step + batchSize.toLong()
                setCurrentStep(newStep)
                val resultsOrderedByTime = futureResults
                    .sortedWith(Comparator.comparing { result: TaskResult -> result.eventTime })
                setCurrentTime(if (maxSlidingWindowTime > time) maxSlidingWindowTime else time)
                doStepDoneAllMonitors(resultsOrderedByTime)
            } catch (e: InterruptedException) {
                LOGGER.error(e.message, e)
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun doStepDoneAllMonitors(resultsOrderedByTime: List<TaskResult>): Unit = when (outputReplayStrategy) {
        is OutputReplayStrategy.Reply -> resultsOrderedByTime.forEach(::doStepDoneAllMonitors)
        is OutputReplayStrategy.Aggregate -> doStepDoneAllMonitors(resultsOrderedByTime[resultsOrderedByTime.size - 1])
    }

    private fun doStepDoneAllMonitors(result: TaskResult) {
        for (monitor: OutputMonitor<T, P> in monitors) {
            monitor.stepDone(environment, result.event, time, step)
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
            error(
                nextEvent.toString() + " is scheduled in the past at time " + scheduledTime +
                    ", current time is " + time +
                    ". Problem occurred at step " + step,
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

    /**
     * Safely set simulation status.
     */
    @SuppressWarnings("ForbiddenVoid")
    override fun newStatus(next: Status): CompletableFuture<Void> {
        synchronized(this) { return super.newStatus(next) }
    }

    private inner class TaskResult(val event: Actionable<T>, val eventTime: Time)

    /**
     * This interface represents the way outputs are replied. It is meant for internal use.
     */
    sealed class OutputReplayStrategy {

        protected val name: String = requireNotNull(this::class.simpleName).lowercase()

        /**
         * Outputs are aggregated.
         */
        data object Aggregate : OutputReplayStrategy()

        /**
         * Outputs are replied.
         */
        data object Reply : OutputReplayStrategy()

        /**
         * Converts a [String] to the corresponding [OutputReplayStrategy], based on the name.
         */
        fun String.toReplayStrategy(): OutputReplayStrategy = when (this.lowercase()) {
            Aggregate.name -> Aggregate
            Reply.name -> Reply
            else ->
                error(
                    """
                    Invalid output reply strategy $this. Available choices: ${listOf(Aggregate, Reply).map { it.name }}    
                    """.trimIndent(),
                )
        }
    }
}
