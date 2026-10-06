/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.timedistributions

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.dsl.impl.NumTreeNode
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.Well19937c
import org.danilopianini.lang.HashString

class SAPERETimeDistributionTest {

    @Test
    fun `a fresh instance preserves the rate equation but not the generator object`() {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val node = LsaNode(environment)
        val source = SAPEREExponentialTime("2", DoubleTime(3.0), Well19937c(0))

        val fresh = assertIs<SAPEREExponentialTime>(source.newInstanceOn(node))

        assertNotSame(source, fresh)
        assertEquals(2.0, fresh.rate)
        assertEquals(DoubleTime(3.0), fresh.startTime)
    }

    @Test
    fun `a static rate draws once per exponential delay and ignores matches`() {
        val random = CountingRandomGenerator()
        val distribution = SAPEREExponentialTime("2", random)
        distribution.setMatches(matchingN(8.0))
        assertEquals(2.0, distribution.rate)
        assertEquals(0, random.draws)
        val delays = List(SAMPLES) { distribution.sample().toDouble() }
        assertEquals(SAMPLES, random.draws)
        assertEquals(0.5, delays.average(), 0.5 * RELATIVE_TOLERANCE)
        assertEquals(0.5, delays.standardDeviation(), 0.5 * RELATIVE_TOLERANCE)
    }

    @Test
    fun `a match-driven rate is evaluated against the current matches`() {
        val random = CountingRandomGenerator()
        val distribution = SAPEREExponentialTime("N", random)
        distribution.setMatches(matchingN(4.0))
        assertEquals(4.0, distribution.rate)
        val fastDelays = List(SAMPLES) { distribution.sample().toDouble() }
        assertEquals(0.25, fastDelays.average(), 0.25 * RELATIVE_TOLERANCE)
        distribution.setMatches(matchingN(0.5))
        assertEquals(0.5, distribution.rate)
        val slowDelays = List(SAMPLES) { distribution.sample().toDouble() }
        assertEquals(2.0, slowDelays.average(), 2.0 * RELATIVE_TOLERANCE)
        assertEquals(2 * SAMPLES, random.draws)
    }

    private class CountingRandomGenerator(private val delegate: RandomGenerator = Well19937c(0)) :
        RandomGenerator by delegate {
        var draws = 0
            private set

        override fun nextDouble(): Double = delegate.nextDouble().also { draws++ }

        override fun nextFloat(): Float = delegate.nextFloat().also { draws++ }

        override fun nextGaussian(): Double = delegate.nextGaussian().also { draws++ }

        override fun nextInt(): Int = delegate.nextInt().also { draws++ }

        override fun nextInt(n: Int): Int = delegate.nextInt(n).also { draws++ }

        override fun nextLong(): Long = delegate.nextLong().also { draws++ }

        override fun nextBoolean(): Boolean = delegate.nextBoolean().also { draws++ }

        override fun nextBytes(bytes: ByteArray) = delegate.nextBytes(bytes).also { draws++ }
    }

    private companion object {
        const val SAMPLES = 100_000
        const val RELATIVE_TOLERANCE = 0.01

        fun matchingN(value: Double) = mapOf(HashString("N") to NumTreeNode(value))

        fun List<Double>.standardDeviation(): Double {
            val mean = average()
            return sqrt(sumOf { (it - mean) * (it - mean) } / (size - 1))
        }
    }
}
