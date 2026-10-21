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
import kotlin.math.floor

/**
 * An environment made of an infinite grid of halls, each one with a room connected to corridors through doors.
 *
 * @param T concentration type
 * @param incarnation the incarnation to be used.
 * @param hallSize the size of a single hall.
 * @param openDoors if true, every room has four open accesses, otherwise only two.
 */
class InfiniteHalls<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    internal val hallSize: Double = DEFAULT_SIZE,
    val openDoors: Boolean = true,
) : AbstractLimitedContinuous2D<T>(incarnation) {
    /**
     * The central corridors, crossing each hall horizontally and vertically.
     */
    internal val centralCorridor = hallSize * CORRIDOR_START..hallSize * CORRIDOR_END

    /**
     * The margin of the external corridors, delimiting each hall on the right and on the top.
     */
    internal val externalBorder: Double get() = externalCorridor.start

    /**
     * The external corridors, delimiting each hall on the right and on the top.
     */
    internal val externalCorridor = hallSize * EXTERNAL_BORDER..hallSize

    /**
     * The room at the center of each hall.
     */
    internal val mainRoom = hallSize * ROOM_START..hallSize * ROOM_END

    /**
     * The farthest position reachable by the node towards the desired one, stopping at the walls of the halls.
     */
    override fun farthestReachablePosition(node: Node<T>, desired: Euclidean2DPosition): Euclidean2DPosition {
        val reachable = super.farthestReachablePosition(node, desired)
        if (isAllowed(reachable)) {
            return reachable
        }
        val (oldX, oldY) = currentPositionOf(node)
        val (newX, newY) = reachable
        val hallX = floor(oldX / hallSize) * hallSize
        val hallY = floor(oldY / hallSize) * hallSize
        val oldInHallX = oldX.relativeToTheHall
        val oldInHallY = oldY.relativeToTheHall
        val x = newX.relativeToTheHall
        val y = newY.relativeToTheHall
        return when {
            // Right corridor
            oldInHallX >= externalBorder -> makePosition(newX.stoppedAt(x, hallX, externalCorridor), newY)
            // Upper corridor
            oldInHallY >= externalBorder -> makePosition(newX, newY.stoppedAt(y, hallY, externalCorridor))
            // Main room
            oldInHallX in mainRoom && oldInHallY in mainRoom ->
                makePosition(newX.stoppedAt(x, hallX, mainRoom), newY.stoppedAt(y, hallY, mainRoom))
            // Horizontal lane passes (room positions and corridors already checked)
            openDoors && oldInHallY in centralCorridor -> makePosition(newX, newY.stoppedAt(y, hallY, centralCorridor))
            // Vertical lane passes (room positions and corridors already checked)
            oldInHallX in centralCorridor -> makePosition(newX.stoppedAt(x, hallX, centralCorridor), newY)
            else -> makePosition(newX, newY)
        }
    }

    override fun isAllowed(position: Euclidean2DPosition): Boolean {
        val hallX = position.x.relativeToTheHall
        val hallY = position.y.relativeToTheHall
        return hallX >= externalBorder || // Right corridor
            hallY >= externalBorder || // Upper corridor
            hallX in centralCorridor || // Vertical lane
            openDoors && hallY in centralCorridor || // Horizontal lane
            hallX in mainRoom && hallY in mainRoom // Room
    }

    /**
     * This coordinate, relative to the hall containing it.
     */
    private val Double.relativeToTheHall: Double get() = (this % hallSize + hallSize) % hallSize

    /**
     * This coordinate, or the closest wall of the hall starting at [hallStart] if its position [inHall]
     * falls outside the [range] passage.
     */
    private fun Double.stoppedAt(inHall: Double, hallStart: Double, range: ClosedFloatingPointRange<Double>): Double =
        if (inHall in range) this else hallStart + inHall.coerceIn(range)

    private companion object {
        private const val CORRIDOR_END = 0.51
        private const val CORRIDOR_START = 0.39
        private const val DEFAULT_SIZE = 10.0
        private const val EXTERNAL_BORDER = 0.89
        private const val ROOM_END = 0.81
        private const val ROOM_START = 0.09
    }
}
