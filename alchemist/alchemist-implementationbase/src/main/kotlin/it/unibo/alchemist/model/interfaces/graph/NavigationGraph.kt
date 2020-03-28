package it.unibo.alchemist.model.interfaces.graph

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A graph used for navigation purposes. Nodes are [ConvexGeometricShape]s,
 * usually representing portions of an environment which are traversable by
 * agents (the advantage of such representation is that agents can freely
 * walk around within a convex area, as it is guaranteed that no obstacle
 * will be found).
 *
 * @param V the [Vector] type for the space.
 * @param A the transformations supported by the shapes in this environment.
 * @param N the type of nodes.
 * @param E the type of edges.
 */
interface NavigationGraph<V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> : Graph<N, E> {

    /**
     * A list of positions of interest (usually destinations).
     */
    fun destinations(): List<V>
}
