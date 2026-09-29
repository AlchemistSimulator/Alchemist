/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.reactions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.conditions.BiomolPresentInCell
import it.unibo.alchemist.model.biochemistry.conditions.GenericMoleculePresent
import it.unibo.alchemist.model.biochemistry.conditions.TensionPresent
import it.unibo.alchemist.model.biochemistry.conditions.TensionPresent.MechanicalState
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class BiochemicalReactionSchedulingTest {

    @Test
    fun `cloning preserves biochemical condition type and destination node`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        val incarnation = BiochemistryIncarnation()
        val environment = BioRect2DEnvironment(incarnation)
        val source = incarnation.createNode(randomGenerator, environment, null)
        val destination = incarnation.createNode(randomGenerator, environment, null)
        val molecule = incarnation.createMolecule("token")
        destination.setConcentration(molecule, 2.0)
        val condition = BiomolPresentInCell(source, molecule, 1.0)
        val reaction = BiochemicalNodeReaction(
            source,
            ExponentialTime(1.0, randomGenerator),
            environment,
            randomGenerator,
        ).apply {
            conditions = listOf(condition)
        }
        val clonedReaction = reaction.cloneOnNewNode(destination, Time.ZERO)
        val clonedCondition = assertIs<BiomolPresentInCell>(clonedReaction.conditions.single())
        assertSame(destination, clonedCondition.getNode())
        assertEquals(1.0, clonedCondition.requiredQuantity)
        assertEquals(2.0, clonedCondition.quantity.current)
        assertFailsWith<IllegalArgumentException> {
            reaction.conditions = listOf(mockk<Condition<Double>>())
        }
    }

    @Test
    fun `a quantity change updates rate while condition validity remains true`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        every { randomGenerator.nextDouble() } returns 0.5
        val fixture = createFixture(randomGenerator)
        val (environment, node, molecule, condition, reaction) = fixture
        reaction.initializationComplete(Time.ZERO, environment)
        val initialOccurrence = reaction.nextOccurrence.current
        verify(exactly = 1) { randomGenerator.nextDouble() }
        node.setConcentration(molecule, 2.0)
        assertEquals(2.0, condition.quantity.current)
        assertTrue(reaction.canExecute.current)
        assertEquals(initialOccurrence * 0.5, reaction.nextOccurrence.current)
        verify(exactly = 1) { randomGenerator.nextDouble() }
    }

    @Test
    fun `mass-action quantities must be non-negative integer counts`() {
        listOf(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            -1.0,
            1.5,
            Int.MAX_VALUE.toDouble() + 1.0,
        ).forEach { invalidQuantity ->
            val randomGenerator = mockk<RandomGenerator>(relaxed = true)
            val (environment, node, molecule, _, reaction) = createFixture(randomGenerator)
            reaction.initializationComplete(Time.ZERO, environment)
            assertFailsWith<IllegalArgumentException> { node.setConcentration(molecule, invalidQuantity) }
        }
    }

    @Test
    fun `fractional stoichiometry is rejected when configuring mass action`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        val (_, node, molecule, _, reaction) = createFixture(randomGenerator)
        assertFailsWith<IllegalArgumentException> {
            reaction.conditions = listOf(GenericMoleculePresent(node, molecule, 1.5))
        }
    }

    @Test
    fun `a malformed later factor is validated after a zero factor`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        val incarnation = BiochemistryIncarnation()
        val environment = BioRect2DEnvironment(incarnation)
        val node = incarnation.createNode(randomGenerator, environment, null)
        val malformedMolecule = incarnation.createMolecule("malformed")
        node.setConcentration(malformedMolecule, 1.0)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        val reaction = BiochemicalNodeReaction(
            node,
            ExponentialTime(1.0, randomGenerator),
            environment,
            randomGenerator,
        ).apply {
            conditions = listOf(
                mockk<TensionPresent>(relaxed = true).also {
                    every { it.observeMechanicalState() } returns MutableObservable.observe(MechanicalState(true, 0.0))
                    every { it.isValid() } returns MutableObservable.observe(true)
                    every { it.getTension() } returns 0.0
                },
                GenericMoleculePresent(node, malformedMolecule, 1.0),
            )
        }
        reaction.initializationComplete(Time.ZERO, environment)
        assertFailsWith<IllegalArgumentException> { node.setConcentration(malformedMolecule, Double.NaN) }
    }

    @Test
    fun `zero rate factors absorb infinity in either order without drawing`() {
        listOf(listOf(0.0, Double.POSITIVE_INFINITY), listOf(Double.POSITIVE_INFINITY, 0.0)).forEach { factors ->
            val randomGenerator = mockk<RandomGenerator>(relaxed = true)
            val incarnation = BiochemistryIncarnation()
            val environment = BioRect2DEnvironment(incarnation)
            val node = incarnation.createNode(randomGenerator, environment, null)
            val reaction =
                BiochemicalNodeReaction(node, ExponentialTime(1.0, randomGenerator), environment, randomGenerator)
            reaction.conditions = factors.map { factor ->
                mockk<TensionPresent>().also {
                    every { it.observeMechanicalState() } returns
                        MutableObservable.observe(MechanicalState(true, factor))
                    every { it.isValid() } returns MutableObservable.observe(true)
                    every { it.getTension() } returns factor
                }
            }
            reaction.initializationComplete(Time.ZERO, environment)
            assertEquals(0.0, reaction.rate)
            assertTrue(reaction.nextOccurrence.current.isInfinite)
            verify(exactly = 0) { randomGenerator.nextDouble() }
        }
    }

    private fun createFixture(randomGenerator: RandomGenerator): BiochemicalFixture {
        val incarnation = BiochemistryIncarnation()
        val environment = BioRect2DEnvironment(incarnation)
        val node = incarnation.createNode(randomGenerator, environment, null)
        val molecule = incarnation.createMolecule("token")
        node.setConcentration(molecule, 1.0)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        val condition = GenericMoleculePresent(node, molecule, 1.0)
        val reaction = BiochemicalNodeReaction(
            node,
            ExponentialTime(1.0, randomGenerator),
            environment,
            randomGenerator,
        ).apply { conditions = listOf(condition) }
        return BiochemicalFixture(environment, node, molecule, condition, reaction)
    }

    private data class BiochemicalFixture(
        val environment: Environment<Double, *>,
        val node: Node<Double>,
        val molecule: Molecule,
        val condition: GenericMoleculePresent<Double>,
        val reaction: BiochemicalNodeReaction,
    )
}
