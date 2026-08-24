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
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.Well19937c
import org.junit.jupiter.api.Test

class TimeDistributionSamplingTest {

    @Test
    fun `the generic contract exposes sampling and fresh instantiation`() {
        assertEquals(
            setOf("sample", "newInstanceOn"),
            TimeDistribution::class.java.declaredMethods.map {
                it.name
            }.toSet(),
        )
    }

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
    fun `exponential samples are finite non-negative delays`() {
        val distribution = ExponentialTime<Any>(2.0, Well19937c(0))
        repeat(100) {
            val sample = distribution.sample().toDouble()
            assertTrue(sample.isFinite() && sample >= 0.0)
        }
    }

    @Test
    fun `an infinite exponential rate schedules an immediate reaction`() {
        val reaction = GenericReaction(mockk<Node<Any>>(), ExponentialTime(Double.POSITIVE_INFINITY, Well19937c(0)))
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())
        reaction.updateSchedulingAfterFiring(Time.ZERO)
        assertEquals(0.0, reaction.nextOccurrence.current.toDouble())
    }

    @Test
    fun `Weibull samples are finite non-negative delays`() {
        val distribution = WeibullTime<Any>(2.0, 0.5, Well19937c(0))
        repeat(100) {
            val sample = distribution.sample().toDouble()
            assertTrue(sample.isFinite() && sample >= 0.0)
        }
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

    private class FixedDistribution(private val delay: Time) : TimeDistribution<Any> {
        override fun sample(): Time = delay

        override fun newInstanceOn(node: Node<Any>): TimeDistribution<Any> = FixedDistribution(delay)
    }
}
