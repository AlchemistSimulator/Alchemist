package it.unibo.alchemist.model.implementations.graph

import it.unibo.alchemist.model.implementations.graph.builder.GraphBuilder
import it.unibo.alchemist.model.implementations.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.geometry.magnitude
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.Graph
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import java.util.PriorityQueue
import kotlin.collections.HashMap

/**
 * @returns the edges of the graph.
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.edges(): List<E> =
    nodes().flatMap { edgesFrom(it) }

/**
 * Checks if there is a path between two provided nodes.
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.isReachable(from: N, to: N): Boolean {
    require(nodes().contains(from) && nodes().contains(to)) { "nodes not found" }
    if (from == to || edgesFrom(from).any { it.head == to }) {
        return true
    }
    return depthFirstSearch(from, to)
}

/**
 * Performs a depth-first search starting from the provided [node] and looking for the [target] node.
 * @param node is the node that will be visited with the first iteration.
 * @param target is the target node we want to find.
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.depthFirstSearch(
    node: N,
    target: N,
    visited: HashMap<N, Boolean> = HashMap(nodes().size)
): Boolean {
    visited[node] = true
    with(edgesFrom(node).map { it.head }) {
        if (any { it == target }) {
            return true
        }
        return filter { visited[it] == null }.any { depthFirstSearch(it, target, visited) }
    }
}

/**
 * @returns the destinations within the provided [node].
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>
NavigationGraph<V, A, N, E>.destinationsWithin(node: N): Collection<V> =
    destinations().filter { node.contains(it) }

/**
 * Checks whether the provided [node] contains any destination.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>
NavigationGraph<V, A, N, E>.containsAnyDestination(node: N): Boolean =
    destinations().any { node.contains(it) }

/**
 * @returns the first node containing the specified position (or destination),
 * null if no node containing it could be found.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>
NavigationGraph<V, A, N, E>.nodeContaining(position: V): N? =
    nodes().firstOrNull { it.contains(position) }

/**
 * Models a path composed by a list of nodes and a weight.
 */
data class Path<N>(
    /**
     */
    val path: List<N>,
    /**
     */
    val weight: Double
)

/**
 * Finds the shortest path between the provided nodes, returns null if no path between them
 * could be found. If the two nodes coincide, the path will contain only such node and will
 * weight 0.
 * @param weight a function that weights each edge.
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.dijkstraShortestPath(from: N, to: N, weight: (E) -> Double): Path<N>? {
    return when {
        nodes().isEmpty() -> null
        from == to -> Path(mutableListOf(from), 0.0)
        else -> {
            /*
             * Distance from source to each node
             */
            val dist: HashMap<N, Double> = HashMap(nodes().size)
            nodes().forEach {
                dist[it] = Double.POSITIVE_INFINITY
            }
            dist[from] = 0.0
            /*
             * Predecessor of each node
             */
            val prev: HashMap<N, N> = HashMap(nodes().size)
            val queue: PriorityQueue<N> = PriorityQueue(nodes().size, compareBy { dist[it] })
            queue.add(from)
            while (queue.isNotEmpty()) {
                /*
                 * This is the node with min dist
                 */
                val node = queue.poll()
                edgesFrom(node).forEach { edge ->
                    val neighbor = edge.head
                    val distToNode = dist[node] ?: Double.POSITIVE_INFINITY
                    val distToNeighbor = distToNode + weight(edge)
                    val prevDist = dist[neighbor] ?: Double.POSITIVE_INFINITY
                    if (distToNeighbor < prevDist) {
                        /*
                         * The element is removed (if present) and added back to
                         * recompute its position in the heap since its dist changed
                         */
                        queue.remove(neighbor)
                        dist[neighbor] = distToNeighbor
                        prev[neighbor] = node
                        queue.add(neighbor)
                    }
                }
            }
            val path = backtrack(to, prev)
            when {
                path.contains(from) -> Path(path.reversed(), dist[to] ?: Double.POSITIVE_INFINITY)
                else -> null
            }
        }
    }
}

private fun <N> backtrack(end: N, prev: HashMap<N, N>): List<N> {
    val path: MutableList<N> = mutableListOf(end)
    var curr = end
    while (prev[curr] != null) {
        prev[curr]?.let {
            path.add(it)
            curr = it
        }
    }
    return path.reversed()
}

/**
 * See [dijkstraShortestPath]. In a navigation graph, the weight assigned by
 * default to each edge is computed as the distance between the centroids of the
 * two [ConvexGeometricShape]s the edge connects.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>
NavigationGraph<V, A, N, E>.dijkstraShortestPath(from: N, to: N): Path<N>? =
    dijkstraShortestPath(from, to, { (it.tail.centroid - it.head.centroid).magnitude() })

/**
 * Computes the minimum spanning forest of the graph using Prim's algorithm.
 * Note that the graph must be undirected (i.e. each edge must be duplicated).
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.primMinimumSpanningForest(weight: (E) -> Double): Graph<N, E> =
    primMinimumSpanningForest(weight, GraphBuilder(nodes().size)).build()

/**
 * See [primMinimumSpanningForest].
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>>
NavigationGraph<V, A, N, E>.primMinimumSpanningForest(
    weight: (E) -> Double = { (it.tail.centroid - it.head.centroid).magnitude() }
): NavigationGraph<V, A, N, E> =
    with(NavigationGraphBuilder<V, A, N, E>(nodes().size)) {
        primMinimumSpanningForest(weight, this)
        build(destinations())
    }

/*
 * Helper function to compute the MSF.
 */
@Suppress("NestedBlockDepth")
private fun <N, E : GraphEdge<N>> Graph<N, E>.primMinimumSpanningForest(
    weight: (E) -> Double,
    builder: GraphBuilder<N, E>,
    sourceNode: N? = null
): GraphBuilder<N, E> {
    if (nodes().isEmpty()) {
        return builder
    }
    val source = sourceNode ?: nodes().first()
    /*
     * Cheapest cost of a connection (an edge) to each node.
     */
    val cost: HashMap<N, Double> = HashMap(nodes().size)
    nodes().forEach {
        cost[it] = Double.POSITIVE_INFINITY
    }
    cost[source] = 0.0
    val prev: HashMap<N, N> = HashMap(nodes().size)
    val queue: PriorityQueue<N> = PriorityQueue(nodes().size, compareBy { cost[it] })
    queue.add(source)
    while (queue.isNotEmpty()) {
        val node = queue.poll()
        /*
         * We consider a node only if it is not already in
         * the minimum spanning tree
         */
        if (builder.addNode(node)) {
            prev[node]?.let {
                builder.addEdge(edgesFrom(it).first { e -> e.head == node })
                builder.addEdge(edgesFrom(node).first { e -> e.head == it })
            }
            edgesFrom(node).forEach { edge ->
                val neighbor = edge.head
                if (!builder.nodes().contains(neighbor)) {
                    val costToNeighbor = weight(edge)
                    val prevCost = cost[neighbor] ?: Double.POSITIVE_INFINITY
                    if (costToNeighbor < prevCost) {
                        queue.remove(neighbor)
                        cost[neighbor] = costToNeighbor
                        prev[neighbor] = node
                        queue.add(neighbor)
                    }
                }
            }
        }
    }
    return when {
        !builder.nodes().containsAll(nodes()) ->
            primMinimumSpanningForest(weight, builder, nodes().first { !builder.nodes().contains(it) })
        else -> builder
    }
}
