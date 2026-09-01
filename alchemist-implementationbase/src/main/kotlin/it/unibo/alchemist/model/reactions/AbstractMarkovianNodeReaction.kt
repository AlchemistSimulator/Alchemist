/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.timedistributions.ExponentialTime

/** Reaction whose scheduling is driven by a memoryless exponential distribution. */
abstract class AbstractMarkovianNodeReaction<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) :
    AbstractNodeReaction<T>(node, timeDistribution) {

    private var previousRate: Double? = null

    init {
        require(timeDistribution is ExponentialTime<*>) {
            "Markovian reactions require an ExponentialTime distribution, got $timeDistribution"
        }
    }

    override fun scheduleNextOccurrenceAfterFiring(currentTime: Time) {
        val distribution = timeDistribution as ExponentialTime<*>
        val newRate = rate
        check(!newRate.isNaN() && previousRate?.isNaN() != true) { "Reaction propensity cannot be NaN" }
        if (newRate == 0.0) {
            setNextOccurrence(Time.INFINITY)
        } else {
            val sampled = scheduleSampleAfter(maxOf(currentTime, distribution.startTime), distribution)
            setNextOccurrence(sampled)
        }
        previousRate = newRate
    }

    override fun updateSchedulingAfterInvalidation(currentTime: Time) {
        val distribution = timeDistribution as ExponentialTime<*>
        val schedulingTime = maxOf(currentTime, distribution.startTime)
        val newRate = rate
        val oldRate = previousRate
        check(!newRate.isNaN() && oldRate?.isNaN() != true) { "Reaction propensity cannot be NaN" }
        when {
            newRate == 0.0 -> setNextOccurrence(Time.INFINITY)
            oldRate == null || oldRate == 0.0 -> scheduleNextOccurrenceAfterFiring(schedulingTime)
            oldRate == Double.POSITIVE_INFINITY && newRate != Double.POSITIVE_INFINITY -> {
                // An infinite propensity has already consumed the residual and scheduled immediately.
                // Keep that occurrence when the propensity becomes finite, without another draw.
                setNextOccurrence(schedulingTime)
            }
            oldRate != newRate -> {
                val remaining = nextOccurrence.current.minus(schedulingTime)
                setNextOccurrence(schedulingTime.plus(remaining.times(oldRate / newRate)))
            }
        }
        previousRate = newRate
    }

    override fun suspendScheduling() {
        super.suspendScheduling()
        previousRate = 0.0
    }

    private fun scheduleSampleAfter(currentTime: Time, distribution: ExponentialTime<*>): Time {
        val sample = validatedSample()
        val delay = if (distribution.lambda == rate) sample else sample.times(distribution.lambda / rate)
        return currentTime.plus(delay)
    }
}
