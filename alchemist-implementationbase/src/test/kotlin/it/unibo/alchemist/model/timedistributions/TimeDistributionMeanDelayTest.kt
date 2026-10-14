/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import arrow.core.some
import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.molecules.SimpleMolecule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import org.apache.commons.math3.distribution.CauchyDistribution
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.distribution.ParetoDistribution
import org.apache.commons.math3.random.Well19937c

class TimeDistributionMeanDelayTest {

    @Test
    fun `a Dirac comb has its period as mean delay`() {
        assertEquals(0.25, DiracComb<Any>(4.0).meanDelay)
    }

    @Test
    fun `a molecule-controlled distribution has the current molecule value as mean delay`() {
        val node = mockk<Node<Any?>>()
        var value = 1.5
        every { node[MOLECULE] } answers { value.some() }
        val distribution = MoleculeControlledTimeDistribution(mockk<Incarnation<Any?, *>>(), node, MOLECULE)
        assertEquals(1.5, distribution.meanDelay)
        value = 3.0
        assertEquals(3.0, distribution.meanDelay)
    }

    @Test
    fun `a Weibull distribution has the empirical average of its samples as mean delay`() {
        listOf(
            WeibullTime<Any>(SHAPE, SCALE, 0.0, Time.ZERO, Well19937c(SEED)),
            WeibullTime(1.0, SCALE, OFFSET, Time.ZERO, Well19937c(SEED)),
            WeibullTime(1.0, 0.2, Time.ZERO, Well19937c(SEED)),
        ).forEach { it.assertMeanDelayMatchesSamples() }
    }

    @Test
    fun `a Weibull distribution with rates arbitrarily close to zero has an infinite mean delay`() {
        assertEquals(Double.POSITIVE_INFINITY, WeibullTime<Any>(1.0, SCALE, 0.0, Time.ZERO, Well19937c(SEED)).meanDelay)
    }

    @Test
    fun `an arbitrary real distribution has the mean of the backing distribution as mean delay`() {
        val random = Well19937c(SEED)
        assertEquals(2.5, AnyRealDistribution<Any>(DiracDeltaDistribution(2.5)).meanDelay)
        assertEquals(Double.POSITIVE_INFINITY, AnyRealDistribution<Any>(ParetoDistribution(random, 1.0, 1.0)).meanDelay)
        assertTrue(AnyRealDistribution<Any>(CauchyDistribution(random, 1.0, 1.0)).meanDelay.isNaN())
    }

    @Test
    fun `an exponential distribution has the reciprocal of its rate as mean delay`() {
        assertEquals(0.5, ExponentialTime<Any>(2.0, Well19937c(SEED)).meanDelay)
    }

    @Test
    fun `network arrivals have the current transmission time as mean delay`() {
        val environment = mockk<Environment<Any, *>>()
        val node = mockk<Node<Any>>()
        every { environment.getNeighborhood(node).current.neighbors } returns persistentListOf()
        val distribution = SimpleNetworkArrivals(mockk(), node, environment, 0.1, 1000.0, 500.0)
        assertEquals(2.1, distribution.meanDelay)
    }

    private fun TimeDistribution<Any>.assertMeanDelayMatchesSamples() {
        val average = List(SAMPLES) { sample().toDouble() }.average()
        assertEquals(average, meanDelay, average * RELATIVE_TOLERANCE, "Mismatched mean delay for $this")
    }

    private companion object {
        val MOLECULE = SimpleMolecule("delay")
        const val OFFSET = 0.5
        const val RELATIVE_TOLERANCE = 0.01
        const val SAMPLES = 1_000_000
        const val SCALE = 2.0
        const val SEED = 0
        const val SHAPE = 4.0
    }
}
