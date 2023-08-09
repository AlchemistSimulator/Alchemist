/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.navigationgraph

import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A graph used for navigation purposes. Nodes are [ConvexShape]s, usually representing portions of an
 * environment which are traversable by agents (the advantage of such representation is that agents can freely
 * walk around within a convex area, as it is guaranteed that no obstacle will be found).
 * Note that implementations of this graph must guarantee predictable ordering for the collections they maintain,
 * as reproducibility is a key feature of Alchemist.
 * Be also aware that, by contract, the [org.jgrapht.Graph] interface does not allow duplicated edges (see
 * [org.jgrapht.Graph.addEdge]).
 *
 * @param V the [Vector] type for the space this graph describes.
 * @param A the transformations supported by shapes in this space.
 * @param N the type of nodes (or vertices).
 * @param E the type of edges.
 */
interface NavigationGraph<V, A, N, E> : org.jgrapht.Graph<N, E> where
      V : Vector<V>,
      A : Transformation<V>,
      N : ConvexShape<V, A> {

    /**
     * @returns the first node containing the specified [position], or null if no node containing it could be found.
     */
    fun nodeContaining(position: V): N? = vertexSet().firstOrNull { it.contains(position) }
}

/**
 * A [NavigationGraph] in an euclidean bidimensional space. Nodes are [ConvexPolygon]s and edges are
 * [Euclidean2DPassage]s. Using [Euclidean2DPassage]s as edges leads to some overhead (as these store the nodes they
 * connect, when this information is already stored in the navigation graph), but allows to have duplicate edges in
 * opposite directions, which means a node n1 can be connected to another node n2 through a passage whose shape is
 * equal to the one of the passage connecting n2 to n1. The two passages would not result equal because their tail
 * and head would be swapped. On the contrary, if edges were plain segments, the graph would not have allowed to have
 * two edges so that e1.equal(e2).
 */
typealias Euclidean2DNavigationGraph =
    NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DPassage>
