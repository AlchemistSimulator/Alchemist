/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Obstacle2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * An [Environment2DWithObstacles] using [Euclidean2DPosition]s.
 */
interface Euclidean2DEnvironmentWithObstacles<W, T> : Euclidean2DEnvironment<T>, Environment2DWithObstacles<W, T>
    where W : Obstacle2D<Euclidean2DPosition>
