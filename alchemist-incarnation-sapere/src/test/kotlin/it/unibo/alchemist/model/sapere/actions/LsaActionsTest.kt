/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.ILsaNode
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.ConstTreeNode
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.reactions.SAPERENodeReaction
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister
import org.danilopianini.lang.HashString

class LsaActionsTest {
    private val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
    private val environment = Continuous2DEnvironment(incarnation)
    private val randomGenerator = MersenneTwister(1)

    private fun reactionOn(node: LsaNode) =
        SAPERENodeReaction(environment, node, randomGenerator, SAPEREExponentialTime("1", randomGenerator))

    @Test
    fun `a cloned all-neighbors action still injects in every neighbor`() {
        val source = LsaNode(environment)
        val destination = LsaNode(environment)
        val neighbors: List<ILsaNode> = listOf(LsaNode(environment), LsaNode(environment))
        val reaction = incarnation.createReaction(
            randomGenerator,
            environment,
            source,
            SAPEREExponentialTime("1", randomGenerator),
            "--> *{aaa}",
        )
        val clonedReaction = reaction.cloneOnNewNode(destination, Time.ZERO)
        val clone = assertIs<LsaAllNeighborsAction>(clonedReaction.actions.single())
        assertSame(clonedReaction, clone.reaction)
        clone.setExecutionContext(HashMap(), neighbors.toMutableList())
        clone.execute()
        neighbors.forEach { assertEquals(1, it.getConcentration(LsaMolecule("aaa")).size) }
        assertTrue(destination.lsaSpace.isEmpty())
    }

    @Test
    fun `changing argument picks a target other than the old one`() {
        val node = LsaNode(environment)
        val action = LsaChangeArgument(reactionOn(node), arrayOf("a", "b"), "Target", randomGenerator)
        val matches: MutableMap<HashString, ITreeNode<*>> = hashMapOf(HashString("OldType") to oldType("a"))
        action.setExecutionContext(matches, mutableListOf())
        action.execute()
        assertEquals("b", matches[HashString("Target")].toString())
    }

    private fun oldType(type: String): ITreeNode<*> = ConstTreeNode(HashString(type))
}
