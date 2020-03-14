package it.unibo.alchemist.model.implementations.graph

import it.unibo.alchemist.model.interfaces.graph.Graph
import it.unibo.alchemist.model.interfaces.graph.GraphEdge

/**
 * An implementation of [Graph].
 */
open class GraphImpl<N, E : GraphEdge<N>>(
    /*
     * The adjacency list maps each node to the list of edges departing from it.
     * A LinkedHashMap guarantees predictable iteration order.
     */
    private val adjacencyList: LinkedHashMap<N, out List<E>>
) : Graph<N, E> {

    override fun nodes() = adjacencyList.keys.toList()

    override fun edgesFrom(node: N) = adjacencyList[node] ?: throw IllegalArgumentException("node not found")
}
