/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.Obstacle2D
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithObstacles
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * An [Euclidean2DEnvironmentWithObstacles] supporting physics.
 */
interface EuclideanPhysics2DEnvironmentWithObstacles<W, T> :
    Euclidean2DEnvironmentWithObstacles<W, T>,
    Physics2DEnvironment<T>
    where W : Obstacle2D<Euclidean2DPosition>
