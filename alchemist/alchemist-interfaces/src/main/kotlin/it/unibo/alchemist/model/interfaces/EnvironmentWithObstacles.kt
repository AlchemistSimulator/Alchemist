/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

/**
 * An environment with obstacles.
 *
 * @param W the type of obstacles.
 * @param T the concentration type.
 * @param P the position type for this environment.
 */
interface EnvironmentWithObstacles<W : Obstacle<P>, T, P : Position<P>> : Environment<T, P> {

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
}
