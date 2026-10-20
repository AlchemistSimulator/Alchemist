/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * An environment made of an infinite grid of halls, each one with a room connected to corridors through doors.
 *
 * @param T concentration type
 * @param incarnation the incarnation to be used.
 * @param size the size of a single hall.
 * @param allOpen whether all the doors are initially open.
 */
class InfiniteHalls<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    size: Double = DEFAULT_SIZE,
    allOpen: Boolean = true,
) : AbstractLimitedContinuous2D<T>(incarnation) {
    /**
     * The size of a single hall.
     */
    val hallSize: Double = size

    /**
     * The final margin of the central corridor.
     */
    val cf: Double = size * CORRIDOR_END

    /**
     * The initial margin of the central corridor.
     */
    val ci: Double = size * CORRIDOR_START

    /**
     * The external border margin.
     */
    val ex: Double = size * EXTERNAL_BORDER

    /**
     * The final margin of the room.
     */
    val sf: Double = size * ROOM_END

    /**
     * The initial margin of the room.
     */
    val si: Double = size * ROOM_START

    /**
     * Whether the doors of the rooms are open: if true, the room has four open accesses, otherwise only two.
     */
    var isDoorsOpen: Boolean = allOpen

    /**
     * Checks whether the position at ([xp], [yp]) is allowed.
     */
    fun allowed(xp: Double, yp: Double): Boolean {
        val x = xp.wrapped()
        val y = yp.wrapped()
        return x >= ex || // Right corridor
            y >= ex || // Upper corridor
            x in ci..cf || // Vertical lane
            isDoorsOpen && y in ci..cf || // Horizontal lane
            x in si..sf && y in si..sf // Room
    }

    /**
     * The farthest position reachable by the node towards the desired one, stopping at the walls of the halls.
     */
    override fun farthestReachablePosition(node: Node<T>, desired: Euclidean2DPosition): Euclidean2DPosition {
        val current = currentPositionOf(node)
        val reachable = super.farthestReachablePosition(node, desired)
        return next(current.x, current.y, reachable.x, reachable.y)
    }

    override fun isAllowed(position: Euclidean2DPosition): Boolean = allowed(position.x, position.y)

    private fun next(ox: Double, oy: Double, nx: Double, ny: Double): Euclidean2DPosition {
        if (allowed(nx, ny)) {
            return Euclidean2DPosition(nx, ny)
        }
        val hallX = (ox / hallSize).toInt() * hallSize
        val hallY = (oy / hallSize).toInt() * hallSize
        val oxm = ox % hallSize
        val oym = oy % hallSize
        val x = nx.wrapped()
        val y = ny.wrapped()
        return when {
            // Right corridor
            oxm >= ex -> Euclidean2DPosition(nx.stoppedAt(x, hallX, ex, hallSize), ny)
            // Upper corridor
            oym >= ex -> Euclidean2DPosition(nx, ny.stoppedAt(y, hallY, ex, hallSize))
            // Main room
            oxm in si..sf && oym in si..sf ->
                Euclidean2DPosition(nx.stoppedAt(x, hallX, si, sf), ny.stoppedAt(y, hallY, si, sf))
            // Horizontal lane passes (room positions and corridors already checked)
            isDoorsOpen && oym in ci..cf -> Euclidean2DPosition(nx, ny.stoppedAt(y, hallY, ci, cf))
            // Vertical lane passes (room positions and corridors already checked)
            oxm in ci..cf -> Euclidean2DPosition(nx.stoppedAt(x, hallX, ci, cf), ny)
            else -> Euclidean2DPosition(nx, ny)
        }
    }

    /**
     * This coordinate, or the closest wall of the hall starting at [hallStart] if its position [inHall]
     * falls outside the [min]..[max] passage.
     */
    private fun Double.stoppedAt(inHall: Double, hallStart: Double, min: Double, max: Double): Double = when {
        inHall < min -> hallStart + min
        inHall > max -> hallStart + max
        else -> this
    }

    /**
     * This coordinate, relative to the hall containing it.
     */
    private fun Double.wrapped(): Double = (this % hallSize + hallSize) % hallSize

    private companion object {
        private const val CORRIDOR_END = 0.51
        private const val CORRIDOR_START = 0.39
        private const val DEFAULT_SIZE = 10.0
        private const val EXTERNAL_BORDER = 0.89
        private const val ROOM_END = 0.81
        private const val ROOM_START = 0.09
    }
}
