/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.timedistributions.AbstractDistribution

/**
 * Partial implementation of a [NodeReaction] whose scheduling is driven by observable model state.
 *
 * @param T concentration type
 */
abstract class AbstractNodeReaction<T>(
    final override val host: Node<T>,
    final override val timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(timeDistribution.startTime),
    NodeReaction<T>,
    TimeDistributedReaction<T> {

    override val rate: Double get() = 1 / timeDistribution.meanDelay

    /**
     * @return a [String] representation of the rate
     */
    override val rateAsString: String get() = rate.toString()

    /** Whether this reaction is awaiting initialization as a newly instantiated program. */
    protected val isNewlyInstantiatedProgram: Boolean get() = newlyInstantiatedAt != null

    private var newlyInstantiatedAt: Time? = null

    /** Refreshes reaction state after firing, then applies firing scheduling policy. */
    final override fun updateSchedulingAfterFiring(currentTime: Time) {
        val environment = checkNotNull(initializedEnvironment) {
            "Reaction $this was advanced before initialization"
        }
        lastKnownTime = currentTime
        refreshReactionState(currentTime, environment)
        if (canExecute.current) {
            scheduleNextOccurrenceAfterFiring(currentTime)
        } else {
            suspendScheduling()
        }
    }

    override fun afterInitializationComplete(atTime: Time, environment: Environment<T, *>) {
        newlyInstantiatedAt?.let { cloneTime ->
            val schedulingTime = maxOf(cloneTime, atTime)
            refreshReactionState(schedulingTime, environment)
            initializeNewProgramScheduling(schedulingTime)
            newlyInstantiatedAt = null
        }
    }

    /** Starts a newly instantiated program without interpreting the initialization as a previous occurrence. */
    protected open fun initializeNewProgramScheduling(currentTime: Time) {
        scheduleNextOccurrenceAfterFiring(currentTime)
    }

    /**
     * Creates a clone and populates it with cloned actions and conditions.
     */
    protected fun <R : AbstractNodeReaction<T>> makeClone(
        node: Node<T>,
        currentTime: Time,
        builder: (TimeDistribution<T>) -> R,
    ): R {
        val freshGenerator = timeDistribution.newInstanceOn(node)
        check(freshGenerator !== timeDistribution) {
            "The time distribution of $this returned itself from newInstanceOn($node)"
        }
        return prepareClone(builder(freshGenerator), currentTime)
    }

    /**
     * Populates a freshly constructed specialized reaction with cloned actions and conditions.
     */
    protected fun <R : AbstractNodeReaction<T>> prepareClone(result: R, currentTime: Time): R = result.also { clone ->
        clone.conditions = conditions.map { condition -> condition.cloneCondition(clone) }
        clone.actions = actions.map { action -> action.cloneAction(clone) }
        clone.newlyInstantiatedAt = currentTime
    }

    /** Applies scheduling policy after a reactive invalidation without firing the reaction. */
    override fun scheduleAfterInvalidation(currentTime: Time) {
        val schedulingTime = maxOf(currentTime, timeDistribution.startTime)
        setNextOccurrence(schedulingTime.plus(validatedSample()))
    }

    /**
     * Applies this reaction's scheduling policy after firing.
     *
     * The default policy draws a new delay after each firing and invalidation.
     *
     * @param currentTime current simulation time
     */
    protected open fun scheduleNextOccurrenceAfterFiring(currentTime: Time) {
        val schedulingTime = maxOf(currentTime, timeDistribution.startTime)
        setNextOccurrence(schedulingTime.plus(validatedSample()))
    }

    protected fun validatedSample(): Time = timeDistribution.sample().also { sample ->
        check(sample.isFinite && sample >= Time.ZERO) {
            "$timeDistribution generated an invalid delay: $sample"
        }
    }

    private companion object {
        val TimeDistribution<*>.startTime: Time
            get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO
    }
}
