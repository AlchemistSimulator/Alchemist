/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies

import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private fun Double.equalityTest(other: Double) = abs(this - other) < 0.0001

private class DummyDistribution : RealDistribution {
    var value: Double = 0.0

    override fun sample() = value

    override fun cumulativeProbability(x: Double) = if (x > value) 1.0 else 0.0

    @Deprecated("Deprecated in Java")
    override fun cumulativeProbability(x0: Double, x1: Double) = if (value > x0 && value < x1) 1.0 else 0.0

    override fun getNumericalMean() = 0.0

    override fun isSupportConnected() = false

    override fun inverseCumulativeProbability(p: Double) = if (p < value) 1.0 else 0.0

    override fun sample(sampleSize: Int) = DoubleArray(sampleSize) { value }

    override fun getNumericalVariance() = 0.0

    override fun probability(x: Double) = if (x.equalityTest(value)) 1.0 else 0.0

    override fun reseedRandomGenerator(seed: Long) {
        value = seed.toDouble()
    }

    override fun density(x: Double) = Double.NaN

    @Deprecated("Deprecated in Java")
    override fun isSupportLowerBoundInclusive() = true

    @Deprecated("Deprecated in Java")
    override fun isSupportUpperBoundInclusive() = true

    override fun getSupportLowerBound() = value

    override fun getSupportUpperBound() = value
}

private class DummyRandomGenerator : RandomGenerator {
    var value = 0.0

    override fun nextBoolean() = value != 0.0

    override fun nextFloat() = value.toFloat()

    override fun setSeed(seed: Int) {
        value = seed.toDouble()
    }

    override fun setSeed(seed: IntArray) {
        value = seed.sum().toDouble()
    }

    override fun setSeed(seed: Long) {
        value = seed.toDouble()
    }

    override fun nextBytes(bytes: ByteArray) = bytes.forEachIndexed { idx, _ ->
        bytes[idx] = value.toInt().toByte()
    }

    override fun nextInt() = value.toInt()

    override fun nextInt(n: Int) = value.toInt() % n

    override fun nextGaussian() = value

    override fun nextDouble() = value

    override fun nextLong() = value.toLong()
}

class TestRandomTarget {
    private lateinit var randomTarget: RandomTarget<Any>
    private lateinit var currentPosition: Euclidean2DPosition
    private lateinit var distanceDistribution: DummyDistribution
    private lateinit var directionGenerator: DummyRandomGenerator

    @BeforeEach
    fun setUp() {
        currentPosition = Euclidean2DPosition(0.0, 0.0)
        distanceDistribution = DummyDistribution()
        directionGenerator = DummyRandomGenerator()
        directionGenerator.value = 0.5
        distanceDistribution.value = 1.0
        randomTarget =
            RandomTarget(
                Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()),
                { currentPosition },
                ::Euclidean2DPosition,
                directionGenerator,
                distanceDistribution,
            )
    }

    @Test
    fun `RandomTarget should change distance according to the distribution`() {
        val target = randomTarget.target.distanceTo(currentPosition)
        assertEquals(target, randomTarget.target.distanceTo(currentPosition))
        assertEquals(target, randomTarget.target.distanceTo(currentPosition))
        distanceDistribution.value = 2.0
        val newTarget = randomTarget.target.distanceTo(currentPosition)
        assertNotEquals(target, newTarget)
        assertEquals(newTarget, randomTarget.target.distanceTo(currentPosition))
        directionGenerator.value = 1.0
        assertEquals(newTarget, randomTarget.target.distanceTo(currentPosition))
    }

    @Test
    fun `RandomTarget should change direction according to the generator`() {
        val target = randomTarget.target.asAngle
        assertEquals(target, randomTarget.target.asAngle)
        assertEquals(target, randomTarget.target.asAngle)
        directionGenerator.value = 1.0
        val newTarget = randomTarget.target.asAngle
        assertNotEquals(target, newTarget)
        assertEquals(newTarget, randomTarget.target.asAngle)
        distanceDistribution.value = 2.0
        assertEquals(newTarget, randomTarget.target.asAngle)
    }
}
