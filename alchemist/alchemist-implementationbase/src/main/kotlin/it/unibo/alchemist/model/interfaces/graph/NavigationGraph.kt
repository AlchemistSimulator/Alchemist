package it.unibo.alchemist.model.interfaces.graph

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A graph used for navigation purposes. The nodes are [ConvexGeometricShape]s,
 * usually representing portions of an environment. Additionally, a navigation
 * graph can store some positions which are considered to be possible destinations.
 *
 * @param V the [Vector] type for the space.
 * @param A the transformations supported by the shapes in this environment.
 * @param N the type of nodes.
 * @param E the type of edges.
 */
interface NavigationGraph<V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> : Graph<N, E> {

    /**
     */
    fun destinations(): List<V>
}
