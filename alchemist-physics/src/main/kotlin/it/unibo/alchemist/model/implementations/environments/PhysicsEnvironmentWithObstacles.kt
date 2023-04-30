/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.environments

import it.unibo.alchemist.model.obstacles.RectObstacle2D
import it.unibo.alchemist.model.physics.environments.EuclideanPhysics2DEnvironmentWithObstacles
import it.unibo.alchemist.model.positions.Euclidean2DPosition

typealias PhysicsEnvironmentWithObstacles<T> =
EuclideanPhysics2DEnvironmentWithObstacles<RectObstacle2D<Euclidean2DPosition>, T>
