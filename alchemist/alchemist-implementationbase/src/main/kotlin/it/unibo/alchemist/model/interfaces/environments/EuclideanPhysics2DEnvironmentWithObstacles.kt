/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Obstacle2D

/**
 * Euclidean physics environment with support for obstacles.
 */
interface EuclideanPhysics2DEnvironmentWithObstacles<W : Obstacle2D<Euclidean2DPosition>, T> :
    Euclidean2DEnvironmentWithObstacles<W, T>,
    Physics2DEnvironment<T>
