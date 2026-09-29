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
import it.unibo.alchemist.model.linkingrules.ClosestN
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.conditions.LsaStandardCondition
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertFailsWith
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

    @Test
    fun `invalid static propensities are rejected before sampling`() {
        val (rng, environment, _, reaction) = fixture("NaN")
        assertFailsWith<IllegalStateException> {
            reaction.initializationComplete(DoubleTime(0.0), environment)
        }
        verify(exactly = 0) { rng.nextDouble() }
    }

    @Test
    fun `invalid match propensities are rejected before sampling`() {
        listOf("0 - N" to 1, "N / N" to 0).forEach { (rate, value) ->
            val (rng, environment, node, reaction) = fixture(rate)
            reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token, N"), node))
            node.setConcentration(LsaMolecule("token, $value"))
            assertFailsWith<IllegalStateException> {
                reaction.initializationComplete(DoubleTime(0.0), environment)
            }
            verify(exactly = 0) { rng.nextDouble() }
        }
    }

    @Test
    fun `an unresolved match propensity is rejected at the reaction boundary`() {
        val (rng, environment, node, reaction) = fixture("Missing")
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token, N"), node))
        node.setConcentration(LsaMolecule("token, 1"))
        assertFailsWith<IllegalStateException> {
            reaction.initializationComplete(DoubleTime(0.0), environment)
        }
        verify(exactly = 0) { rng.nextDouble() }
    }

    @Test
    fun `neighbor state mutations reschedule a gradient`() {
        val rng = mockk<RandomGenerator>(relaxed = true)
        every { rng.nextDouble() } returns 0.5
        val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>())
        environment.linkingRule = ConnectWithinDistance(1.0)
        val node = LsaNode(environment)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        val reaction = SAPEREGradient(
            environment,
            node,
            LsaMolecule("source, Value"),
            LsaMolecule("gradient, Distance, #O"),
            1,
            "#D",
            null,
            100.0,
            SAPEREExponentialTime("2", rng),
        )
        reaction.initializationComplete(DoubleTime(0.0), environment)
        verify(exactly = 0) { rng.nextDouble() }
        val neighbor = LsaNode(environment)
        assertTrue(environment.addNode(neighbor, Euclidean2DPosition(0.5, 0.0)))
        assertTrue(environment.getNeighborhood(node).current.neighbors.contains(neighbor))
        verify(exactly = 2) { rng.nextDouble() }
        neighbor.setConcentration(LsaMolecule("gradient, 1, 1"))
        verify(exactly = 3) { rng.nextDouble() }
        environment.moveNodeToPosition(neighbor, Euclidean2DPosition(0.75, 0.0))
        verify(exactly = 4) { rng.nextDouble() }
        environment.moveNodeToPosition(neighbor, Euclidean2DPosition(2.0, 0.0))
        verify(exactly = 6) { rng.nextDouble() }
        environment.moveNodeToPosition(neighbor, Euclidean2DPosition(0.5, 0.0))
        verify(exactly = 7) { rng.nextDouble() }
        environment.removeNode(neighbor)
        verify(exactly = 8) { rng.nextDouble() }
        environment.linkingRule = ClosestN(1)
        val closest = LsaNode(environment)
        val replacement = LsaNode(environment)
        assertTrue(environment.addNode(closest, Euclidean2DPosition(0.5, 0.0)))
        verify(exactly = 9) { rng.nextDouble() }
        assertTrue(environment.addNode(replacement, Euclidean2DPosition(0.75, 0.0)))
        verify(exactly = 9) { rng.nextDouble() }
        environment.removeNode(closest)
        assertTrue(environment.getNeighborhood(node).current.neighbors.contains(replacement))
        verify(exactly = 10) { rng.nextDouble() }
    }

    private fun fixture(rate: String = "2"): Fixture {
        val rng = mockk<RandomGenerator>(relaxed = true)
        every { rng.nextDouble() } returns 0.5
        val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>())
        val node = LsaNode(environment)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        return Fixture(
            rng,
            environment,
            node,
            SAPERENodeReaction(environment, node, rng, SAPEREExponentialTime(rate, rng)),
        )
    }

    private data class Fixture(
        val randomGenerator: RandomGenerator,
        val environment: Continuous2DEnvironment<List<ILsaMolecule>>,
        val node: LsaNode,
        val reaction: SAPERENodeReaction,
    )
}
