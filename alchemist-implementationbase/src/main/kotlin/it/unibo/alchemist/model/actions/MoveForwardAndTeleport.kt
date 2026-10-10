/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position2D

/**
 * Moves the node along the x axis up to [maxX], with steps of size [deltaX]; once beyond [maxX], the node is
 * teleported back to [minX]. It mimics the movement of a node in a cylindrical environment.
 *
 * @param T concentration type
 * @param P position type
 * @property deltaX the step along the x axis
 * @property minX the teleport destination along the x axis
 * @property maxX the x coordinate beyond which the node is teleported
 */
class MoveForwardAndTeleport<T, P : Position2D<P>>(
    environment: Environment<T, P>,
    reaction: NodeReaction<T>,
    val deltaX: Double,
    val minX: Double,
    val maxX: Double,
) : AbstractMoveNode<T, P>(environment, reaction, true) {
    private var y = Double.NaN

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): MoveForwardAndTeleport<T, P> =
        MoveForwardAndTeleport(environment, newReaction, deltaX, minX, maxX)

    override fun getNextPosition(): P {
        val current = environment.getCurrentPosition(targetNode)
        if (y.isNaN()) {
            y = current.y
        }
        val x = current.x
        return environment.makePosition(if (x > maxX) minX else x + deltaX, y)
    }
}
