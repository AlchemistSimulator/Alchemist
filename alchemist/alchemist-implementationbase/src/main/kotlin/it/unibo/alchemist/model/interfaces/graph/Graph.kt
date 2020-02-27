package it.unibo.alchemist.model.interfaces.geometry.graph

/**
 * Models a basic directed edge.
 *
 * @param N the type of nodes this edge connects.
 */
open class GraphEdge<N>(
    /**
     */
    val from: N,
    /**
     */
    val to: N
)

/**
 * A [GraphEdge] storing some kind of data.
 *
 * @param N the type of nodes this edge connects.
 * @param D the type of data.
 */
open class GraphEdgeWithData<N, D>(
    from: N,
    to: N,
    /**
     */
    val data: D
) : GraphEdge<N>(from, to)

/**
 * A graph composed by a set of nodes and a set of edges
 * connecting such nodes. Edges are directed, undirected
 * graphs can be obtained by duplicating each edge.
 *
 * @param N the type of nodes.
 * @param E the type of edges.
 */
interface Graph<N, E : GraphEdge<N>> {

    /**
     * A list is used to allow predictable iteration order.
     */
    fun nodes(): List<N>

    /**
     * @returns the edges outgoing from the specified node.
     * A list is used to allow predictable iteration order.
     */
    fun edgesFrom(node: N): List<E>
}
