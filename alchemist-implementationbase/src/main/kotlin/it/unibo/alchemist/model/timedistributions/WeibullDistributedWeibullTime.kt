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

/** Weibull-distributed events whose per-device parameters are themselves Weibull-distributed. */
class WeibullDistributedWeibullTime<T> private constructor(
    private val configuredMean: Double,
    private val deviceDeviation: Double,
    private val networkDeviation: Double,
    private val deviationDeviation: Double,
    start: Time,
    private val randomGenerator: RandomGenerator,
    private val randomizeStart: Boolean,
) : WeibullTime<T>(
    weibullValue(configuredMean, networkDeviation, randomGenerator),
    weibullValue(deviceDeviation, deviationDeviation, randomGenerator),
    start,
    randomGenerator,
) {

    /** Builds a generator with a randomized absolute start. */
    constructor(
        mean: Double,
        deviceDeviation: Double,
        networkDeviation: Double,
        randomGenerator: RandomGenerator,
    ) : this(
        mean,
        deviceDeviation,
        networkDeviation,
        0.0,
        DoubleTime(randomGenerator.nextDouble() * mean),
        randomGenerator,
        true,
    )

    /** Builds a generator with an explicit absolute start. */
    constructor(
        mean: Double,
        deviceDeviation: Double,
        networkDeviation: Double,
        start: Time,
        randomGenerator: RandomGenerator,
    ) : this(mean, deviceDeviation, networkDeviation, 0.0, start, randomGenerator, false)

    /** Builds a generator with distributed device means and deviations and an explicit absolute start. */
    constructor(
        mean: Double,
        deviceDeviation: Double,
        networkDeviation: Double,
        deviationDeviation: Double,
        start: Time,
        randomGenerator: RandomGenerator,
    ) : this(mean, deviceDeviation, networkDeviation, deviationDeviation, start, randomGenerator, false)

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = if (randomizeStart) {
        WeibullDistributedWeibullTime(configuredMean, deviceDeviation, networkDeviation, randomGenerator)
    } else {
        WeibullDistributedWeibullTime(
            configuredMean,
            deviceDeviation,
            networkDeviation,
            deviationDeviation,
            startTime,
            randomGenerator,
        )
    }

    private companion object {
        private fun weibullValue(mean: Double, deviation: Double, randomGenerator: RandomGenerator): Double =
            if (deviation > 0) {
                weibullFromMean(mean, deviation, randomGenerator)
                    .inverseCumulativeProbability(randomGenerator.nextDouble())
            } else {
                mean
            }
    }
}
