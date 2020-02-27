package it.unibo.alchemist.model.implementations.geometry.graph.builder

import it.unibo.alchemist.model.implementations.geometry.graph.GraphImpl
import it.unibo.alchemist.model.interfaces.geometry.graph.Graph
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData
import java.lang.IllegalArgumentException

/**
 * A builder for [Graph].
 */
open class GraphBuilder<N, E : GraphEdge<N>>(
    initialCapacity: Int = 1
) {

    /**
     */
    protected val adjacencyList: LinkedHashMap<N, MutableList<E>> = LinkedHashMap(initialCapacity)

    /**
     * The builder allows to access already added node.
     */
    open fun nodes(): List<N> = adjacencyList.keys.toList()

    /**
     * @returns true if the node was added, false otherwise
     * (e.g. it was already present).
     */
    open fun addNode(node: N): Boolean {
        if (!adjacencyList.keys.contains(node)) {
            adjacencyList[node] = mutableListOf()
            return true
        }
        return false
    }

    /**
     * The builder allows to access already added edges.
     */
    open fun edgesFrom(node: N): List<E> =
        adjacencyList[node] ?: throw IllegalArgumentException("node not found")

    /**
     * @returns true if the edge was added, false otherwise
     * (e.g. it was already present an edge connecting such
     * nodes).
     */
    open fun addEdge(e: E): Boolean {
        /*
         * Each node should be added to the key set of the adjacency list
         * (even if it has no edges departing from it), otherwise it won't
         * be considered when the nodes() function is called.
         */
        if (adjacencyList[e.to] == null) {
            adjacencyList[e.to] = mutableListOf()
        }
        adjacencyList[e.from]?.let {
            return if (!it.map { e -> e.to }.contains(e.to)) {
                it.add(e)
                true
            } else {
                false
            }
        }
        adjacencyList[e.from] = mutableListOf(e)
        return true
    }

    /**
     * Builds the graph.
     */
    open fun build(): Graph<N, E> = GraphImpl(adjacencyList)
}

/**
 */
fun <N> GraphBuilder<N, GraphEdge<N>>.addEdge(from: N, to: N): Boolean =
    addEdge(GraphEdge(from, to))

/**
 * Adds two edges: one connecting n1 to n2 and another connecting n2 to n1.
 */
fun <N> GraphBuilder<N, GraphEdge<N>>.addUndirectedEdge(n1: N, n2: N): Boolean =
    addEdge(n1, n2) || addEdge(n2, n1)

/**
 */
fun <N, D> GraphBuilder<N, GraphEdgeWithData<N, D>>.addEdge(from: N, to: N, data: D): Boolean =
    addEdge(GraphEdgeWithData(from, to, data))

/**
 * Adds two edges: one connecting n1 to n2 and another connecting n2 to n1.
 */
fun <N, D> GraphBuilder<N, GraphEdgeWithData<N, D>>.addUndirectedEdge(n1: N, n2: N, data: D): Boolean =
    addEdge(n1, n2, data) || addEdge(n2, n1, data)
