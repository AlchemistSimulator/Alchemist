/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.observation.Disposable

/**
 * Represents a simulation engine that manages execution and scheduling.
 * Provides multiple factory methods to simplify the creation process.
 *
 * @param T the concentration type
 * @param P the position type, extending [Position]
 * @param environment the simulation environment
 * @property scheduler the scheduler managing event execution
 *
 * Scheduling observables must emit on the simulation thread. Model mutations from other threads
 * must be submitted through [Simulation.schedule].
 * Reaction execution and scheduled commands form invalidation transactions that refresh each affected reaction
 * once before the next scheduler selection.
 */
open class Engine<T, P : Position<out P>>(environment: Environment<T, P>, protected val scheduler: Scheduler<T>) :
    AbstractEngine<T, P>(environment) {

    private val dirtyReactions = mutableSetOf<Reaction<T>>()

    /*
     * Hosts dispose removed reactions immediately, while engine registration is deferred to a queued command.
     * Tracking the additions whose command is still queued lets a removal cancel them,
     * instead of initializing a reaction that has already been disposed.
     */
    private val pendingAdditions = mutableSetOf<Reaction<T>>()

    private val schedulingSubscriptions = mutableMapOf<Reaction<T>, Disposable>()

    /*
     * A model mutation may synchronously trigger multiple, possibly nested observable cascades.
     * We do not want to reschedule/resample from intermediate model states, thus,
     * this variable stores the depth and refreshDirtyReactions() gets called only when
     * the outermost mutation is complete, so that reactions reschedule once from the final mutated state.
     */
    private var modelMutationDepth = 0

    // The reaction being refreshed: refreshes change nothing but their own next occurrence, so they invalidate nothing.
    private var refreshing: Reaction<T>? = null

    constructor(environment: Environment<T, P>) : this(environment, ArrayIndexedPriorityQueue())

    override fun reactionAdded(reactionToAdd: Reaction<T>) {
        if (state != SimulationState.TERMINATED) {
            pendingAdditions.add(reactionToAdd)
            scheduleUnlessTerminated {
                if (pendingAdditions.remove(reactionToAdd)) {
                    scheduleReaction(reactionToAdd)
                }
            }
        }
    }

    override fun reactionInvalidated(reactionToUpdate: Reaction<T>) {
        checkCaller()
        check(refreshing == null) {
            "Refreshing $refreshing invalidated $reactionToUpdate, but refreshes must change nothing but their own " +
                "next occurrence"
        }
        check(schedulingSubscriptions.containsKey(reactionToUpdate)) {
            "Reaction $reactionToUpdate was invalidated without being scheduled"
        }
        if (modelMutationDepth == 0) {
            modelMutation { dirtyReactions.add(reactionToUpdate) }
        } else {
            dirtyReactions.add(reactionToUpdate)
        }
    }

    override fun reactionRemoved(reactionToRemove: Reaction<T>) {
        if (state != SimulationState.TERMINATED) {
            if (modelMutationDepth > 0) {
                dirtyReactions.remove(reactionToRemove)
            }
            if (!pendingAdditions.remove(reactionToRemove)) {
                scheduleUnlessTerminated { removeReactionIfScheduled(reactionToRemove) }
            }
        }
    }

    override fun afterRun() {
        schedulingSubscriptions.values.forEach { handle ->
            runCatching(handle::dispose).exceptionOrNull()?.let(::recordError)
        }
        schedulingSubscriptions.clear()
        dirtyReactions.clear()
        pendingAdditions.clear()
        // Reactions belong to the environment; afterRun only releases engine-owned subscriptions.
    }

    override fun doStep() {
        val nextEvent = scheduler.next ?: run {
            terminate()
            LOGGER.info("No more reactions.")
            return
        }
        val scheduledTime = nextEvent.nextOccurrence.current
        check(scheduledTime >= time) {
            "$nextEvent is scheduled in the past at time $scheduledTime. Current time: $time; current step: $step."
        }
        if (!scheduledTime.isFinite) {
            terminate()
            LOGGER.info("No finite reactions remain.")
            return
        }
        currentTime = scheduledTime
        check(nextEvent.canExecute.current) {
            "$nextEvent exposed a finite next occurrence while reporting that it could not execute"
        }
        modelMutation {
            nextEvent.execute()
            // Successful execution owns post-firing advancement, which supersedes invalidations caused by its actions.
            dirtyReactions.remove(nextEvent)
        }
        monitors.forEach { it.stepDone(environment, nextEvent, time, step) }
        if (environment.isTerminated) {
            terminate()
            LOGGER.info("Termination condition reached.")
        }
        currentStep = step + 1
    }

    override fun initialize() {
        // Initialization registers every hosted reaction, superseding the additions queued before the run started.
        pendingAdditions.clear()
        environment.reactions.current.forEach(::scheduleReaction)
        environment.nodes.current.forEach { it.reactions.current.forEach(::scheduleReaction) }
    }

    override fun processCommand(command: () -> Unit) = modelMutation {
        super.processCommand(command)
    }

    private inline fun <R> modelMutation(mutation: () -> R): R {
        modelMutationDepth++
        val result = mutation()
        modelMutationDepth--
        if (modelMutationDepth == 0) {
            refreshDirtyReactions()
        }
        return result
    }

    private fun refreshDirtyReactions() {
        val pending = dirtyReactions.toList()
        dirtyReactions.clear()
        pending.forEach { reaction ->
            if (schedulingSubscriptions.containsKey(reaction)) {
                refreshing = reaction
                reaction.updateSchedulingAfterInvalidation(time)
                refreshing = null
            }
        }
    }

    private fun removeReaction(reaction: Reaction<T>) {
        checkCaller()
        dirtyReactions.remove(reaction)
        checkNotNull(schedulingSubscriptions.remove(reaction)) {
            "Reaction $reaction was removed without being scheduled"
        }.dispose()
        scheduler.removeReaction(reaction)
        reaction.dispose()
    }

    private fun removeReactionIfScheduled(reaction: Reaction<T>) {
        if (schedulingSubscriptions.containsKey(reaction)) {
            removeReaction(reaction)
        }
    }

    private fun scheduleReaction(reaction: Reaction<T>) {
        // The scheduler, subscription map, and their callbacks are all owned by the simulation thread.
        checkCaller()
        check(!schedulingSubscriptions.containsKey(reaction)) {
            "Reaction $reaction was scheduled more than once"
        }
        // Registration is fail-fast: AbstractEngine terminates and discards the scheduler if any step throws.
        // Initialization computes the first occurrence before the scheduler reads it.
        reaction.initializationComplete(time, environment)
        val firstOccurrence = reaction.nextOccurrence.current
        require(firstOccurrence >= time) {
            "$reaction cannot be registered at time $time, as its first occurrence $firstOccurrence is in the past"
        }
        scheduler.addReaction(reaction)
        // Do not emit the current value on subscription: scheduler insertion already indexed it.
        schedulingSubscriptions[reaction] =
            reaction.nextOccurrence.onChange(notifyOnRegistration = false) {
                checkCaller()
                scheduler.updateReaction(reaction)
            }
    }

    /*
     * Once the simulation is terminated, nothing is scheduled any more, but hosts may still change, for instance from
     * OutputMonitor.finished or a GUI: their notifications are ignored rather than failing the host mutation.
     * A command queued while the simulation terminates is never processed, as no step follows.
     */
    private fun scheduleUnlessTerminated(command: () -> Unit) {
        if (state != SimulationState.TERMINATED) {
            commands.add(command)
        }
    }
}
