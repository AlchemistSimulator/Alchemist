
/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.core

import it.unibo.alchemist.core.Engine
import it.unibo.alchemist.core.Scheduler
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveReactionAdapter
import it.unibo.alchemist.rx.model.adapters.reaction.asReactive
import java.util.LinkedHashSet
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.jooq.lambda.fi.lang.CheckedRunnable
import org.slf4j.LoggerFactory

/**
 * A reactive engine that drives the simulation using [ReactiveReactionAdapter]s and [ObservableEnvironment].
 *
 * Instead of relying on a dependency graph to determine which reactions to update after an execution,
 * this engine relies on the reactive nature of [ReactiveReactionAdapter]. When a reaction's dependencies change,
 * it emits a [ReactiveReactionAdapter.rescheduleRequest], which this engine listens to in order to update
 * the scheduler.
 *
 * @param T concentration type
 * @param P position type
 * @property environment the observable environment.
 * @property scheduler the scheduler
 */
class ReactiveEngine<T, P : Position<out P>>(
    private val environment: ObservableEnvironment<T, P>,
    scheduler: Scheduler<T>,
) : Engine<T, P>(environment, scheduler) {

    private val reactionWrappers = ConcurrentHashMap<Actionable<T>, ReactiveReactionAdapter<T>>()
    private val pendingUpdates = LinkedHashSet<Actionable<T>>()
    private var batchingUpdates = AtomicBoolean(false)

    @Suppress("DuplicatedCode")
    override fun doStep() {
        val nextReaction = scheduler.getNext() ?: run {
            newStatus(Status.TERMINATED)
            LOGGER.info("No more reactions.")
            return
        }
        val scheduledTime = nextReaction.tau
        check(scheduledTime >= time) {
            "$nextReaction is scheduled in the past at time $scheduledTime. Current time: $time; current step: $step."
        }

        currentTime = scheduledTime

        if (scheduledTime.isFinite && nextReaction.canExecute()) {
            nextReaction.conditions.forEach { it.reactionReady() }
            batchingUpdates.set(true)
            try {
                nextReaction.execute()
            } finally {
                batchingUpdates.set(false)
            }
            pendingUpdates.remove(nextReaction)
            pendingUpdates.forEach { updateReactionInScheduler(it, executed = false) }
            pendingUpdates.clear()
        }

        updateReactionInScheduler(nextReaction, executed = true)

        monitors.forEach { it.stepDone(environment, nextReaction, time, step) }
        currentStep = step + 1
        if (environment.isTerminated) {
            newStatus(Status.TERMINATED)
            LOGGER.info("Termination condition reached.")
        }
    }

    override fun run() {
        newStatus(Status.READY)

        environment.nodes.forEach { node ->
            node.reactions.forEach { reaction ->
                scheduleReaction(reaction)
            }
        }

        monitors.forEach { it.initialized(environment) }

        try {
            while (status != Status.TERMINATED && currentTime < Time.INFINITY) {
                processCommands()

                if (status == Status.RUNNING) {
                    doStep()
                }
                processCommandsWhileIn(Status.PAUSED)
            }
        } catch (e: Throwable) {
            error = Optional.of(e)
            LOGGER.error("Simulation crashed", e)
        } finally {
            status = Status.TERMINATED
            commands.clear()
            runCatching {
                monitors.forEach { it.finished(environment, currentTime, step) }
            }.onFailure { e ->
                error.ifPresentOrElse({ it.addSuppressed(e) }, { error = Optional.of(e) })
            }
        }
    }

    private fun processCommands() {
        while (commands.isNotEmpty()) {
            commands.poll()?.run()
        }
    }

    private fun scheduleReaction(actionable: Actionable<T>) {
        // If it's a global dependencies just reschedule it by now.
        if (actionable is Reaction<T>) {
            val reactiveReaction = reactionWrappers.computeIfAbsent(actionable) {
                actionable.asReactive(environment)
            }

            reactiveReaction.initializationComplete(currentTime, environment)
            scheduler.addReaction(reactiveReaction)

            reactiveReaction.rescheduleRequest.onChange(this) {
                if (batchingUpdates.get()) {
                    pendingUpdates.add(reactiveReaction)
                } else {
                    updateReactionInScheduler(reactiveReaction, executed = false)
                }
            }
        } else {
            actionable.initializationComplete(currentTime, environment)
            scheduler.addReaction(actionable)
        }
    }

    private fun removeReaction(actionable: Actionable<T>) {
        if (actionable is Reaction<T>) {
            reactionWrappers.remove(actionable)?.let { wrapper ->
                wrapper.rescheduleRequest.stopWatching(this)
                scheduler.removeReaction(wrapper)
            }
        } else {
            scheduler.removeReaction(actionable)
        }
    }

    private fun updateReactionInScheduler(actionable: Actionable<T>, executed: Boolean) {
        val previousTau = actionable.tau
        actionable.update(currentTime, executed, environment)
        if (actionable.tau != previousTau) {
            scheduler.updateReaction(actionable)
        }
    }

    override fun schedule(runnable: CheckedRunnable) {
        if (status != Status.TERMINATED) {
            commands.add(runnable)
        }
    }

    override fun nodeAdded(node: Node<T>) {
        schedule {
            node.reactions.forEach { scheduleReaction(it) }
        }
    }

    override fun nodeRemoved(node: Node<T>, oldNeighborhood: Neighborhood<T>) {
        schedule { node.reactions.forEach { removeReaction(it) } }
    }

    override fun reactionAdded(reactionToAdd: Actionable<T>) {
        schedule {
            scheduleReaction(reactionToAdd)
        }
    }

    override fun reactionRemoved(reactionToRemove: Actionable<T>) {
        schedule {
            removeReaction(reactionToRemove)
        }
    }

    // reactions are updated through observables
    override fun neighborAdded(node: Node<T>, n: Node<T>) { }
    override fun neighborRemoved(node: Node<T>, n: Node<T>) { }
    override fun nodeMoved(node: Node<T>) { }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ReactiveEngine::class.java)
    }
}
