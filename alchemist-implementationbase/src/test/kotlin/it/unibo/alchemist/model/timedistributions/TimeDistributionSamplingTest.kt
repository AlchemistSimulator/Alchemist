/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.Well19937c

class TimeDistributionSamplingTest {

    @Test
    fun `a Dirac comb samples its constant period`() {
        assertEquals(DoubleTime(0.25), DiracComb<Any>(4.0).sample())
    }

    @Test
    fun `an arbitrary real distribution delegates sampling`() {
        assertEquals(DoubleTime(2.5), AnyRealDistribution<Any>(DiracDeltaDistribution(2.5)).sample())
    }

    @Test
    fun `an arbitrary real distribution rejects invalid delays`() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { invalidSample ->
            val distribution = mockk<RealDistribution>()
            every { distribution.sample() } returns invalidSample
            assertFailsWith<IllegalStateException> {
                AnyRealDistribution<Any>(distribution).sample()
            }
        }
    }

    @Test
    fun `exponential sampling draws once per delay and follows the configured rate`() {
        val random = CountingRandomGenerator()
        val distribution = ExponentialTime<Any>(2.0, DoubleTime(10.0), random)
        assertEquals(0, random.draws)
        val delays = List(SAMPLES) { distribution.sample().toDouble() }
        assertEquals(SAMPLES, random.draws)
        assertTrue(delays.all { it.isFinite() && it >= 0.0 })
        assertEquals(0.5, delays.average(), 0.5 * RELATIVE_TOLERANCE)
        assertEquals(0.5, delays.standardDeviation(), 0.5 * RELATIVE_TOLERANCE)
    }

    @Test
    fun `an infinite exponential rate schedules an immediate reaction`() {
        val reaction = GenericReaction(mockk<Node<Any>>(), ExponentialTime(Double.POSITIVE_INFINITY, Well19937c(0)))
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())
        reaction.updateSchedulingAfterFiring(Time.ZERO)
        assertEquals(0.0, reaction.nextOccurrence.current.toDouble())
    }

    @Test
    fun `Weibull sampling draws once per delay and follows the configured rate law`() {
        val random = CountingRandomGenerator()
        val distribution = WeibullTime<Any>(2.0, 0.5, Time.ZERO, random)
        assertEquals(0, random.draws)
        val delays = List(SAMPLES) { distribution.sample().toDouble() }
        assertEquals(SAMPLES, random.draws)
        assertTrue(delays.all { it.isFinite() && it >= 0.0 })
        val rates = delays.map { 1 / it }
        assertEquals(2.0, rates.average(), 2.0 * RELATIVE_TOLERANCE)
        assertEquals(0.5, rates.standardDeviation(), 0.5 * RELATIVE_TOLERANCE)
    }

    @Test
    fun `a Weibull-distributed Weibull time draws its device parameters once at construction`() {
        val random = CountingRandomGenerator()
        val distribution = WeibullDistributedWeibullTime<Any>(2.0, 0.5, 0.1, 0.1, Time.ZERO, random)
        assertEquals(2, random.draws)
        repeat(SAMPLES) { distribution.sample() }
        assertEquals(2 + SAMPLES, random.draws)
    }

    @Test
    fun `a random Dirac comb draws its rate once and then samples a constant period`() {
        val random = CountingRandomGenerator()
        val distribution = RandomDiracComb<Any>(random, Time.ZERO, 1.0, 4.0)
        assertEquals(1, random.draws)
        val period = distribution.sample()
        assertTrue(period.toDouble() in 0.25..1.0)
        repeat(SAMPLES) { assertEquals(period, distribution.sample()) }
        assertEquals(1, random.draws)
        distribution.newInstanceOn(mockk())
        assertEquals(2, random.draws)
    }

    @Test
    fun `reactions own absolute occurrence updates`() {
        val reaction = GenericReaction(mockk<Node<Any>>(), FixedDistribution(DoubleTime(2.0)))
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())

        reaction.updateSchedulingAfterFiring(DoubleTime(3.0))

        assertEquals(DoubleTime(5.0), reaction.nextOccurrence.current)
    }

    @Test
    fun `reactions reject invalid custom samples`() {
        val reaction = GenericReaction(mockk<Node<Any>>(), FixedDistribution(DoubleTime(-1.0)))
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())
        assertFailsWith<IllegalStateException> {
            reaction.updateSchedulingAfterFiring(Time.ZERO)
        }
    }

    private class CountingRandomGenerator(
        private val delegate: RandomGenerator = Well19937c(SEED),
    ) : RandomGenerator by delegate {
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

    private class FixedDistribution(private val delay: Time) : TimeDistribution<Any> {
        override fun sample(): Time = delay

        override fun newInstanceOn(node: Node<Any>): TimeDistribution<Any> = FixedDistribution(delay)
    }

    private companion object {
        const val SEED = 0
        const val SAMPLES = 100_000
        const val RELATIVE_TOLERANCE = 0.01

        fun List<Double>.standardDeviation(): Double {
            val mean = average()
            return sqrt(sumOf { (it - mean) * (it - mean) } / (size - 1))
        }
    }
}
