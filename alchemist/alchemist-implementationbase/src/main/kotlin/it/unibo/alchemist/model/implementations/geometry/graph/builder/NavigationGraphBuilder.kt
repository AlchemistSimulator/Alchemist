package it.unibo.alchemist.model.implementations.geometry.graph.builder

import it.unibo.alchemist.model.implementations.geometry.graph.NavigationGraphImpl
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph

/**
 */
class NavigationGraphBuilder<V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>(
    initialCapacity: Int = 1
) : GraphBuilder<N, E>(initialCapacity) {

    /**
     */
    fun build(destinations: Collection<V>): NavigationGraph<V, A, N, E> =
        NavigationGraphImpl(adjacencyList, destinations.toList())
}
