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
import org.danilopianini.symmetricmatrix.SymmetricMatrix
import kotlin.Double.Companion.NaN
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max

/**
 * Extensions functions of generic environments.
 */
object Environments {
    private fun <T> Environment<T, *>.neighborDistanceMetric(
        computeDistance: (Node<T>, Node<T>) -> Double,
    ): (Node<T>, Node<T>) -> Double = { n1, n2 ->
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
    fun <T> Environment<T, *>.allSubNetworks(
        computeDistance: (Node<T>, Node<T>) -> Double = environmentMetricDistance(),
    ): Set<Network<T>> {
        val subnetworks = arrayOfNulls<MutableNetwork<T>>(nodeCount)
        val result = mutableSetOf<Network<T>>()
        val paths = allShortestPaths(computeDistance)
        // Update all the subnetworks with the last evaluated; that is the most complete
        nodes.forEachIndexed { centerIndex, centerNode ->
            when (val subnetwork = subnetworks[centerIndex]) {
                null -> {
                    val newSubnetwork = MutableNetwork(0.0, mutableListOf(centerNode))
                    result.add(newSubnetwork)
                    val centerRow = paths.row(centerIndex)
                    for (potentialNeighborIndex in (centerIndex + 1) until nodeCount) {
                        val distanceToNeighbor = centerRow[potentialNeighborIndex]
                        if (distanceToNeighbor.isFinite()) {
                            newSubnetwork.diameter = max(newSubnetwork.diameter, distanceToNeighbor)
                            newSubnetwork.neighbors += nodes[potentialNeighborIndex]
                            subnetworks[potentialNeighborIndex] = newSubnetwork
                        }
                    }
                }
                else -> {
                    // The subnetwork is already computed
                    subnetwork.diameter = max(
                        subnetwork.diameter,
                        paths.row(centerIndex).asSequence()
                            .drop(centerIndex + 1)
                            .filter { it.isFinite() }
                            .maxOrNull()
                            ?: 0.0,
                    )
                }
            }
        }
        return result
    }

    /**
     * Computes the diameter of all subnetworks in the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Map] containing the [SubNetwork] related to each [Node] of the environment.
     */
    fun <T> Environment<T, *>.allSubNetworksByNode(
        computeDistance: (Node<T>, Node<T>) -> Double = environmentMetricDistance(),
    ): Map<Node<T>, Network<T>> = allSubNetworks(computeDistance).let { subnetworks ->
        nodes.associateWith { node ->
            subnetworks.first { it.nodes.contains(node) }
        }
    }

    /**
     * Calculates the shortest paths using the Floyd-Warshall algorithm calculating the Hop Distance between nodes.
     */
    fun <T> Environment<T, *>.allShortestHopPaths() = allShortestPaths(hopDistance())

    /**
     * Computes all the minimum distances with the provided metric using the Floyd–Warshall algorithm.
     */
    private fun <T> Environment<T, *>.allShortestPaths(
        computeDistance: (Node<T>, Node<T>) -> Double =
            neighborDistanceMetric { n1, n2 ->
                getDistanceBetweenNodes(n1, n2)
            },
    ): SymmetricMatrix<Double> {
        val nodes = nodes.toList()
        /*
         * The distance matrix is a triangular matrix stored in a flat array.
         */
        val distances = MutableDoubleSymmetricMatrix(nodeCount)
        for (i in 0 until nodeCount) {
            for (j in i until nodeCount) {
                distances[i, j] = computeDistance(nodes[i], nodes[j])
            }
        }
        for (intermediate in 0 until nodeCount) {
            for (i in 0 until nodeCount) {
                for (j in i + 1 until nodeCount) {
                    val throughIntermediate = distances[i, intermediate] + distances[intermediate, j]
                    if (distances[i, j] > throughIntermediate) {
                        distances[i, j] = throughIntermediate
                    }
                }
            }
        }
        return distances
    }

    /**
     * Returns true the network is segmented, false otherwise.
     */
    fun <T> Environment<T, *>.isNetworkSegmented(): Boolean {
        val explored = mutableSetOf<Node<T>>()
        val toExplore: MutableSet<Node<T>> = nodes.firstOrNull()?.let { setOf(it) }.orEmpty().toMutableSet()
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

    private data class SubNetwork<T>(override val diameter: Double, override val nodes: Set<Node<T>>) : Network<T> {
        init {
            require(nodes.isNotEmpty())
            require(diameter.isFinite() && diameter >= 0.0)
        }

        constructor(diameter: Double, vararg nodes: Node<T>) : this(diameter, nodes.toSet())

        constructor(diameter: Double, nodes: Collection<Node<T>>) : this(diameter, nodes.toSet())
    }

    private data class MutableNetwork<T>(override var diameter: Double, val neighbors: MutableList<Node<T>>) :
        Network<T> {
        override val nodes: Set<Node<T>> by lazy { neighbors.toSet() }
    }
}
