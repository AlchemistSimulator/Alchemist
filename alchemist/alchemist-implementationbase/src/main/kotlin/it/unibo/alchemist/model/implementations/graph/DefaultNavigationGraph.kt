package it.unibo.alchemist.model.implementations.graph

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * An implementation of [NavigationGraph].
 */
class NavigationGraphImpl<
    V : Vector<V>,
    A : GeometricTransformation<V>,
    N : ConvexGeometricShape<V, A>,
    E
>(
    private val destinations: List<V>,
    edgeClass: Class<out E>
) : NavigationGraph<V, A, N, E>,
    /*
     * Guarantees predictable iteration order, see https://jgrapht.org/guide/UserOverview para. 4.
     */
    DefaultDirectedGraph<N, E>(edgeClass) {

    override fun destinations(): List<V> = destinations
}
