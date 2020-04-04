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
import it.unibo.alchemist.model.interfaces.Obstacle
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph

/**
 * An environment with obstacles providing a [NavigationGraph], i.e. a graph whose
 * nodes are convex shapes representing the areas of the environment traversable
 * by agents (namely, walkable areas), whereas edges represent connections between
 * them.
 */
interface EnvironmentWithGraph<
    T,
    P,
    W : Obstacle<P>,
    A : GeometricTransformation<P>,
    N : ConvexGeometricShape<P, A>,
    E
> : EnvironmentWithObstacles<T, P, W> where P : Position<P>, P : Vector<P> {

    /**
     * @returns the navigation graph.
     */
    fun graph(): NavigationGraph<P, A, N, E>
}

/**
 * An euclidean [EnvironmentWithGraph].
 */
interface Euclidean2DEnvironmentWithGraph<
    T,
    W : Obstacle2D<Euclidean2DPosition>,
    N : Euclidean2DConvexShape,
    E
> : EnvironmentWithGraph<T, Euclidean2DPosition, W, Euclidean2DTransformation, N, E>,
    Euclidean2DEnvironmentWithObstacles<T, W>

/**
 * An [Euclidean2DEnvironmentWithGraph] with physics.
 */
interface EuclideanPhysics2DEnvironmentWithGraph<
    T,
    W : Obstacle2D<Euclidean2DPosition>,
    N : Euclidean2DConvexShape,
    E
> : Euclidean2DEnvironmentWithGraph<T, W, N, E>, EuclideanPhysics2DEnvironmentWithObstacles<T, W>
