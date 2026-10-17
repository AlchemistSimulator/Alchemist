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
import it.unibo.alchemist.model.positions.Euclidean2DPosition
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

    private fun addNode() {
        environment.addNode(GenericNode(environment), environment.makePosition(0, 0))
    }
}
