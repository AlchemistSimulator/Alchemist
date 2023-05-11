/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.Obstacle
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.physics.PhysicsEnvironment

/**
 * An [EnvironmentWithGraph] supporting physics.
 */
interface PhysicsEnvironmentWithObstacles<W, T, P, A, N, E, F> :
    EnvironmentWithGraph<W, T, P, A, N, E>,
    PhysicsEnvironment<T, P, A, F>
    where W : Obstacle<P>,
          P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          N : ConvexShape<P, A>,
          F : GeometricShapeFactory<P, A>
