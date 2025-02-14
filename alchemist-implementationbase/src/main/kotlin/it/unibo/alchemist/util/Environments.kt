/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Network
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.util.Environments.allShortestHopPaths
import org.danilopianini.symmetricmatrix.MutableDoubleSymmetricMatrix
import kotlin.Double.Companion.NaN
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max

/**
 * Extensions functions of generic environments.
 */
object Environments {
    private fun <T> Environment<T, *>.neighborDistanceMetric(
        computeDistance: (Node<T>, Node<T>) -> Double,
    ): (Node<T>, Node<T>) -> Double =
        { n1, n2 ->
            when {
                n1 == n2 -> 0.0
                n2 in getNeighborhood(n1) -> computeDistance(n1, n2)
                else -> POSITIVE_INFINITY
            }
        }

    private fun <T> Environment<T, *>.hopDistance() = neighborDistanceMetric { n1, n2 -> 1.0 }

    private fun <T> Environment<T, *>.environmentMetricDistance() =
        neighborDistanceMetric { n1, n2 -> getDistanceBetweenNodes(n1, n2) }

    /**
     * Computes the diameter of all subnetworks in the environment.
     * The diameter is the longest shortest path between any two nodes,
     * evaluated using the [allShortestHopPaths] method.
     * Returns a [Set] containing the [SubNetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksByNodeWithHopDistance(): Map<Node<T>, Network<T>> =
        allSubNetworksByNode(hopDistance())

    /**
     * Computes the diameter of all subnetworks in the environment.
     * The diameter is the longest shortest path between any two nodes,
     * evaluated using the [allShortestHopPaths] method.
     * Returns a [Set] containing the [SubNetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksWithHopDistance(): Set<Network<T>> =
        allSubNetworksByNodeWithHopDistance().values.toSet()

    /**
     * Computes the diameter of all subnetworks in the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Set] containing the [SubNetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksByNode(
        computeDistance: (Node<T>, Node<T>) -> Double = environmentMetricDistance(),
    ): Map<Node<T>, Network<T>> {
        val subnetworks = mutableMapOf<Node<T>, Network<T>>()

        fun subnetOf(
            distance: Double,
            node: Node<T>,
        ): Network<T> =
            subnetworks[node]
                .let { subnet ->
                    val result = SubNetwork(distance, node)
                    if (subnet == null) result else result + subnet
                }
        val paths = allShortestPaths(computeDistance)
        for (i in 0 until nodes.size) {
            val reference = nodes[i]
            for (j in i until nodes.size) {
                val target = nodes[j]
                val distance = paths[UndirectedEdge(reference, target)]
                if (distance != null && distance.isFinite()) {
                    val merger = subnetOf(distance, reference) + subnetOf(distance, target)
                    subnetworks[target] = merger
                }
            }
        }
        // Update all the subnetworks with the last evaluated, that is the most complete
        val toVisit = nodes.toMutableSet()
        while (toVisit.isNotEmpty()) {
            val current = toVisit.last().also { toVisit -= it }
            val subnet = subnetworks.getValue(current)
            // For each node in the current subnet, update the subnetworks related to each node
            subnet.nodes.forEach { node ->
                toVisit -= node
                subnetworks[node] = subnet
            }
        }
        return subnetworks
    }

    /**
     * Computes the diameter of all subnetworks in the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Set] containing the [SubNetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworks(
        computeDistance: (Node<T>, Node<T>) -> Double = environmentMetricDistance(),
    ): Set<Network<T>> = allSubNetworksByNode(computeDistance).values.toSet()

    /**
     * Calculates the shortest paths using the Floyd-Warshall algorithm calculating the Hop Distance between nodes.
     */
    fun <T> Environment<T, *>.allShortestHopPaths() = allShortestPaths(hopDistance())

