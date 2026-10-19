/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.geometry.Vector

/**
 * An environment with [Obstacle]s.
 *
 * @param W the type of obstacles.
 * @param T the concentration type.
 * @param P the position and vector type for this environment.
 */
interface EnvironmentWithObstacles<W, T, P> :
    EuclideanEnvironment<T, P> where
          W : Obstacle<P>,
          P : Position<P>,
          P : Vector<P> {
    /**
     * A list of all the obstacles in this environment, fixed once the environment holds nodes.
     */
    val obstacles: List<W>

    /**
     * Adds an obstacle to this environment.
     * Obstacles are part of the environment setup: adding one fails once the environment holds nodes.
     *
     * @param obstacle
     *              the obstacle to add
     */
    fun addObstacle(obstacle: W)

    /**
     * Removes an obstacle from this environment.
     * Obstacles are part of the environment setup: removing one fails once the environment holds nodes.
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
    fun shortestPathIntersectsObstacle(start: P, end: P): Boolean

    /**
     * Whether the segment connecting this position and [other] touches an obstacle,
     * see [shortestPathIntersectsObstacle].
     */
    fun P.shortestPathToIntersectsObstacle(other: P): Boolean = shortestPathIntersectsObstacle(this, other)

    /**
     * Removes this obstacle from the environment, see [removeObstacle].
     *
     * @return true if the obstacle has actually been removed
     */
    fun W.remove(): Boolean = removeObstacle(this)
}
