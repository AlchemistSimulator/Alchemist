/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.EnvironmentWithObstacles
import it.unibo.alchemist.model.Obstacle
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph

/**
 * An [EnvironmentWithObstacles] providing a [NavigationGraph]. This is a graph whose nodes are [ConvexGeometricShape]s
 * representing areas of the environment traversable by agents (namely, walkable areas), whereas edges represent
 * connections between these areas. For instance, in an indoor environment, nodes should represent rooms and corridors,
 * whereas edges should represent doors and passages. This data structure is also known as
 * [navigation mesh](https://en.wikipedia.org/wiki/Navigation_mesh).
 */
interface EnvironmentWithGraph<W, T, P, A, N, E> : EnvironmentWithObstacles<W, T, P>
    where W : Obstacle<P>,
          P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          N : ConvexGeometricShape<P, A> {

    /**
     * The navigation graph.
     */
    val graph: NavigationGraph<P, A, N, E>
}
