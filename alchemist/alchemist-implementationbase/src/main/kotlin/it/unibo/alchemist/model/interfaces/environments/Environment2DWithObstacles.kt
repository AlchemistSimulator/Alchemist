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
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * An bidimensional [EnvironmentWithObstacles].
 *
 * Pragmatically, [P] could be restricted to Euclidean2DPosition, but
 * let's maintain as much generality as possible.
 */
interface Environment2DWithObstacles<T, P, W : Obstacle2D<P>> : EnvironmentWithObstacles<T, P, W>
    where P : Position2D<P>, P : Vector2D<P>

/**
 * An [Environment2DWithObstacles] using [Euclidean2DPosition]s.
 */
interface Euclidean2DEnvironmentWithObstacles<T, W : Obstacle2D<Euclidean2DPosition>> :
    Environment2DWithObstacles<T, Euclidean2DPosition, W>

/**
 * Euclidean physics environment with support for obstacles.
 */
interface EuclideanPhysics2DEnvironmentWithObstacles<T, W : Obstacle2D<Euclidean2DPosition>> :
    Physics2DEnvironment<T>,
    Euclidean2DEnvironmentWithObstacles<T, W>
