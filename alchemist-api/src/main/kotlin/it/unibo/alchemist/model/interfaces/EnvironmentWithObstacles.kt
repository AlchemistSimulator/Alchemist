/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An environment with [Obstacle]s.
 *
 * @param W the type of obstacles.
 * @param T the concentration type.
 * @param P the position and vector type for this environment.
 */
interface EnvironmentWithObstacles<W, T, P> : EuclideanEnvironment<T, P> where
W : Obstacle<P>,
P : Position<P>,
P : Vector<P> {

    /**
     * A list of all the obstacles in this environment.
     */
    val obstacles: List<W>

    /**
     * Adds an obstacle to this environment.
     *
     * @param obstacle
     *              the obstacle to add
     */
    fun addObstacle(obstacle: W)

    /**
     * Removes an obstacle from this environment.
     *
     * @param obstacle
     *              the obstacle to remove
     * @return true if the obstacle has actually been removed
     */
    fun removeObstacle(obstacle: W): Boolean

    /**
     * Checks whether there is at least an obstacle intersecting the line connecting [start] and [end].
     *
     * @param start
     *              start position
     * @param end
     *              end position
     * @return true if the line connecting start and end touches an obstacle
     */
    fun intersectsObstacle(start: P, end: P): Boolean

    /**
     * This method must calculate the ABSOLUTE next allowed position given the
     * current position and the position in which the node wants to move. For
     * example, if your node is in position [2,3], wants to move to [3,4] but
     * the next allowed position (because, e.g., of physical obstacles) is
     * [2.5,3.5], the result must be a Position containing coordinates
     * [2.5,3.5].
     *
     * @param current
     *              the current position
     * @param desired
     *              the desired position
     * @return the next allowed position, where the node can actually move. This
     * position MUST be considered as a vector whose start point is [current].
     */
    fun next(current: P, desired: P): P
}
