/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.Obstacle
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory

/**
 * An [EnvironmentWithGraph] supporting physics.
 */
interface PhysicsEnvironmentWithGraph<W, T, P, A, N, E, F> :
    EnvironmentWithGraph<W, T, P, A, N, E>,
    PhysicsEnvironment<T, P, A, F>
    where W : Obstacle<P>,
          P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          N : ConvexGeometricShape<P, A>,
          F : GeometricShapeFactory<P, A>
