/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.EnvironmentWithObstacles
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Obstacle2D

/**
 * A bidimensional [EnvironmentWithObstacles].
 */
interface Environment2DWithObstacles<W : Obstacle2D<Euclidean2DPosition>, T> :
    EnvironmentWithObstacles<W, T, Euclidean2DPosition>,
    Euclidean2DEnvironment<T> {

    /**
     * Given a point and a range, retrieves all the obstacles within.
     *
     * @param center
     *              the center point
     * @param range
     *              the range to scan
     * @return the list of obstacles
     */
    fun getObstaclesInRange(center: Euclidean2DPosition, range: Double): List<W>

    /**
     * Given a point and a range, retrieves all the obstacles within.
     *
     * @param centerx
     *              the x coordinate of the center
     * @param centery
     *              the y coordinate of the center
     * @param range
     *              the range to scan
     * @return the list of Obstacles
     */
    /*
     * Maintained in order not to break older code.
     */
    fun getObstaclesInRange(centerx: Double, centery: Double, range: Double): List<W>

    /**
     * @return true if this environment has mobile obstacles, false if
     * the obstacles are static
     */
    fun hasMobileObstacles(): Boolean
}
