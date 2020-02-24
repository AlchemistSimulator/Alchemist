package it.unibo.alchemist.model.implementations.geometry.graph

import it.unibo.alchemist.model.implementations.geometry.graph.builder.GraphBuilder
import it.unibo.alchemist.model.implementations.geometry.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.geometry.magnitude
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.graph.Graph
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import java.util.*
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
    if (from == to || edgesFrom(from).any { it.to == to }) {
        return true
    }
    return dfs(from, to)
}

/**
 * Performs a depth-first search starting from the provided [node] and looking for the [target] node.
 * @param node is the node that will be visited with the first iteration.
 * @param target is the target node we want to find.
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.dfs(node: N, target: N, visited: HashMap<N, Boolean> = HashMap(nodes().size)): Boolean {
    visited[node] = true
    with(edgesFrom(node).map { it.to }) {
        if (any { it == target }) {
            return true
        }
        return filter { visited[it] == null }.any { dfs(it, target, visited) }
    }
}

/**
 * @returns the destinations within the provided [node].
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> NavigationGraph<V, A, N, E>.destinationsWithin(node: N): Collection<V> =
    destinations().filter { node.contains(it) }

/**
 * Checks whether the provided [node] contains any destination.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> NavigationGraph<V, A, N, E>.containsDestination(node: N): Boolean =
    destinations().any { node.contains(it) }

/**
 * @returns the first node containing the specified position (or destination),
 * null if no node containing it could be found.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> NavigationGraph<V, A, N, E>.nodeContaining(position: V): N? =
    nodes().firstOrNull { it.contains(position) }

/**
 * See [nodeContaining].
 */
fun NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>.nodeContaining(position: Euclidean2DPosition): ConvexPolygon? =
    nodes().firstOrNull { it.containsOrLiesOnBoundary(position) }

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
    if (nodes().isEmpty()) {
        return null
    }
    if (from == to) {
        return Path(mutableListOf(from), 0.0)
    }
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
    val q: PriorityQueue<N> = PriorityQueue(nodes().size, compareBy { dist[it] })
    q.add(from)
    while (q.isNotEmpty()) {
        /*
         * This is the node with min dist
         */
        val u = q.poll()
        edgesFrom(u).forEach { e ->
            /*
             * Edge e connects u to v
             */
            val v = e.to
            val distToV = dist[u]!! + weight(e)
            /*
             * A shorter path has been found
             */
            if (distToV < dist[v]!!) {
                /*
                 * The element is removed (if present) and added back to
                 * recompute its position in the heap since its dist changed
                 */
                q.remove(v)
                dist[v] = distToV
                prev[v] = u
                q.add(v)
            }
        }
    }
    /*
     * Backtracking
     */
    val path: MutableList<N> = mutableListOf(to)
    var curr = to
    while (prev[curr] != null) {
        path.add(prev[curr]!!)
        curr = prev[curr]!!
    }
    return if (path.contains(from)) {
        Path(path.reversed(), dist[to]!!)
    } else {
        null
    }
}

/**
 * See [dijkstraShortestPath]. In a navigation graph, the weight assigned by
 * default to each edge is computed as the distance between the centroids of the
 * two [ConvexGeometricShape]s the edge connects.
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> NavigationGraph<V, A, N, E>.dijkstraShortestPath(from: N, to: N): Path<N>? =
    dijkstraShortestPath(from, to, { (it.from.centroid - it.to.centroid).magnitude() } )

/**
 * Computes the minimum spanning tree of the graph. Note that the graph must be
 * undirected (i.e. each edge must be duplicated).
 */
fun <N, E : GraphEdge<N>> Graph<N, E>.primMST(weight: (E) -> Double): Graph<N, E> =
    primMST(weight, GraphBuilder(nodes().size)).build()

/**
 * See [primMST].
 */
fun <V : Vector<V>, A : GeometricTransformation<V>, N : ConvexGeometricShape<V, A>, E : GraphEdge<N>> NavigationGraph<V, A, N, E>.primMST(weight: (E) -> Double = { (it.from.centroid - it.to.centroid).magnitude() }): NavigationGraph<V, A, N, E> =
    with(NavigationGraphBuilder<V, A, N, E>(nodes().size)) {
        primMST(weight, this)
        build(destinations())
    }

/*
 * Helper function to compute the MST. Param s is the source node for prim's algorithm.
 */
private fun <N, E : GraphEdge<N>> Graph<N, E>.primMST(weight: (E) -> Double, builder: GraphBuilder<N, E>, s: N? = null): GraphBuilder<N, E> {
    if (nodes().isEmpty()) {
        return builder
    }
    val source = s ?: nodes().first()
    /*
     * Cheapest cost of a connection (an edge) to each node.
     */
    val cost: HashMap<N, Double> = HashMap(nodes().size)
    nodes().forEach {
        cost[it] = Double.POSITIVE_INFINITY
    }
    cost[source] = 0.0
    val prev: HashMap<N, N> = HashMap(nodes().size)
    val q: PriorityQueue<N> = PriorityQueue(nodes().size, compareBy { cost[it] })
    q.add(source)
    while (q.isNotEmpty()) {
        val u = q.poll()
        /*
         * We consider a node only if it is not already in
         * the minimum spanning tree
         */
        if (builder.addNode(u)) {
            prev[u]?.let {
                builder.addEdge(edgesFrom(it).first { e -> e.to == u })
                builder.addEdge(edgesFrom(u).first { e -> e.to == it })
            }
            edgesFrom(u).forEach { e ->
                val v = e.to
                if (!builder.nodes().contains(v)) {
                    val w = weight(e)
                    if (w < cost[v]!!) {
                        q.remove(v)
                        cost[v] = w
                        prev[v] = u
                        q.add(v)
                    }
                }
            }
        }
    }
    if (!builder.nodes().containsAll(nodes())) {
        return primMST(weight, builder, nodes().first { !builder.nodes().contains(it) })
    }
    return builder
}
