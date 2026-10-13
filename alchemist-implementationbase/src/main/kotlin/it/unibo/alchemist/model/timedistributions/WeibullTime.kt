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
import org.apache.commons.math3.analysis.integration.IterativeLegendreGaussIntegrator
import org.apache.commons.math3.distribution.WeibullDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.special.Gamma
import org.apache.commons.math3.util.FastMath

/**
 * Weibull distributed events.
 *
 */
open class WeibullTime<T> private constructor(
    private val randomGenerator: RandomGenerator,
    private val backingDistribution: WeibullDistribution,
    private val offset: Double,
    start: Time,
) : AbstractDistribution<T>(start) {
    /**
     * @return the standard deviation for this distribution.
     */
    val deviation: Double
        get() = FastMath.sqrt(backingDistribution.numericalVariance)

    /**
     * @return the mean for this distribution.
     */
    val mean: Double
        get() = backingDistribution.numericalMean + this.offset

    /**
     * The mean of the delays, which are the reciprocals of the Weibull-distributed samples.
     * Without [offset], it is computed in closed form, and it diverges unless the shape is greater than one;
     * otherwise, the delays are bounded by the reciprocal of [offset], and their mean is integrated numerically.
     * It is undefined for negative offsets, as the delays may be negative.
     */
    override val meanDelay: Double by lazy {
        val shape = backingDistribution.shape
        when {
            offset < 0 -> Double.NaN
            offset == 0.0 && shape <= 1 -> Double.POSITIVE_INFINITY
            offset == 0.0 -> Gamma.gamma(1 - 1 / shape) / backingDistribution.scale
            // E[1 / (X + offset)] is the integral of P(1 / (X + offset) > t) for t from zero to 1 / offset.
            else -> IterativeLegendreGaussIntegrator(
                INTEGRATION_POINTS,
                INTEGRATION_RELATIVE_ACCURACY,
                INTEGRATION_ABSOLUTE_ACCURACY,
            ).integrate(
                INTEGRATION_MAX_EVALUATIONS,
                { t -> backingDistribution.cumulativeProbability(1 / t - offset) },
                0.0,
                1 / offset,
            )
        }
    }

    /**
     * @param mean
     *            mean for this distribution
     * @param deviation
     *            standard deviation for this distribution
     * @param random
     *            {@link RandomGenerator} used internally
     */
    constructor(mean: Double, deviation: Double, random: RandomGenerator) : this(
        mean,
        deviation,
        DoubleTime(random.nextDouble() * mean),
        random,
    )

    /**
     * @param mean
     *            mean for this distribution
     * @param deviation
     *            standard deviation for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    constructor(mean: Double, deviation: Double, start: Time, random: RandomGenerator) : this(
        random,
        weibullFromMean(mean, deviation, random),
        0.0,
        start,
    )

    /**
     * @param shapeParameter
     *            shape parameter for this distribution
     * @param scaleParameter
     *            shape parameter for this distribution
     * @param offsetParameter
     *            minimum possible time interval for this distribution
     * @param start
     *            initial time
     * @param random
     *            {@link RandomGenerator} used internally
     */
    constructor(
        shapeParameter: Double,
        scaleParameter: Double,
        offsetParameter: Double,
        start: Time,
        random: RandomGenerator,
    ) : this(
        random,
        WeibullDistribution(random, shapeParameter, scaleParameter, PREFERRED_INVERSE_CUMULATIVE_ACCURACY),
        offsetParameter,
        start,
    )

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = WeibullTime(
        backingDistribution.shape,
        backingDistribution.scale,
        offset,
        startTime,
        randomGenerator,
    )

    override fun sample(): Time = DoubleTime(1.0 / genSample())

    /**
     * @return a sample from the distribution
     */
    protected fun genSample(): Double =
        backingDistribution.inverseCumulativeProbability(randomGenerator.nextDouble()) + this.offset

    protected companion object {
        private const val INTEGRATION_ABSOLUTE_ACCURACY = 1.0E-12
        private const val INTEGRATION_MAX_EVALUATIONS = 1_000_000
        private const val INTEGRATION_POINTS = 5
        private const val INTEGRATION_RELATIVE_ACCURACY = 1.0E-9
        private const val PREFERRED_INVERSE_CUMULATIVE_ACCURACY = 1.0E-9

        /**
         * Generates a {@link WeibullDistribution} given its mean and standard deviation.
         *
         * @param mean
         *            the mean
         * @param deviation
         *            the standard deviation
         * @param random
         *            the random generator
         * @return a new {@link WeibullDistribution}
         */
        protected fun weibullFromMean(mean: Double, deviation: Double, random: RandomGenerator?): WeibullDistribution {
            val t = FastMath.log(deviation * deviation / (mean * mean) + 1.0)
            var kmin = 0.0
            var kmax: Double
            kmax = 1.0
            while (Gamma.logGamma(1.0 + 2.0 * kmax) - 2.0 * Gamma.logGamma(1.0 + kmax) < t) {
                kmin = kmax
                kmax *= 2.0
            }
            var k: Double
            k = (kmin + kmax) / 2.0
            while (kmin < k && k < kmax) {
                if (Gamma.logGamma(1.0 + 2.0 * k) - 2.0 * Gamma.logGamma(1.0 + k) < t) {
                    kmin = k
                } else {
                    kmax = k
                }
                k = (kmin + kmax) / 2.0
            }
            val shapeParameter = 1.0 / k
            val scaleParameter = mean / FastMath.exp(Gamma.logGamma(1.0 + k))
            return WeibullDistribution(random, shapeParameter, scaleParameter, PREFERRED_INVERSE_CUMULATIVE_ACCURACY)
        }
    }
}
