package it.unibo.alchemist.model.implementations.geometry.graph

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph

/**
 * An implementation of [NavigationGraph].
 */
class NavigationGraphImpl<V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>(
    adjacencyList: LinkedHashMap<N, out List<E>>,
    private val destinations: List<V>
) : NavigationGraph<V, A, N, E>, GraphImpl<N, E>(adjacencyList) {

    override fun destinations(): List<V> = destinations
}
