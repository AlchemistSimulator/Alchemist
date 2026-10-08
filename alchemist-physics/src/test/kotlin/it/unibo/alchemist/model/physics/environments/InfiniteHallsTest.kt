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
import it.unibo.alchemist.model.linkingrules.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InfiniteHallsTest {
    private val environment =
        InfiniteHalls(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()).apply {
            linkingRule = NoLinks()
        }
    private val node = GenericNode(environment)

    @Test
    fun `positions are allowed in the room, lanes, and corridors, but not in the walls`() {
        assertTrue(environment.allowed(2.0, 2.0))
        assertTrue(environment.allowed(4.5, 0.5))
        assertTrue(environment.allowed(9.0, 0.5))
        assertFalse(environment.allowed(2.0, 8.5))
        assertFalse(environment.allowed(0.5, 0.5))
    }

    @Test
    fun `nodes cannot be placed in walls`() {
        environment.addNode(node, Euclidean2DPosition(0.5, 0.5))
        assertEquals(0, environment.nodeCount.current)
    }

    @Test
    fun `nodes reach allowed destinations`() {
        environment.addNode(node, Euclidean2DPosition(2.0, 2.0))
        environment.moveNodeTo(node, Euclidean2DPosition(3.0, 3.0))
        assertEquals(Euclidean2DPosition(3.0, 3.0), environment.getCurrentPosition(node))
    }

    @Test
    fun `nodes moving from the room are stopped at the room border`() {
        environment.addNode(node, Euclidean2DPosition(2.0, 2.0))
        environment.moveNodeTo(node, Euclidean2DPosition(2.0, 8.5))
        assertEquals(Euclidean2DPosition(2.0, environment.sf), environment.getCurrentPosition(node))
    }
}
