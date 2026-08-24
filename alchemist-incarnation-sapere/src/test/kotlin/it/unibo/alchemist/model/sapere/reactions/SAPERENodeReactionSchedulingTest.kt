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
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertTrue
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class SAPERENodeReactionSchedulingTest {

    @Test
    fun `initialization and firing draw independent exponential delays`() {
        val rng = mockk<RandomGenerator>(relaxed = true)
        every { rng.nextDouble() } returns 0.5
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val node = LsaNode(environment)
        assertTrue(environment.addNode(node, Euclidean2DPosition(0.0, 0.0)))
        val reaction = SAPERENodeReaction(environment, node, rng, SAPEREExponentialTime("2", rng))

        reaction.initializationComplete(DoubleTime(0.0), environment)
        assertTrue(reaction.nextOccurrence.current.isFinite)
        assertTrue(reaction.nextOccurrence.current > DoubleTime(0.0))
        verify(exactly = 1) { rng.nextDouble() }

        reaction.updateSchedulingAfterFiring(reaction.nextOccurrence.current)
        verify(exactly = 2) { rng.nextDouble() }
    }

    @Test
    fun `locality follows installed action contexts`() {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val node = LsaNode(environment)
        val rng = mockk<RandomGenerator>(relaxed = true)
        val reaction = SAPERENodeReaction(environment, node, rng, SAPEREExponentialTime("1", rng))
        assertTrue(reaction.modifiesOnlyLocally())
        val local = mockk<Action<List<ILsaMolecule>>>()
        every { local.getContext() } returns Context.LOCAL
        reaction.setActions(listOf(local))
        assertTrue(reaction.modifiesOnlyLocally())
        val neighbor = mockk<Action<List<ILsaMolecule>>>()
        every { neighbor.getContext() } returns Context.NEIGHBORHOOD
        reaction.setActions(listOf(neighbor))
        assertTrue(!reaction.modifiesOnlyLocally())
    }
}
