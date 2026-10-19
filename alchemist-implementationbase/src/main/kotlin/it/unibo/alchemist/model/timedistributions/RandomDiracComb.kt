/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

/** A [DiracComb] whose rate is sampled uniformly within the provided bounds for each new instance. */
class RandomDiracComb<T> private constructor(
    private val randomGenerator: RandomGenerator,
    start: Time,
    private val minRate: Double,
    private val maxRate: Double,
    private val randomizeStart: Boolean,
) : DiracComb<T>(start, sampleRate(randomGenerator, minRate, maxRate)) {

    /** Builds a randomized-rate generator with an explicit absolute start time. */
    constructor(randomGenerator: RandomGenerator, start: Time, minRate: Double, maxRate: Double) :
        this(randomGenerator, start, minRate, maxRate, false)

    /** Builds a randomized-rate generator whose initial start is randomized as well. */
    constructor(randomGenerator: RandomGenerator, minRate: Double, maxRate: Double) :
        this(randomGenerator, DoubleTime(minRate * randomGenerator.nextDouble()), minRate, maxRate, true)

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = if (randomizeStart) {
        RandomDiracComb(randomGenerator, minRate, maxRate)
    } else {
        RandomDiracComb(randomGenerator, startTime, minRate, maxRate)
    }

    private companion object {
        private fun sampleRate(randomGenerator: RandomGenerator, minRate: Double, maxRate: Double): Double {
            require(minRate <= maxRate && minRate > 0 && maxRate > 0) {
                "Invalid rate values: {min=$minRate, max=$maxRate}."
            }
            return minRate + (maxRate - minRate) * randomGenerator.nextDouble()
        }
    }
}
