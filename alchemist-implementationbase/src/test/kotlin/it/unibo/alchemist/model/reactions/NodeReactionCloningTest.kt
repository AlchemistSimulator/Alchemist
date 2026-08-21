/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.MoleculeControlledTimeDistribution
import it.unibo.alchemist.model.timedistributions.RandomDiracComb
import it.unibo.alchemist.model.timedistributions.Trigger
import it.unibo.alchemist.model.timedistributions.WeibullDistributedWeibullTime
import it.unibo.alchemist.model.timedistributions.WeibullTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.random.Well19937c
import org.junit.jupiter.api.Test

class NodeReactionCloningTest {

    private val environment = mockk<Environment<Any, *>>()

    @Test
    fun `a clone owns a new deterministic generator and starts at clone time`() {
        val sourceDistribution = DiracComb<Any>(0.5)
        val source = GenericReaction(mockk<Node<Any>>(), sourceDistribution)
        source.initializationComplete(Time.ZERO, environment)
        source.updateAfterFiring(DoubleTime(3.0))

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertNotSame(sourceDistribution, clone.timeDistribution)
        assertEquals(DoubleTime(5.0), source.nextOccurrence.current)
        assertEquals(DoubleTime(12.0), clone.nextOccurrence.current)
    }

    @Test
    fun `a clone respects the configured absolute start before sampling`() {
        val source = GenericReaction(mockk<Node<Any>>(), DiracComb(DoubleTime(20.0), 0.5))

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertEquals(DoubleTime(22.0), clone.nextOccurrence.current)
    }

    @Test
    fun `a cloned trigger is a new pending program rather than an executed trigger`() {
        val source = GenericReaction(mockk<Node<Any>>(), Trigger(DoubleTime(20.0)))

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertNotSame(source.timeDistribution, clone.timeDistribution)
        assertEquals(DoubleTime(20.0), clone.nextOccurrence.current)
    }

    @Test
    fun `a non-memoryless clone does not inherit source sampler state`() {
        val sourceDistribution = SequenceDistribution(1.0, 100.0)
        val source = GenericReaction(mockk(), sourceDistribution)
        source.initializationComplete(Time.ZERO, environment)
        source.updateAfterFiring(Time.ZERO)

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)
        source.updateAfterFiring(DoubleTime(1.0))

        assertNotSame(sourceDistribution, clone.timeDistribution)
        assertEquals(DoubleTime(101.0), source.nextOccurrence.current)
        assertEquals(DoubleTime(11.0), clone.nextOccurrence.current)
    }

    @Test
    fun `a chemical reaction rejects non-memoryless distributions`() {
        assertFailsWith<IllegalArgumentException> {
            ChemicalNodeReaction(mockk<Node<Any>>(), SequenceDistribution(3.0, 100.0))
        }
    }

    @Test
    fun `a molecule-controlled clone reads the destination node`() {
        val molecule = mockk<Molecule>()
        val sourceNode = mockk<Node<Any>>()
        val destinationNode = mockk<Node<Any>>()
        every { sourceNode.getConcentration(molecule) } returns 1.0
        every { destinationNode.getConcentration(molecule) } returns 4.0
        val sourceDistribution = MoleculeControlledTimeDistribution(
            mockk<Incarnation<Any, *>>(),
            sourceNode,
            molecule,
        )
        val source = GenericReaction(sourceNode, sourceDistribution)

        val clone = source.cloneOnNewNode(destinationNode, DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        val cloneDistribution = assertIs<MoleculeControlledTimeDistribution<*>>(clone.timeDistribution)
        assertSame(destinationNode, cloneDistribution.node)
        assertEquals(DoubleTime(14.0), clone.nextOccurrence.current)
    }

    @Test
    fun `RNG-backed generators are distinct while consuming the shared simulation RNG`() {
        val rng = Well19937c(0)
        val source = GenericReaction(mockk<Node<Any>>(), ExponentialTime(2.0, rng))

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertNotSame(source.timeDistribution, clone.timeDistribution)
        assertIs<ExponentialTime<*>>(clone.timeDistribution)
        assertTrue(clone.nextOccurrence.current > DoubleTime(10.0))
    }

    @Test
    fun `a chemical reaction clone draws one fresh occurrence without inheriting source state`() {
        val node = mockk<Node<Any>>()
        val environment = mockk<Environment<Any, *>>(relaxed = true)
        every { environment.simulationOrNull } returns null
        val randomGenerator = mockk<RandomGenerator>()
        var samples = 0
        every { randomGenerator.nextDouble() } answers {
            samples++
            0.5
        }
        val source = ChemicalNodeReaction(node, ExponentialTime(1.0, randomGenerator))
        source.initializationComplete(Time.ZERO, environment)
        val sourceOccurrence = source.nextOccurrence.current
        source.updateAfterFiring(sourceOccurrence)
        val sourceRescheduled = source.nextOccurrence.current

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertEquals(3, samples)
        assertTrue(clone.nextOccurrence.current > DoubleTime(10.0))
        assertNotSame(source.timeDistribution, clone.timeDistribution)
        assertTrue(sourceRescheduled != clone.nextOccurrence.current)
    }

    @Test
    fun `a non-memoryless built-in generator is reconstructed with its configured law`() {
        val source = GenericReaction(mockk<Node<Any>>(), WeibullTime(2.0, 0.5, Well19937c(0)))

        val clone = source.cloneOnNewNode(mockk(), DoubleTime(10.0))
        clone.initializationComplete(DoubleTime(10.0), environment)

        assertNotSame(source.timeDistribution, clone.timeDistribution)
        assertIs<WeibullTime<*>>(clone.timeDistribution)
        assertTrue(clone.nextOccurrence.current > DoubleTime(10.0))
    }

    @Test
    fun `specialized randomized generators preserve their concrete type`() {
        val node = mockk<Node<Any>>()
        val distributions: List<TimeDistribution<Any>> = listOf(
            RandomDiracComb(Well19937c(0), 1.0, 2.0),
            WeibullDistributedWeibullTime(2.0, 0.5, 0.2, Well19937c(0)),
        )

        distributions.forEach { distribution ->
            val clone = GenericReaction(node, distribution).cloneOnNewNode(mockk(), DoubleTime(10.0))
            clone.initializationComplete(DoubleTime(10.0), environment)

            assertNotSame(distribution, clone.timeDistribution)
            assertEquals(distribution::class, clone.timeDistribution::class)
        }
    }

    @Test
    fun `an opaque Apache distribution reports that it cannot reconstruct itself`() {
        val source = GenericReaction(mockk<Node<Any>>(), AnyRealDistribution(DiracDeltaDistribution(1.0)))

        val error = assertFailsWith<IllegalStateException> {
            source.cloneOnNewNode(mockk(), Time.ZERO)
        }

        assertTrue(error.message.orEmpty().contains("override newInstanceOn"))
    }

    private class SequenceDistribution(vararg samples: Double) : TimeDistribution<Any> {
        private val configuredSamples = samples.copyOf()
        private val iterator = configuredSamples.iterator()

        override fun sample(): Time = DoubleTime(iterator.nextDouble())

        override fun newInstanceOn(node: Node<Any>): TimeDistribution<Any> = SequenceDistribution(*configuredSamples)
    }
}
