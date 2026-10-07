/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NoOtherReactionCanExecuteTest {

    @Test
    fun `reactions added after the condition can make it invalid`() {
        val environment =
            Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(0, 0))
        val guarded = GenericReaction(node, DiracComb(1.0))
        val condition = NoOtherReactionCanExecute(guarded)
        guarded.conditions = listOf(condition)
        node.addReaction(guarded)
        val validity = condition.isValid
        val subscription = validity.subscribe { }
        assertTrue(validity.current)
        val competitor = GenericReaction(node, DiracComb(1.0)).apply {
            conditions = listOf(ContainsMolecule(this, MOLECULE))
        }
        node.addReaction(competitor)
        assertTrue(validity.current)
        node.setConcentration(MOLECULE, 1)
        assertFalse(validity.current)
        node.removeReaction(competitor)
        assertTrue(validity.current)
        subscription.dispose()
    }

    private companion object {
        val MOLECULE = SimpleMolecule("token")
    }
}
