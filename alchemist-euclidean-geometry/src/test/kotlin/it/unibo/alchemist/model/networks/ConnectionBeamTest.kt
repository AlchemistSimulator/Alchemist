/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.obstacles.RectObstacle2D
import it.unibo.alchemist.model.physics.environments.Continuous2DObstacles
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionBeamTest {
    private val environment =
        Continuous2DObstacles(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    init {
        environment.networkModel = with(environment) { connectionBeam(radius = 10.0, beamWidth = 0.5) }
    }

    @Test
    fun `nodes are linked through a gap of a wall within the beam`() {
        environment.addObstacle(RectObstacle2D(1.9, -0.1, 0.2, 5.0))
        val first = nodeAt(0, 0)
        val second = nodeAt(4, 0)
        assertEquals(listOf(second), neighborsOf(first))
    }

    @Test
    fun `nodes are not linked through a closed wall`() {
        environment.addObstacle(RectObstacle2D(1.9, -0.1, 0.2, 5.0))
        environment.addObstacle(RectObstacle2D(1.9, -5.0, 0.2, 4.9))
        val first = nodeAt(0, 0)
        nodeAt(4, 0)
        assertEquals(emptyList(), neighborsOf(first))
    }

    private fun neighborsOf(node: Node<Any>): List<Node<Any>> =
        environment.neighborhoodOf(node).current.neighbors.toList()

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }
}
