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
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.sapere.reactions.SAPERENodeReaction
import it.unibo.alchemist.model.sapere.timedistributions.SAPEREExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.MersenneTwister

class LsaAscendingGradientDistTest {
    private val environment = Continuous2DEnvironment(SAPEREIncarnation<Euclidean2DPosition>()).apply {
        networkModel = connectWithinDistance(5.0)
    }
    private val randomGenerator = MersenneTwister(1)

    private fun node(x: Double, y: Double) = LsaNode(environment).also {
        environment.addNode(it, environment.makePosition(x, y))
    }

    @Test
    fun `a node carrying the action can be removed from the environment`() {
        val agent = node(0.0, 0.0)
        val neighbor = node(1.0, 0.0)
        LsaAscendingGradientDist(
            environment,
            SAPERENodeReaction(environment, agent, randomGenerator, SAPEREExponentialTime("1", randomGenerator)),
        )
        environment.removeNode(agent)
        assertEquals(listOf(neighbor), environment.nodes.current)
    }
}