    /**
     * Represents an undirected edge between the [source] and the [target] nodes.
     * The order of the nodes does not matter.
     */
    data class UndirectedEdge<T>(
        val source: Node<T>,
        val target: Node<T>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is UndirectedEdge<*>) return false
            return (source == other.source && target == other.target) ||
                (source == other.target && target == other.source)
        }

        override fun hashCode(): Int = source.hashCode() + target.hashCode()
    }

    /**
     * Computes all the minimum distances with the provided metric using the Floyd–Warshall algorithm.
     */
    fun <T> Environment<T, *>.allShortestPaths(
        computeDistance: (Node<T>, Node<T>) -> Double =
            neighborDistanceMetric { n1, n2 ->
                getDistanceBetweenNodes(n1, n2)
            },
    ): Map<UndirectedEdge<T>, Double> {
        val nodes = nodes.toList()
        /*
         * The distance matrix is a triangular matrix stored in a flat array.
         */
        val distances = MutableDoubleSymmetricMatrix(nodeCount)
        val result = LinkedHashMap<UndirectedEdge<T>, Double>(nodes.size * (nodes.size + 1) / 2, 1.0f)
        for (i in 0 until nodeCount) {
            for (j in i until nodeCount) {
                distances[i, j] = computeDistance(nodes[i], nodes[j])
                if (distances[i, j].isFinite()) {
                    result.put(UndirectedEdge(nodes[i], nodes[j]), distances[i, j])
                }
            }
        }
        for (intermediate in 0 until nodeCount) {
            for (i in 0 until nodeCount) {
                for (j in i + 1 until nodeCount) {
                    val throughIntermediate = distances[i, intermediate] + distances[intermediate, j]
                    if (distances[i, j] > throughIntermediate) {
                        distances[i, j] = throughIntermediate
                        result.put(UndirectedEdge(nodes[i], nodes[j]), throughIntermediate)
                    }
                }
            }
        }
        return result
    }

    /**
     * Returns true the network is segmented, false otherwise.
     */
    fun <T> Environment<T, *>.isNetworkSegmented(): Boolean {
        val explored = mutableSetOf<Node<T>>()
        val toExplore: MutableSet<Node<T>> =
            nodes
                .firstOrNull()
                ?.let { setOf(it) }
                .orEmpty()
                .toMutableSet()
        while (toExplore.isNotEmpty()) {
            val current = toExplore.first()
            explored += current
            val neighbors = getNeighborhood(current).toMutableSet()
            toExplore += neighbors
            toExplore -= explored
        }
        return explored.size < nodes.size
    }

    /**
     * Computes the network diameter of the segment containing [node].
     */
    fun <T> Environment<T, *>.networkDiameterByHopDistance(node: Node<T>): Double =
        requireNotNull(allSubNetworksByNodeWithHopDistance()[node]) {
            "Subnetwork for $node cannot be computed: is it part of the environment?"
        }.diameter

    /**
     * Returns the hop-distance diameter of the network if it is not segmented, and [NaN] otherwise.
     */
    fun Environment<*, *>.networkDiameterByHopDistance(): Double =
        allSubNetworksWithHopDistance().singleOrNull()?.diameter ?: NaN

    /**
     * Returns the diameter of the network in environment units if it is not segmented, and [NaN] otherwise.
     */
    fun Environment<*, *>.networkDiameter(): Double = allSubNetworks().singleOrNull()?.diameter ?: NaN

    private data class SubNetwork<T>(
        override val diameter: Double,
        override val nodes: Set<Node<T>>,
    ) : Network<T> {
        init {
            require(nodes.isNotEmpty())
            require(diameter.isFinite() && diameter >= 0.0)
        }

        constructor(diameter: Double, vararg nodes: Node<T>) : this(diameter, nodes.toSet())

        constructor(node: Node<T>) : this(0.0, setOf(node))

        override fun plus(otherNetwork: Network<T>): Network<T> =
            SubNetwork(max(diameter, otherNetwork.diameter), nodes + otherNetwork.nodes)
    }
}
