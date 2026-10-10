/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.actions

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.reactions.SAPERENodeReaction
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.MersenneTwister

class LsaAscendingAgentTest {
    private val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>()).apply {
        linkingRule = ConnectWithinDistance(5.0)
    }
    private val randomGenerator = MersenneTwister(1)
    private val agent = node(0.0, 0.0)
    private val action = LsaAscendingAgent(
        SAPERENodeReaction(environment, agent, randomGenerator, SAPEREExponentialTime("1", randomGenerator)),
        environment,
        LsaMolecule("grad, V"),
        1,
    )

    private fun node(x: Double, y: Double, vararg lsas: String) = LsaNode(environment).also { node ->
        lsas.forEach { node.setConcentration(LsaMolecule(it)) }
        environment.addNode(node, environment.makePosition(x, y))
    }

    private fun assertAgentAt(x: Double, y: Double) {
        action.execute()
        assertEquals(environment.makePosition(x, y), environment.getCurrentPosition(agent))
    }

    @Test
    fun `the agent steps towards the lowest gradient by at most the limit along each axis`() {
        node(1.0, 0.05, "grad, 1")
        node(-1.0, -1.0, "grad, 5", "grad, 2")
        assertAgentAt(0.1, 0.05)
    }

    @Test
    fun `the agent does not move towards an active neighbor`() {
        node(1.0, 1.0, "grad, 1", "active")
        node(-1.0, -1.0, "grad, 5")
        assertAgentAt(0.0, 0.0)
    }

    @Test
    fun `the agent stays still without gradients to follow`() {
        node(1.0, 1.0, "other, 1")
        assertAgentAt(0.0, 0.0)
    }
}
