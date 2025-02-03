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
     * Computes the diameter of the subnetworks of the environment.
     * The diameter is the longest shortest path between any two nodes,
     * evaluated using the [allShortestHopPaths] method.
     * Returns a [Set] containing the [Subnetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksByNodeWithHopDistance(): Map<Node<T>, Network<T>> =
        allSubNetworksByNode(hopDistance())

    /**
     * Computes the diameter of the subnetworks of the environment.
     * The diameter is the longest shortest path between any two nodes,
     * evaluated using the [allShortestHopPaths] method.
     * Returns a [Set] containing the [Subnetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksWithHopDistance(): Set<Network<T>> =
        allSubNetworksByNodeWithHopDistance().values.toSet()

    /**
     * Computes the diameter of the subnetworks of the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Set] containing the [Subnetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworksByNode(
        computeDistance: (Node<T>, Node<T>) -> Double = environmentMetricDistance(),
    ): Map<Node<T>, Network<T>> {
        data class SubNetwork<T>(
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
        val subnetworks = mutableMapOf<Node<T>, Network<T>>()

        fun subnetOf(node: Node<T>): Network<T> = subnetworks.getOrPut(node) { SubNetwork(node) }
        val paths = allShortestPaths(computeDistance)
        for (i in 0 until nodes.size) {
            val reference = nodes[i]
            for (j in i until nodes.size) {
                val target = nodes[j]
                val distance = paths[reference to target]
                if (distance != null && distance.isFinite()) {
                    val merger = subnetOf(reference) + subnetOf(target)
                    subnetworks[reference] = merger
                    subnetworks[target] = merger
                }
            }
        }
        return subnetworks
    }

    /**
     * Computes the diameter of the subnetworks of the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Set] containing the [Subnetwork]s.
     */
    fun <T> Environment<T, *>.allSubNetworks(
        computeDistance: (Node<T>, Node<T>) -> Double = { n1, n2 -> getDistanceBetweenNodes(n1, n2) },
    ): Set<Network<T>> = allSubNetworksByNode(computeDistance).values.toSet()

    /**
     * Calculates the shortest paths using the Floyd-Warshall algorithm calculating the Hop Distance between nodes.
     */
    fun <T> Environment<T, *>.allShortestHopPaths() = allShortestPaths(hopDistance())

    /**
     * Computes all the minimum distances using the Floyd–Warshall algorithm.
     */
    fun <T> Environment<T, *>.allShortestPaths(
        computeDistance: (Node<T>, Node<T>) -> Double =
            neighborDistanceMetric { n1, n2 ->
                getDistanceBetweenNodes(n1, n2)
            },
    ): Map<Pair<Node<T>, Node<T>>, Double> {
        val nodes = nodes.toList()
        /*
         * The distance matrix is a triangular matrix stored in a flat array.
         */
        val distances = MutableDoubleSymmetricMatrix(nodeCount)
        val result = LinkedHashMap<Pair<Node<T>, Node<T>>, Double>(nodes.size * (nodes.size + 1) / 2, 1.0f)
        for (i in 0 until nodeCount) {
            for (j in i + 1 until nodeCount) {
                distances[i, j] = computeDistance(nodes[i], nodes[j])
                if (distances[i, j].isFinite()) {
                    result.put(nodes[i] to nodes[j], distances[i, j])
                }
            }
        }
        for (cycle in 0 until nodeCount) {
            result.put(nodes[cycle].let { node -> node to node }, 0.0)
            for (i in 0 until nodeCount) {
                for (j in i + 1 until nodeCount) {
                    if (distances[i, j] > distances[i, cycle] + distances[cycle, j]) {
                        distances[i, j] = distances[i, cycle] + distances[cycle, j]
                        result.put(nodes[i] to nodes[j], distances[i, j])
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
    fun Environment<*, *>.networkDiameter(): Double =
        allSubNetworks().singleOrNull()?.diameter ?: NaN
}
