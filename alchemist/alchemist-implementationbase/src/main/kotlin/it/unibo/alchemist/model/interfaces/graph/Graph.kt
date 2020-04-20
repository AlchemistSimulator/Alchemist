package it.unibo.alchemist.model.interfaces.graph

/**
 * Models a basic directed edge.
 *
 * @param N
 *          the type of nodes this edge connects.
 * @param tail
 *          the node this edge outgoes from.
 * @param head
 *          the node this edge arrives to.
 */
open class GraphEdge<N>(
    /**
     * The node this edge outgoes from.
     */
    val tail: N,
    /**
     * The node this edge arrives to.
     */
    val head: N
)

/**
 * A [GraphEdge] storing some kind of data.
 *
 * @param N
 *          the type of nodes this edge connects.
 * @param D
 *          the type of data.
 * @param data
 *          the data this edge stores.
 */
open class GraphEdgeWithData<N, D>(
    tail: N,
    head: N,
    /**
     * The data this edge stores.
     */
    val data: D
) : GraphEdge<N>(tail, head)

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
