/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.reactions

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.physics.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.alchemist.model.timedistributions.AbstractDistribution
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.SimpleNetworkArrivals
import it.unibo.alchemist.model.timedistributions.WeibullTime

/**
 * A global reaction responsible for updating a [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T>(
    /** The physics environment advanced by this reaction. */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T> = DiracComb(DEFAULT_RATE),
) : AbstractReaction<T>(timeDistribution.startTime),
    TimeDistributedReaction<T> {

    constructor(environment: Dynamics2DEnvironment<T>, updateRate: Double) : this(environment, DiracComb(updateRate))

    override val rate: Double get() = timeDistribution.expectedRate

    override fun performModelMutation() = environment.updatePhysics(1 / rate)

    override fun updateSchedulingAfterFiring(currentTime: Time) {
        val sample = timeDistribution.sample()
        check(sample.isFinite && sample >= Time.ZERO) { "$timeDistribution generated an invalid delay: $sample" }
        setNextOccurrence(currentTime.plus(sample))
    }

    override fun scheduleAfterInvalidation(currentTime: Time) = updateSchedulingAfterFiring(currentTime)

    private companion object {
        const val DEFAULT_RATE = 30.0

        private val TimeDistribution<*>.startTime: Time
            get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO

        private val TimeDistribution<*>.expectedRate: Double
            get() = when (this) {
                is DiracComb<*> -> frequency
                is ExponentialTime<*> -> lambda
                is AnyRealDistribution<*> -> mean
                is WeibullTime<*> -> mean
                is SimpleNetworkArrivals<*> -> expectedRate
                else -> Double.NaN
            }
    }
}
