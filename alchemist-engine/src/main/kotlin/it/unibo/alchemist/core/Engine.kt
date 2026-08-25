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
import java.util.IdentityHashMap

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
 */
open class Engine<T, P : Position<out P>>(
    private val environment: Environment<T, P>,
    protected val scheduler: Scheduler<T>,
) : AbstractEngine<T, P>(environment) {

    private val schedulingSubscriptions = IdentityHashMap<Reaction<T>, Disposable>()

    constructor(environment: Environment<T, P>) : this(environment, ArrayIndexedPriorityQueue())

    override fun initialize() {
        environment.reactions.forEach(::scheduleReaction)
        environment.nodes.forEach { it.reactions.forEach(::scheduleReaction) }
    }

    override fun doStep() {
        val nextEvent = scheduler.getNext() ?: run {
            terminate()
            LOGGER.info("No more reactions.")
            return
        }
        val scheduledTime = nextEvent.nextOccurrence.current
        check(scheduledTime >= time) {
            "$nextEvent is scheduled in the past at time $scheduledTime. Current time: $time; current step: $step."
        }
        currentTime = scheduledTime
        val executed = scheduledTime.isFinite && nextEvent.canExecute().current
        if (executed) {
            nextEvent.conditions.forEach { it.reactionReady() }
            nextEvent.execute()
        }
        nextEvent.updateSchedulingAfterFiring(time)

        monitors.forEach { it.stepDone(environment, nextEvent, time, step) }
        if (environment.isTerminated) {
            terminate()
            LOGGER.info("Termination condition reached.")
        }
        currentStep = step + 1
    }

    override fun reactionAdded(reactionToAdd: Reaction<T>) {
        schedule { scheduleReaction(reactionToAdd) }
    }

    override fun reactionRemoved(reactionToRemove: Reaction<T>) {
        schedule { removeReactionIfScheduled(reactionToRemove) }
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
        scheduler.addReaction(reaction)
        // Do not emit the current value on subscription: scheduler insertion already indexed it.
        schedulingSubscriptions[reaction] =
            reaction.nextOccurrence.subscribe(invokeOnSubscription = false) {
                checkCaller()
                scheduler.updateReaction(reaction)
            }
    }

    private fun removeReaction(reaction: Reaction<T>) {
        checkCaller()
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

    override fun afterRun() {
        schedulingSubscriptions.values.forEach { handle ->
            runCatching(handle::dispose).exceptionOrNull()?.let(::recordError)
        }
        schedulingSubscriptions.clear()
        // Reactions belong to the environment; afterRun only releases engine-owned subscriptions.
    }
}
