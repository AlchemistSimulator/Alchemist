/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package org.apache.commons.math3.distribution

import org.apache.commons.math3.random.RandomGenerator

/**
 * Models a Real Distribution backed by a Dirac Delta Function.
 * This is similar to a Logistic probability function with a shape whose value tends to zero.
 *
 * In practice, samples from this function return the provided [value] as a constant.
 * The variance is zero, there is no randomness involved, and most of the useful information of a real distribution
 * are actually lost.
 * However, this utility can transform tools meant to work with a probability function in such a way that
 * they work with a constant value (e.g., random walks with a constant step).
 *
 */
class DiracDeltaDistribution constructor(val value: Double) : RealDistribution, java.io.Serializable {

    /**
     * This constructor is meant for reflection compatibility only.
     * [randomGenerator] is unused.
     */
    constructor(
        @Suppress("UNUSED_PARAMETER") randomGenerator: RandomGenerator? = null,
        value: Double,
    ) : this(value)

    override fun probability(x: Double) = if (x == value) 1.0 else 0.0

    override fun density(x: Double) = if (x == value) Double.NaN else 0.0

    override fun cumulativeProbability(x: Double): Double = when {
        x < value -> 0.0
        else -> 1.0
    }

    override fun cumulativeProbability(x0: Double, x1: Double): Double = cumulativeProbability(maxOf(x0, x1))

    override fun inverseCumulativeProbability(p: Double) = Double.NaN

    override fun getNumericalMean() = value

    override fun getNumericalVariance() = 0.0

    override fun getSupportLowerBound() = inverseCumulativeProbability(0.0)

    override fun getSupportUpperBound() = inverseCumulativeProbability(1.0)

    override fun isSupportLowerBoundInclusive() = false

    override fun isSupportUpperBoundInclusive() = false

    override fun isSupportConnected() = false

    override fun reseedRandomGenerator(seed: Long) = Unit

    override fun sample() = value

    override fun sample(sampleSize: Int) = DoubleArray(sampleSize) { value }
}
