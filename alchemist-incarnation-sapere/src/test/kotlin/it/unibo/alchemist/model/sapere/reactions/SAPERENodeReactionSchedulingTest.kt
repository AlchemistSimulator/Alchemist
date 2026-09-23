/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.reactions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.conditions.LsaStandardCondition
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertTrue
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class SAPERENodeReactionSchedulingTest {

    @Test
    fun `initialization and firing draw independent exponential delays`() {
        val (rng, environment, _, reaction) = fixture()

        reaction.initializationComplete(DoubleTime(0.0), environment)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        assertTrue(reaction.nextOccurrence.current > DoubleTime(0.0))
        verify(exactly = 1) { rng.nextDouble() }

        reaction.execute()
        verify(exactly = 2) { rng.nextDouble() }
    }

    @Test
    fun `a new match reschedules even while condition validity remains true`() {
        val (rng, environment, node, reaction) = fixture()
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token"), node))
        reaction.initializationComplete(DoubleTime(0.0), environment)
        assertTrue(reaction.nextOccurrence.current.isInfinite)
        verify(exactly = 0) { rng.nextDouble() }

        node.setConcentration(LsaMolecule("token"))
        assertTrue(reaction.canExecute.current)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        verify(exactly = 1) { rng.nextDouble() }

        node.setConcentration(LsaMolecule("token"))
        assertTrue(reaction.canExecute.current)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        verify(exactly = 2) { rng.nextDouble() }
    }

    private fun fixture(): Fixture {
        val rng = mockk<RandomGenerator>(relaxed = true)
        every { rng.nextDouble() } returns 0.5
        val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>())
        val node = LsaNode(environment)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        return Fixture(
            rng,
            environment,
            node,
            SAPERENodeReaction(environment, node, rng, SAPEREExponentialTime("2", rng)),
        )
    }

    private data class Fixture(
        val randomGenerator: RandomGenerator,
        val environment: Continuous2DEnvironment<List<ILsaMolecule>>,
        val node: LsaNode,
        val reaction: SAPERENodeReaction,
    )
}
