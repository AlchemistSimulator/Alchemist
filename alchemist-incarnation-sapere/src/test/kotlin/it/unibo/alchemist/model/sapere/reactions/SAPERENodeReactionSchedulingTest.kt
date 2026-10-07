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
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
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
import kotlin.math.ln
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class SAPERENodeReactionSchedulingTest {

    @Test
    fun `initialization and firing draw independent exponential delays`() {
        val (randomGenerator, environment, _, reaction) = fixture()
        reaction.initializationComplete(DoubleTime(0.0), environment)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        assertTrue(reaction.nextOccurrence.current > DoubleTime(0.0))
        verify(exactly = 1) { randomGenerator.nextDouble() }
        reaction.execute()
        verify(exactly = 2) { randomGenerator.nextDouble() }
    }

    @Test
    fun `a new match reschedules even while condition validity remains true`() {
        val (randomGenerator, environment, node, reaction) = fixture()
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token"), reaction))
        reaction.initializationComplete(DoubleTime(0.0), environment)
        assertTrue(reaction.nextOccurrence.current.isInfinite)
        verify(exactly = 0) { randomGenerator.nextDouble() }
        node.setConcentration(LsaMolecule("token"))
        assertTrue(reaction.canExecute.current)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        verify(exactly = 1) { randomGenerator.nextDouble() }
        node.setConcentration(LsaMolecule("token"))
        assertTrue(reaction.canExecute.current)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        verify(exactly = 2) { randomGenerator.nextDouble() }
    }

    @Test
    fun `match revalidation redraws from the current simulation time`() {
        val (randomGenerator, environment, node, reaction) = fixture()
        var now: Time = Time.ZERO
        val simulation = mockk<Simulation<List<ILsaMolecule>, Euclidean2DPosition>>(relaxed = true)
        every { simulation.reactionInvalidated(any()) } answers {
            firstArg<Reaction<List<ILsaMolecule>>>().updateSchedulingAfterInvalidation(now)
        }
        environment.simulation = simulation
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token"), reaction))
        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(Time.INFINITY, reaction.nextOccurrence.current)
        verify(exactly = 0) { randomGenerator.nextDouble() }
        now = DoubleTime(5.0)
        node.setConcentration(LsaMolecule("token"))
        verify(exactly = 1) { randomGenerator.nextDouble() }
        assertEquals(5.0 + ln(2.0) / 2, reaction.nextOccurrence.current.toDouble(), TOLERANCE)
        now = DoubleTime(6.0)
        node.setConcentration(LsaMolecule("token"))
        verify(exactly = 2) { randomGenerator.nextDouble() }
        assertTrue(reaction.nextOccurrence.current > now)
    }

    @Test
    fun `a cloned reaction matches only on its destination and stops after disposal`() {
        val (randomGenerator, environment, source, reaction) = fixture()
        val destination = LsaNode(environment)
        assertTrue(environment.addNode(destination, Euclidean2DPosition(5.0, 0.0)))
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token"), reaction))
        reaction.initializationComplete(Time.ZERO, environment)
        val clone = reaction.cloneOnNewNode(destination, Time.ZERO)
        clone.initializationComplete(Time.ZERO, environment)
        assertTrue(reaction.nextOccurrence.current.isInfinite)
        assertTrue(clone.nextOccurrence.current.isInfinite)
        verify(exactly = 0) { randomGenerator.nextDouble() }
        source.setConcentration(LsaMolecule("token"))
        assertTrue(reaction.nextOccurrence.current.isFinite)
        assertTrue(clone.nextOccurrence.current.isInfinite)
        verify(exactly = 1) { randomGenerator.nextDouble() }
        destination.setConcentration(LsaMolecule("token"))
        assertTrue(clone.nextOccurrence.current.isFinite)
        verify(exactly = 2) { randomGenerator.nextDouble() }
        val cloneOccurrence = clone.nextOccurrence.current
        clone.dispose()
        destination.setConcentration(LsaMolecule("token"))
        assertEquals(cloneOccurrence, clone.nextOccurrence.current)
        verify(exactly = 2) { randomGenerator.nextDouble() }
        source.setConcentration(LsaMolecule("token"))
        verify(exactly = 3) { randomGenerator.nextDouble() }
    }

    @Test
    fun `invalid static propensities are rejected before sampling`() {
        val (randomGenerator, environment, _, reaction) = fixture("NaN")
        assertFailsWith<IllegalStateException> {
            reaction.initializationComplete(DoubleTime(0.0), environment)
        }
        verify(exactly = 0) { randomGenerator.nextDouble() }
    }

    @Test
    fun `invalid match propensities are rejected before sampling`() {
        listOf("0 - N" to 1, "N / N" to 0).forEach { (rate, value) ->
            val (rng, environment, node, reaction) = fixture(rate)
            reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token, N"), reaction))
            node.setConcentration(LsaMolecule("token, $value"))
            assertFailsWith<IllegalStateException> {
                reaction.initializationComplete(DoubleTime(0.0), environment)
            }
            verify(exactly = 0) { rng.nextDouble() }
        }
    }

    @Test
    fun `an unresolved match propensity is rejected at the reaction boundary`() {
        val (randomGenerator, environment, node, reaction) = fixture("Missing")
        reaction.conditions = listOf(LsaStandardCondition(LsaMolecule("token, N"), reaction))
        node.setConcentration(LsaMolecule("token, 1"))
        assertFailsWith<IllegalStateException> {
            reaction.initializationComplete(DoubleTime(0.0), environment)
        }
        verify(exactly = 0) { randomGenerator.nextDouble() }
    }

    @Test
    fun `neighbor state mutations reschedule a gradient`() {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        every { randomGenerator.nextDouble() } returns 0.5
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
            SAPEREExponentialTime("2", randomGenerator),
        )
        reaction.initializationComplete(DoubleTime(0.0), environment)
        verify(exactly = 0) { randomGenerator.nextDouble() }
        // Nodes outside the neighborhood are not observed at all.
        val distant = LsaNode(environment)
        assertTrue(environment.addNode(distant, Euclidean2DPosition(5.0, 0.0)))
        distant.setConcentration(LsaMolecule("gradient, 1, 1"))
        environment.removeNode(distant)
        verify(exactly = 0) { randomGenerator.nextDouble() }
        val neighbor = LsaNode(environment)
        assertTrue(environment.addNode(neighbor, Euclidean2DPosition(0.5, 0.0)))
        assertTrue(environment.getNeighborhood(node).current.neighbors.contains(neighbor))
        verify(exactly = 1) { randomGenerator.nextDouble() }
        neighbor.setConcentration(LsaMolecule("gradient, 1, 1"))
        verify(exactly = 2) { randomGenerator.nextDouble() }
        environment.moveNodeTo(neighbor, Euclidean2DPosition(0.75, 0.0))
        verify(exactly = 3) { randomGenerator.nextDouble() }
        environment.moveNodeTo(neighbor, Euclidean2DPosition(2.0, 0.0))
        verify(exactly = 5) { randomGenerator.nextDouble() }
        environment.moveNodeTo(neighbor, Euclidean2DPosition(0.5, 0.0))
        verify(exactly = 6) { randomGenerator.nextDouble() }
        environment.removeNode(neighbor)
        verify(exactly = 7) { randomGenerator.nextDouble() }
        environment.linkingRule = ClosestN(1)
        val closest = LsaNode(environment)
        val replacement = LsaNode(environment)
        assertTrue(environment.addNode(closest, Euclidean2DPosition(0.5, 0.0)))
        verify(exactly = 8) { randomGenerator.nextDouble() }
        assertTrue(environment.addNode(replacement, Euclidean2DPosition(0.75, 0.0)))
        verify(exactly = 8) { randomGenerator.nextDouble() }
        environment.removeNode(closest)
        assertTrue(environment.getNeighborhood(node).current.neighbors.contains(replacement))
        verify(exactly = 9) { randomGenerator.nextDouble() }
    }

    @Test
    fun `a match with infinite propensity is the one that fires`() {
        val (randomGenerator, environment, node) = fixture()
        // With rate 1/N, the match with N = 0 has infinite propensity, even if it is not the last match.
        node.setConcentration(LsaMolecule("token, 0"))
        node.setConcentration(LsaMolecule("token, 1"))
        val reaction = SAPEREIncarnation<Euclidean2DPosition>().createReaction(
            randomGenerator,
            environment,
            node,
            SAPEREExponentialTime("1 / N", randomGenerator),
            "{token, N} --> {picked, N}",
        )
        reaction.initializationComplete(Time.ZERO, environment)
        assertEquals(Double.POSITIVE_INFINITY, (reaction as SAPERENodeReaction).rate)
        reaction.execute()
        assertTrue(node.contains(LsaMolecule("picked, 0")))
        assertTrue(node.contains(LsaMolecule("token, 1")))
    }

    @Test
    fun `neighbor actions target the neighbors at firing time`() {
        val (randomGenerator, environment, node) = fixture()
        environment.linkingRule = ConnectWithinDistance(1.0)
        // As in the engine, invalidations are applied after the mutation completes, not while the reaction fires.
        environment.simulation = mockk<Simulation<List<ILsaMolecule>, Euclidean2DPosition>>(relaxed = true)
        node.setConcentration(LsaMolecule("token"))
        val reaction = SAPEREIncarnation<Euclidean2DPosition>().createReaction(
            randomGenerator,
            environment,
            node,
            SAPEREExponentialTime("1", randomGenerator),
            "{token} --> {token} +{msg}",
        )
        reaction.initializationComplete(Time.ZERO, environment)
        // The neighbor arrives after the last refresh, without changing the matches of the reaction.
        val neighbor = LsaNode(environment)
        assertTrue(environment.addNode(neighbor, Euclidean2DPosition(0.5, 0.0)))
        reaction.execute()
        assertTrue(neighbor.contains(LsaMolecule("msg")))
    }

    private fun fixture(rate: String = "2"): Fixture {
        val randomGenerator = mockk<RandomGenerator>(relaxed = true)
        every { randomGenerator.nextDouble() } returns 0.5
        val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>())
        val node = LsaNode(environment)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        return Fixture(
            randomGenerator,
            environment,
            node,
            SAPERENodeReaction(environment, node, randomGenerator, SAPEREExponentialTime(rate, randomGenerator)),
        )
    }

    private companion object {
        const val TOLERANCE = 1e-12
    }

    private data class Fixture(
        val randomGenerator: RandomGenerator,
        val environment: Continuous2DEnvironment<List<ILsaMolecule>>,
        val node: LsaNode,
        val reaction: SAPERENodeReaction,
    )
}
