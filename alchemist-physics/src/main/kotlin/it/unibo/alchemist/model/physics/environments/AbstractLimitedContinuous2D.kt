/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
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
import java.io.Serial

/**
 * A 2D continuous environment with spatial constraints.
 * Nodes cannot move to positions that are not allowed.
 *
 * @param T the concentration type
 */
abstract class AbstractLimitedContinuous2D<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    ContinuousPhysics2DEnvironment<T>(incarnation) {

    private companion object {
        @Serial private const val serialVersionUID = -7838255122589911058L
    }

    override fun moveNodeToPosition(node: Node<T>, newPosition: Euclidean2DPosition) {
        val (curX, curY) = getPosition(node).coordinates
        val (newX, newY) = newPosition.coordinates
        super.moveNodeToPosition(node, next(curX, curY, newX, newY))
    }

    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition): Boolean =
        isAllowed(position) && super.nodeShouldBeAdded(node, position)

    /**
     * Computes the closest allowed position a node can move to.
     *
     * @param curX The current X coordinate
     * @param curY The current Y coordinate
     * @param newX The target X coordinate
     * @param newY The target Y coordinate
     * @return The next allowed position
     */
    protected abstract fun next(curX: Double, curY: Double, newX: Double, newY: Double): Euclidean2DPosition

    /**
     * Determines whether a position is allowed for a node.
     *
     * @param position The position to check
     * @return `true` if the position is allowed, `false` otherwise
     */
    protected abstract fun isAllowed(position: Euclidean2DPosition): Boolean
}
