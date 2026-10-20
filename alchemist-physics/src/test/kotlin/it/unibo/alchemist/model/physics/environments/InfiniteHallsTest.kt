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
import it.unibo.alchemist.model.networks.NoLinks
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InfiniteHallsTest {
    private val environment = infiniteHalls()

    /**
     * A coordinate of the external corridor.
     */
    private val corridor = environment.externalCorridor.middle

    /**
     * A coordinate of the main room, after the central corridor.
     */
    private val farRoom = between(environment.centralCorridor.endInclusive, environment.mainRoom.endInclusive)

    /**
     * A coordinate of the central corridor.
     */
    private val lane = environment.centralCorridor.middle

    private val node = GenericNode(environment)

    /**
     * A coordinate of the wall between the main room and the external corridor.
     */
    private val outerWall = between(environment.mainRoom.endInclusive, environment.externalBorder)

    /**
     * A coordinate of the main room, before the central corridor.
     */
    private val room = between(environment.mainRoom.start, environment.centralCorridor.start)

    /**
     * A coordinate of the wall between the hall start and the main room.
     */
    private val wall = between(0.0, environment.mainRoom.start)

    @Test
    fun `closing the doors walls off the horizontal lane`() {
        assertTrue(environment.accepts(wall, lane))
        assertFalse(infiniteHalls(openDoors = false).accepts(wall, lane))
    }

    @Test
    fun `nodes cannot be placed in walls`() {
        environment.addNode(node, environment.makePosition(wall, wall))
        assertEquals(0, environment.nodeCount.current)
    }

    @Test
    fun `nodes moving from the horizontal lane are stopped at its border`() {
        environment.addNode(node, environment.makePosition(wall, lane))
        environment.moveNodeTo(node, environment.makePosition(wall, room))
        assertNodeAt(wall, environment.centralCorridor.start)
    }

    @Test
    fun `nodes moving from the right corridor are stopped at its border`() {
        environment.addNode(node, environment.makePosition(corridor, room))
        environment.moveNodeTo(node, environment.makePosition(outerWall, room))
        assertNodeAt(environment.externalBorder, room)
    }

    @Test
    fun `nodes moving from the right corridor of a hall at negative coordinates are stopped at its border`() {
        val hallSize = environment.hallSize
        environment.addNode(node, environment.makePosition(corridor - hallSize, room))
        environment.moveNodeTo(node, environment.makePosition(outerWall - hallSize, room))
        assertNodeAt(environment.externalBorder - hallSize, room)
    }

    @Test
    fun `nodes moving from the room are stopped at the room border`() {
        environment.addNode(node, environment.makePosition(room, room))
        environment.moveNodeTo(node, environment.makePosition(room, outerWall))
        assertNodeAt(room, environment.mainRoom.endInclusive)
    }

    @Test
    fun `nodes moving from the vertical lane are stopped at its border`() {
        environment.addNode(node, environment.makePosition(lane, wall))
        environment.moveNodeTo(node, environment.makePosition(room, wall))
        assertNodeAt(environment.centralCorridor.start, wall)
    }

    @Test
    fun `nodes reach allowed destinations`() {
        environment.addNode(node, environment.makePosition(room, room))
        environment.moveNodeTo(node, environment.makePosition(farRoom, farRoom))
        assertNodeAt(farRoom, farRoom)
    }

    @Test
    fun `positions are allowed in the room, lanes, and corridors, but not in the walls`() {
        assertTrue(infiniteHalls().accepts(room, room))
        assertTrue(infiniteHalls().accepts(lane, wall))
        assertTrue(infiniteHalls().accepts(corridor, wall))
        assertFalse(infiniteHalls().accepts(room, outerWall))
        assertFalse(infiniteHalls().accepts(wall, wall))
    }

    private fun assertNodeAt(x: Double, y: Double) {
        val position = environment.currentPositionOf(node)
        assertEquals(x, position.x, TOLERANCE)
        assertEquals(y, position.y, TOLERANCE)
    }

    private companion object {
        private const val TOLERANCE = 1e-9

        private val ClosedFloatingPointRange<Double>.middle: Double get() = (start + endInclusive) / 2

        private fun between(first: Double, second: Double): Double = (first + second) / 2

        /**
         * Whether a new node can be placed at ([x], [y]) in this environment.
         */
        private fun InfiniteHalls<Any>.accepts(x: Double, y: Double): Boolean {
            val before = nodeCount.current
            addNode(GenericNode(this), makePosition(x, y))
            return nodeCount.current > before
        }

        private fun infiniteHalls(openDoors: Boolean = true) = InfiniteHalls(
            SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow(),
            openDoors = openDoors,
        ).apply { networkModel = NoLinks() }
    }
}
