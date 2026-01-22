/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position

/**
 * Represents a simulation engine that manages execution and scheduling.
 * Provides multiple factory methods to simplify the creation process.
 *
 * @param T the concentration type
 * @param P the position type, extending [Position]
 * @param environment the simulation environment
 * @property scheduler the scheduler managing event execution
 */
open class Engine<T, P : Position<out P>>(
    private val environment: Environment<T, P>,
    protected val scheduler: Scheduler<T>,
) : AbstractEngine<T, P>(environment) {

    constructor(environment: Environment<T, P>) : this(environment, ArrayIndexedPriorityQueue())

    override fun initialize() {
        environment.globalReactions.forEach(::scheduleReaction)
        environment.forEach { it.reactions.forEach(::scheduleReaction) }
    }

    override fun doStep() {
        val nextEvent = scheduler.getNext() ?: run {
            terminate()
            LOGGER.info("No more reactions.")
            return
        }
        val scheduledTime = nextEvent.tau
        check(scheduledTime >= time) {
            "$nextEvent is scheduled in the past at time $scheduledTime. Current time: $time; current step: $step."
        }
        currentTime = scheduledTime
        if (scheduledTime.isFinite && nextEvent.canExecute()) {
            nextEvent.conditions.forEach { it.reactionReady() }
            nextEvent.execute()
        }
        nextEvent.update(time, true, environment)
        scheduler.updateReaction(nextEvent)

        monitors.forEach { it.stepDone(environment, nextEvent, time, step) }
        if (environment.isTerminated) {
            terminate()
            LOGGER.info("Termination condition reached.")
        }
        currentStep = step + 1
    }

    /* No dependency graph to update */
    override fun neighborAdded(node: Node<T>, n: Node<T>) = Unit

    override fun neighborRemoved(node: Node<T>, n: Node<T>) = Unit

    override fun nodeMoved(node: Node<T>) = Unit

    override fun nodeAdded(node: Node<T>) {
        schedule {
            node.reactions.forEach { scheduleReaction(it) }
        }
    }

    override fun nodeRemoved(node: Node<T>, oldNeighborhood: Neighborhood<T>) {
        schedule {
            node.reactions.forEach { removeReaction(it) }
        }
    }

    override fun reactionAdded(reactionToAdd: Actionable<T>) {
        schedule { scheduleReaction(reactionToAdd) }
    }

    override fun reactionRemoved(reactionToRemove: Actionable<T>) {
        schedule { removeReaction(reactionToRemove) }
    }

    private fun scheduleReaction(reaction: Actionable<T>) {
        reaction.initializationComplete(time, environment)
        scheduler.addReaction(reaction)

        reaction.rescheduleRequest.onChange(this, false) {
            updateReaction(reaction)
        }
    }

    protected open fun updateReaction(reaction: Actionable<T>) {
        val previousTau = reaction.tau
        reaction.update(time, false, environment)
        if (reaction.tau != previousTau) {
            scheduler.updateReaction(reaction)
        }
    }

    private fun removeReaction(reaction: Actionable<T>) {
        reaction.rescheduleRequest.stopWatching(this)
        scheduler.removeReaction(reaction)
    }
}
