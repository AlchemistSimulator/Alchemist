/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.RealDistributions
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Generates delays by delegating to an Apache Commons Math [RealDistribution].
 *
 */
open class AnyRealDistribution(start: Time, protected val distribution: RealDistribution) :
    AbstractDistribution(start) {

    /**
     * Builds a generator from an Apache Commons Math distribution name.
     *
     * @param rng simulation random generator
     * @param distribution distribution name
     * @param parameters distribution parameters
     */
    constructor(rng: RandomGenerator, distribution: String, vararg parameters: Double) :
        this(Time.ZERO, rng, distribution, *parameters)

    /**
     * Builds a generator from an Apache Commons Math distribution name.
     *
     * @param start initial scheduling time
     * @param rng simulation random generator
     * @param distribution distribution name
     * @param parameters distribution parameters
     */
    constructor(start: Time, rng: RandomGenerator, distribution: String, vararg parameters: Double) :
        this(start, RealDistributions.makeRealDistribution(rng, distribution, *parameters))

    /**
     * Builds a generator delegating to [distribution].
     *
     * @param distribution source distribution, configured with the simulation random generator for reproducibility
     */
    constructor(distribution: RealDistribution) : this(Time.ZERO, distribution)

    /** Mean value exposed by the backing distribution. */
    val mean: Double get() = distribution.numericalMean

    override fun sample(): Time = distribution.sample().let { delay ->
        check(delay.isFinite() && delay >= 0) {
            "$distribution generated an invalid delta time: $delay"
        }
        DoubleTime(delay)
    }
}
