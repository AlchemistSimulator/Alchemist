package it.unibo.alchemist.model.implementations.graph.builder

import it.unibo.alchemist.model.implementations.graph.GraphImpl
import it.unibo.alchemist.model.interfaces.graph.Graph
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
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
     * (edge.g. it was already present an edge connecting such
     * nodes).
     */
    open fun addEdge(edge: E): Boolean {
        /*
         * Each node should be added to the key set of the adjacency list
         * (even if it has no edges departing from it), otherwise it won't
         * be considered when the nodes() function is called.
         */
        if (adjacencyList[edge.to] == null) {
            adjacencyList[edge.to] = mutableListOf()
        }
        adjacencyList[edge.from]?.let {
            return if (!it.map { e -> e.to }.contains(edge.to)) {
                it.add(edge)
                true
            } else {
                false
            }
        }
        adjacencyList[edge.from] = mutableListOf(edge)
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
 * Adds two edges: one connecting node1 to node2 and another connecting node2 to node1.
 */
fun <N> GraphBuilder<N, GraphEdge<N>>.addUndirectedEdge(node1: N, node2: N): Boolean =
    addEdge(node1, node2) || addEdge(node2, node1)

/**
 */
fun <N, D> GraphBuilder<N, GraphEdgeWithData<N, D>>.addEdge(from: N, to: N, data: D): Boolean =
    addEdge(GraphEdgeWithData(from, to, data))

/**
 * Adds two edges: one connecting node1 to node2 and another connecting node2 to node1.
 */
fun <N, D> GraphBuilder<N, GraphEdgeWithData<N, D>>.addUndirectedEdge(node1: N, node2: N, data: D): Boolean =
    addEdge(node1, node2, data) || addEdge(node2, node1, data)
