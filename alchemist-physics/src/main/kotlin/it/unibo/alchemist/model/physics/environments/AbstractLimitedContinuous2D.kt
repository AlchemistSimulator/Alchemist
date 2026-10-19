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

/**
 * A 2D continuous environment with spatial constraints.
 * Nodes cannot be added in positions that are not allowed, and subclasses stop them before reaching one through
 * [farthestReachablePosition].
 *
 * @param T the concentration type
 * @param incarnation the incarnation used to build nodes and concentrations
 */
abstract class AbstractLimitedContinuous2D<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    ContinuousPhysics2DEnvironment<T>(incarnation) {

    override fun nodeShouldBeAdded(node: Node<T>, position: Euclidean2DPosition): Boolean =
        isAllowed(position) && super.nodeShouldBeAdded(node, position)

    /**
     * Determines whether a position is allowed for a node.
     *
     * @param position The position to check
     * @return `true` if the position is allowed, `false` otherwise
     */
    protected abstract fun isAllowed(position: Euclidean2DPosition): Boolean
}
