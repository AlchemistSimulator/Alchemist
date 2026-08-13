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
import it.unibo.alchemist.model.reactions.Event
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
    fun `the generic contract only exposes sampling`() {
        assertEquals(listOf("sample"), TimeDistribution::class.java.declaredMethods.map { it.name })
    }

    @Test
    fun `a Dirac comb samples its constant period`() {
        assertEquals(DoubleTime(0.25), DiracComb(4.0).sample())
    }

    @Test
    fun `an arbitrary real distribution delegates sampling`() {
        assertEquals(DoubleTime(2.5), AnyRealDistribution(DiracDeltaDistribution(2.5)).sample())
    }

    @Test
    fun `an arbitrary real distribution rejects invalid delays`() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { invalidSample ->
            val distribution = mockk<RealDistribution>()
            every { distribution.sample() } returns invalidSample
            assertFailsWith<IllegalStateException> {
                AnyRealDistribution(distribution).sample()
            }
        }
    }

    @Test
    fun `exponential samples are finite non-negative delays`() {
        val distribution = ExponentialTime(2.0, Well19937c(0))
        repeat(100) {
            val sample = distribution.sample().toDouble()
            assertTrue(sample.isFinite() && sample >= 0.0)
        }
    }

    @Test
    fun `an infinite exponential rate schedules an immediate reaction`() {
        val reaction = Event(mockk<Node<Any>>(), ExponentialTime(Double.POSITIVE_INFINITY, Well19937c(0)))
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())

        reaction.update(Time.ZERO)

        assertEquals(0.0, reaction.tau.current.toDouble())
    }

    @Test
    fun `Weibull samples are finite non-negative delays`() {
        val distribution = WeibullTime(2.0, 0.5, Well19937c(0))
        repeat(100) {
            val sample = distribution.sample().toDouble()
            assertTrue(sample.isFinite() && sample >= 0.0)
        }
    }

    @Test
    fun `a trigger samples its configured time`() {
        assertEquals(DoubleTime(42.0), Trigger(DoubleTime(42.0)).sample())
    }

    @Test
    fun `reactions own absolute occurrence updates`() {
        val reaction = Event(mockk<Node<Any>>()) { DoubleTime(2.0) }
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())

        reaction.update(DoubleTime(3.0))

        assertEquals(DoubleTime(5.0), reaction.tau.current)
    }

    @Test
    fun `reactions reject invalid custom samples`() {
        val reaction = Event(mockk<Node<Any>>()) { DoubleTime(-1.0) }
        reaction.initializationComplete(Time.ZERO, mockk<Environment<Any, *>>())

        assertFailsWith<IllegalStateException> {
            reaction.update(Time.ZERO)
        }
    }
}
