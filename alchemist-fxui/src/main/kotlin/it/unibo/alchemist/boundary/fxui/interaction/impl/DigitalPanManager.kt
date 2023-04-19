/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.impl

import it.unibo.alchemist.boundary.fxui.interaction.api.Direction2D
import it.unibo.alchemist.boundary.fxui.util.PointExtension.plus
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.interfaces.Position2D
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

/**
 * Manages panning towards a cardinal (N, S, E, W) or intercardinal (NE, NW, SE, SW) direction.
 * When a direction is added, panning towards it begins and doesn't stop until the given direction is removed.
 * @param speed The speed of each movement.
 * @param period Amount of time (milliseconds) between each movement.
 * @param wormhole The wormhole used to pan.
 * @param updates A runnable which will be called whenever a panning movement occurs.
 */
class DigitalPanManager<P : Position2D<P>>(
    private val speed: Int = 5,
    private val period: Long = 15,
    private val wormhole: Wormhole2D<P>,
    private val updates: () -> Unit,
) {
    private var timer: Timer = Timer()
    private var currentDirection: Direction2D = Direction2D.NONE

    /**
     * Inputs a movement towards a certain direction.
     */
    operator fun plusAssign(direction: Direction2D) {
        if (direction !in currentDirection) {
            redirect(currentDirection + direction)
        }
    }

    /**
     * Stops moving towards a certain direction.
     */
    operator fun minusAssign(direction: Direction2D) {
        if (direction in currentDirection) {
            redirect(currentDirection - direction)
        }
    }

    /**
     * Redirects the movement towards a given direction,
     * potentially stopping the movement altogether if the given direction is [Direction2D.NONE].
     */
    private fun redirect(direction: Direction2D) {
        if (direction == Direction2D.NONE) {
            timer.cancel()
            currentDirection = Direction2D.NONE
        } else {
            if (direction != currentDirection) {
                timer.cancel()
                currentDirection = direction
                // the X-axis seems to be flipped... (grows positively towards the "left")
                // so we flip the X-values of the directions
                direction.flippedX.let {
                    timer = fixedRateTimer(period = period) {
                        wormhole.viewPosition += it * speed
                        updates()
                    }
                }
            }
        }
    }
}
