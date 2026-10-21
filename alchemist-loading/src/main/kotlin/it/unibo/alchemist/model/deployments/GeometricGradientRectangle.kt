/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign
import org.apache.commons.math3.distribution.ExponentialDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Distributes nodes geometrically within a rectangular shape.
 *
 * @param P position type
 */
class GeometricGradientRectangle<P : Position<out P>> private constructor(
    randomGenerator: RandomGenerator,
    environment: Environment<*, P>,
    nodes: Int,
    x: Double,
    y: Double,
    sizeX: Double,
    sizeY: Double,
    lambda: Double,
    private val continuous: Boolean,
    private val steps: Int,
    private val horizontal: Boolean,
    private val increasing: Boolean,
) : Rectangle<P>(environment, randomGenerator, nodes, x, y, sizeX, sizeY) {
    private val size: Double = abs(if (horizontal) sizeX else sizeY)
    private val exponential: ExponentialDistribution =
        ExponentialDistribution(randomGenerator, size * validLambda(lambda, continuous, steps))

    // The bound in terms of cumulative probability
    private val bound: Double = exponential.cumulativeProbability(size)

    /**
     * Use this constructor to displace multiple groups of devices with
     * exponentially varied density along an axis.
     *
     * @param randomGenerator [RandomGenerator]
     * @param environment [Environment]
     * @param nodes the number of nodes to displace
     * @param x start x position
     * @param y start y position
     * @param sizeX width
     * @param sizeY height
     * @param lambda the lambda parameter of the exponential. The actual lambda is
     *               computed by multiplying this value with the dimension chosen
     *               for the exponential distribution
     * @param steps number of discrete groups. One falls back to uniform
     *              distribution, very large values approximate a continuous
     *              exponential distribution
     * @param horizontal true if the exponential axis is horizontal
     * @param increasing true if device density should increase with the desired axis
     */
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<*, P>,
        nodes: Int,
        x: Double,
        y: Double,
        sizeX: Double,
        sizeY: Double,
        lambda: Double,
        steps: Int,
        horizontal: Boolean,
        increasing: Boolean,
    ) : this(randomGenerator, environment, nodes, x, y, sizeX, sizeY, lambda, false, steps, horizontal, increasing)

    /**
     * Use this constructor to displace devices with an exponentially varied
     * density along an axis.
     *
     * @param randomGenerator [RandomGenerator]
     * @param environment [Environment]
     * @param nodes the number of nodes to displace
     * @param x start x position
     * @param y start y position
     * @param sizeX width
     * @param sizeY height
     * @param lambda the lambda parameter of the exponential. The actual lambda is
     *               computed by multiplying this value with the dimension chosen
     *               for the exponential distribution
     * @param horizontal true if the exponential axis is horizontal
     * @param increasing true if device density should increase with the desired axis
     */
    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<*, P>,
        nodes: Int,
        x: Double,
        y: Double,
        sizeX: Double,
        sizeY: Double,
        lambda: Double,
        horizontal: Boolean,
        increasing: Boolean,
    ) : this(
        randomGenerator,
        environment,
        nodes,
        x,
        y,
        sizeX,
        sizeY,
        lambda,
        true,
        Int.MIN_VALUE,
        horizontal,
        increasing,
    )

    override fun indexToPosition(i: Int): P {
        val sampled = if (increasing) size - nextExponentialRandom() else nextExponentialRandom()
        val alongAxis = when {
            continuous -> sampled
            else -> {
                val groupSize = size / steps
                val groupStart = floor(sampled / groupSize) * groupSize
                randomDouble(groupStart, groupStart + groupSize)
            }
        }
        val positionX = x + sign(width) * if (horizontal) alongAxis else randomDouble(0.0, abs(width))
        val positionY = y + sign(height) * if (horizontal) randomDouble(0.0, abs(height)) else alongAxis
        return makePosition(positionX, positionY)
    }

    /**
     * Generates a random value following the configured exponential distribution.
     */
    private fun nextExponentialRandom(): Double = exponential.inverseCumulativeProbability(randomDouble(0.0, bound))

    private companion object {
        private const val MAX_LAMBDA = 100.0

        /**
         * Validates the parameters before the exponential distribution gets built, returning the [lambda].
         */
        private fun validLambda(lambda: Double, continuous: Boolean, steps: Int): Double {
            require(lambda > 0 && lambda <= MAX_LAMBDA) { "lambda must be in the (0, $MAX_LAMBDA] interval." }
            require(continuous || steps >= 1) { "The number of steps must be greater than 0" }
            return lambda
        }
    }
}
