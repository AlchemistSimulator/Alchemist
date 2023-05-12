/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * An euclidean bidimensional [EnvironmentWithGraph].
 */
interface Euclidean2DEnvironmentWithGraph<W, T, N, E> :
    EnvironmentWithGraph<W, T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    Euclidean2DEnvironmentWithObstacles<W, T>
    where W : it.unibo.alchemist.model.Obstacle2D<Euclidean2DPosition>,
          N : Euclidean2DConvexShape
