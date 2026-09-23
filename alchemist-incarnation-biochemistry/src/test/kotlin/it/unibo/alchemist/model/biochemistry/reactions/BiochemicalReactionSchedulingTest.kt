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
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.conditions.GenericMoleculePresent
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class BiochemicalReactionSchedulingTest {

    @Test
    fun `a quantity change updates rate while condition validity remains true`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        every { randomGenerator.nextDouble() } returns 0.5
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
        ).apply {
            conditions = listOf(condition)
        }
        reaction.initializationComplete(Time.ZERO, environment)
        val initialOccurrence = reaction.nextOccurrence.current
        verify(exactly = 1) { randomGenerator.nextDouble() }

        node.setConcentration(molecule, 2.0)

        assertEquals(2.0, condition.quantity.current)
        assertTrue(reaction.canExecute.current)
        assertEquals(initialOccurrence * 0.5, reaction.nextOccurrence.current)
        verify(exactly = 1) { randomGenerator.nextDouble() }
    }
}
