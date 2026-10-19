/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.obstacles.RectObstacle2D
import it.unibo.alchemist.model.physics.properties.CircularArea
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.nextDown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Continuous2DObstaclesTest {
    private val environment =
        Continuous2DObstacles(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private val obstacle = RectObstacle2D<Euclidean2DPosition>(1.0, 1.0, 1.0, 1.0)

    @Test
    fun `obstacles cannot be added once the environment holds nodes`() {
        addNode()
        assertFailsWith<IllegalStateException> { environment.addObstacle(obstacle) }
        assertEquals(emptyList(), environment.obstacles)
    }

    @Test
    fun `obstacles cannot be removed once the environment holds nodes`() {
        environment.addObstacle(obstacle)
        addNode()
        assertFailsWith<IllegalStateException> { environment.removeObstacle(obstacle) }
        assertEquals(listOf(obstacle), environment.obstacles)
    }

    @Test
    fun `moving nodes stop at the first of the obstacles and the other nodes on their path`() {
        environment.addObstacle(RectObstacle2D(3.0, -1.0, 1.0, 6.0))
        // A node in front of the obstacle stops the movement first.
        val blocked = circleAt(0, 0)
        val blocker = circleAt(2, 0)
        environment.moveNodeTo(blocked, environment.makePosition(6, 0))
        val hitboxes = environment.getShape(blocked).radius + environment.getShape(blocker).radius
        assertEquals(hitboxes, environment.distanceBetween(blocked, blocker), absoluteTolerance = TOLERANCE)
        // The obstacle in front of a node stops the movement first.
        val walled = circleAt(0, 3)
        circleAt(5, 3)
        environment.moveNodeTo(walled, environment.makePosition(6, 3))
        assertEquals(environment.makePosition(3.0.nextDown(), 3), environment.currentPositionOf(walled))
    }

    private fun circleAt(x: Number, y: Number) = GenericNode(environment).also {
        it.addProperty(CircularArea(environment, it, RADIUS))
        environment.addNode(it, environment.makePosition(x, y))
    }

    private fun addNode() {
        environment.addNode(GenericNode(environment), environment.makePosition(0, 0))
    }

    private companion object {
        const val RADIUS = 0.5
        const val TOLERANCE = 1e-9
    }
}
