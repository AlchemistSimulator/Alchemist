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
import it.unibo.alchemist.model.timedistributions.DiracComb

/**
 * A reaction hosted by a [Dynamics2DEnvironment], advancing its physics at every occurrence.
 * Without an explicit [timeDistribution] or rate, it fires 30 times per time unit; environments may install their own
 * instance with a different rate (`EnvironmentWithDynamics` uses a rate of 1, unless the simulation provides one).
 */
class PhysicsUpdate<T>(
    /** The physics environment advanced by this reaction. */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T> = DiracComb(DEFAULT_RATE),
) : AbstractReaction<T>(timeDistribution.startTime),
    TimeDistributedReaction<T> {

    override val host: Dynamics2DEnvironment<T> get() = environment

    override val meanDelay: Double get() = timeDistribution.meanDelay

    constructor(environment: Dynamics2DEnvironment<T>, updateRate: Double) : this(environment, DiracComb(updateRate))

    override fun updateSchedulingAfterFiring(currentTime: Time) {
        val sample = timeDistribution.sample()
        check(sample.isFinite && sample >= Time.ZERO) { "$timeDistribution generated an invalid delay: $sample" }
        setNextOccurrence(currentTime.plus(sample))
    }

    override fun performModelMutation() = environment.updatePhysics(meanDelay)

    override fun scheduleAfterInvalidation(currentTime: Time) = updateSchedulingAfterFiring(currentTime)

    private companion object {
        const val DEFAULT_RATE = 30.0

        private val TimeDistribution<*>.startTime: Time
            get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO
    }
}
