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
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph

/**
 * A bidimensional environment with obstacles providing a [NavigationGraph], i.e.
 * a graph whose nodes are convex shapes representing the areas of the environment
 * traversable by agents (namely, walkable areas), whereas edges represent connections
 * between them.
 */
interface Environment2DWithGraph<
    W : Obstacle2D,
    T,
    P,
    A : GeometricTransformation<P>,
    N : ConvexGeometricShape<P, A>,
    E : GraphEdge<N>
> : Environment2DWithObstacles<W, T, P> where P : Position<out P>, P : Vector<P> {

    /**
     * @returns the navigation graph.
     */
    fun graph(): NavigationGraph<P, A, N, E>
}

/**
 * An euclidean [Environment2DWithGraph].
 */
interface Euclidean2DEnvironmentWithGraph<
    W : Obstacle2D,
    T,
    N : Euclidean2DConvexShape,
    E : GraphEdge<N>
> : Environment2DWithGraph<W, T, Euclidean2DPosition, Euclidean2DTransformation, N, E>

/**
 * An [Environment2DWithGraph] with physics.
 */
interface EuclideanPhysics2DEnvironmentWithGraph<
    W : Obstacle2D,
    T,
    N : Euclidean2DConvexShape,
    E : GraphEdge<N>
> : Euclidean2DEnvironmentWithGraph<W, T, N, E>, EuclideanPhysics2DEnvironment<T>
