/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.core.Simulation
import org.danilopianini.symmetricmatrix.MutableDoubleSymmetricMatrix
import org.danilopianini.util.ListSet
import java.io.Serializable

/**
 * Interface for an environment.
 * Every environment must implement this specification.
 * [T] is the [Concentration] type, [P] is the [Position] type.
 */
interface Environment<T, P : Position<out P>> :
    Serializable,
    Iterable<Node<T>> {
    /**
     * Add a [Layer] to the [Environment].
     */
    fun addLayer(
        molecule: Molecule,
        layer: Layer<T, P>,
    )

    /**
     * Add a [GlobalReaction] to the [Environment].
     */
    fun addGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Remove a [GlobalReaction] from the [Environment].
     */
    fun removeGlobalReaction(reaction: GlobalReaction<T>)

    /**
     * Get the [Environment]'s [GlobalReaction]s.
     */
    val globalReactions: ListSet<GlobalReaction<T>>

    /**
     * This method allows to add a new [node] to this environment in a specific [position].
     * The environment is responsible to call the right method of the simulation in order to
     * ensure that the reaction is properly scheduled.
     * The function returns true if the node is added to the environment.
     */
    fun addNode(
        node: Node<T>,
        position: P,
    ): Boolean

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     */
    fun addTerminator(terminator: TerminationPredicate<T, P>)

    /**
     * Add a [terminator] indicating whether the simulation should be considered finished.
     */
    fun addTerminator(terminator: (Environment<T, P>) -> Boolean) = addTerminator(TerminationPredicate(terminator))

    /**
     * The number of dimensions of this environment.
     */
    val dimensions: Int

    /**
     * Measures the distance between two nodes  ([n1], [n2]) in the environment.
     */
    fun getDistanceBetweenNodes(
        n1: Node<T>,
        n2: Node<T>,
    ): Double

    /**
     * Return the [Incarnation] used to initialize the entities of this [Environment], if it has been set.
     */
    val incarnation: Incarnation<T, P>

    /**
     * Get the [Layer] associate to the given [molecule]. If no Layer is associated
     * with the given molecule, return `null`.
     */
    fun getLayer(molecule: Molecule): Layer<T, P>?

    /**
     * Return all the Layers in this [Environment].
     */
    val layers: ListSet<Layer<T, P>>

    /**
     * Returns the current [LinkingRule].
     */
    var linkingRule: LinkingRule<T, P>

    /**
     * Given a [node], this method returns its neighborhood.
     */
    fun getNeighborhood(node: Node<T>): Neighborhood<T>

    /**
     * Allows to access a [Node] in this [Environment] known its [id].
     * Depending on the implementation, this method may or not be optimized
     * (namely, id could run in constant or linear time with the number of nodes).
     */
    fun getNodeByID(id: Int): Node<T>

    /**
     * Returns all the [Node]s that exist in current [Environment].
     */
    val nodes: ListSet<Node<T>>

    /**
     * Returns the number of [Node]s currently in the [Environment].
     */
    val nodeCount: Int

    /**
     * Given a [node] this method returns a list of all the surroundings
     * nodes within the given [range]. Note that this method (depending on the
     * implementation) might be not optimized, and it's consequently **much**
     * better to use [Environment.getNeighborhood] and filter the
     * neighborhood if you are sure that all the nodes within the range are
     * connected to the center.
     */
    fun getNodesWithinRange(
        node: Node<T>,
        range: Double,
    ): ListSet<Node<T>>

    /**
     * Given a [position] this method returns a list of all the
     * surroundings nodes within the given [range]. Note that this method
     * (depending on the implementation) might be not optimized.
     */
    fun getNodesWithinRange(
        position: P,
        range: Double,
    ): ListSet<Node<T>>

    /**
     * This method allows to know which are the smallest coordinates represented.
     * Return an array of length [dimensions] containing the smallest
     * coordinates for each dimension.
     */
    val offset: DoubleArray

    /**
     * Calculates the position of a [node].
     */
    fun getPosition(node: Node<T>): P

    /**
     * Return the current [Simulation], if present, or throws an [IllegalStateException] otherwise.
     */
    var simulation: Simulation<T, P>

    /**
     * Return the current [Simulation], if present, or `null` otherwise.
     */
    val simulationOrNull: Simulation<T, P>?

    /**
     * The size of the environment as an array of length getDimensions().
     * This method must return distance measured with the same unit used by the positions.
     * No non-euclidean distance metrics are allowed.
     */
    val size: DoubleArray

    /**
     * This method returns the size of the environment as an array of length
     * [.getDimensions]. This method must return distance measured with
     * the same unit used for measuring distances. It may or may not return the
     * same result of [.getSize].
     */
    val sizeInDistanceUnits: DoubleArray

    /**
     * Return true if all the terminators are true.
     */
    val isTerminated: Boolean

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(vararg coordinates: Number): P

    /**
     * Given the [coordinates] of the point,
     * returns a [Position] compatible with this environment.
     */
    fun makePosition(coordinates: List<Number>): P = makePosition(*coordinates.toTypedArray())

    /**
     * This method moves a [node] in the environment to some [position].
     * If node move is unsupported, it does nothing.
     */
    fun moveNodeToPosition(
        node: Node<T>,
        position: P,
    )

    /**
     * This method allows to remove a [node].
     * If node removal is unsupported, it does nothing.
     */
    fun removeNode(node: Node<T>)

    /**
     * Computes the diameter of the subnetworks of the environment.
     * The diameter is the longest shortest path between any two nodes.
     * Returns a [Set] containing the [Subnetwork]s.
     */
    fun allDiameters(): Set<Subnetwork<T>> {
        data class SubNetwork<T>(override val nodes: Set<Node<T>>, override val diameter: Double) : Subnetwork<T>
        val subnets = mutableSetOf<SubNetwork<T>>()
        val paths = shortestHopPaths()
        val visited = nodes.toMutableSet()
        paths.forEach { (n1, n2), diameter ->
            val subnetwork = subnets.find { s -> s.contains(n1) || s.contains(n2) }
            val newSubnet = when {
                subnetwork != null -> { // already exists a subnetwork containing n1 or n2
                    val newNodes = subnetwork.merge(n1, n2)
                    val newDiameter = if (subnetwork.diameter > diameter) subnetwork.diameter else diameter
                    subnets.remove(subnetwork)
                    SubNetwork(newNodes, newDiameter)
                }
                else -> SubNetwork(setOf(n1, n2), diameter)
            }
            visited.removeAll(setOf(n1, n2))
            subnets.add(newSubnet)
        }
        if (visited.isNotEmpty()) {
            visited.forEach { n -> subnets.add(SubNetwork(setOf(n), 0.0)) }
        }
        return subnets
    }

    fun shortestHopPaths() = shortestPaths { n1, n2 ->
        when {
            n1 == n2 -> 0.0
            getNeighborhood(n1).contains(n2) -> 1.0
            else -> Double.POSITIVE_INFINITY
        }
    }

    /**
     * Computes all the minimum distances using the Floyd–Warshall algorithm
     */
    fun shortestPaths(
        computeDistance: (Node<T>, Node<T>) -> Double = { n1, n2 ->
            when {
                n1 == n2 -> 0.0
                getNeighborhood(n1).contains(n2) -> getDistanceBetweenNodes(n1, n2)
                else -> Double.POSITIVE_INFINITY
            }
        }
    ): Map<Pair<Node<T>, Node<T>>, Double> {
        val nodes = nodes.toList()
        /*
         * The distances matrix is a triangular matrix stored in a flat array.
         */
        val distances = MutableDoubleSymmetricMatrix(nodeCount)
        for (i in 0 until nodeCount) {
            for (j in i + 1 until nodeCount) {
                distances[i, j] = computeDistance(nodes[i], nodes[j])
            }
        }
        val result = LinkedHashMap<Pair<Node<T>, Node<T>>, Double>(nodes.size * (nodes.size + 1) / 2, 1.0f)
        for (cycle in 0 until nodeCount) {
            for (i in 0 until nodeCount) {
                for (j in i + 1 until nodeCount) {
                    if (distances[i, j] > distances[i, cycle] + distances[cycle, j]) {
                        distances[i, j] = distances[i, cycle] + distances[cycle, j]
                    }
                    if (distances[i, j] != Double.POSITIVE_INFINITY) {
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
    val isNetworkSegmented: Boolean
        get() = allDiameters().size > 1

    /**
     * Computes the network diameter of the segment containing [node].
     */
    fun networkDiameter(node: Node<T>): Double =
        allDiameters().find { it.nodes.contains(node)}?.diameter ?: 0.0
//        (getNeighborhood(node).map { n -> bfs(n).values.max() } + bfs(node).values.max()).maxOrNull() ?: 0

    /**
     * Performs a breadth-first search (BFS) starting from a [start] node.
     * Computes the shortest distance from the start node to all other reachable nodes.
     */
    private fun bfs(start: Node<T>): Map<Node<T>, Int> {
        val distances: MutableMap<Node<T>, Int> = mutableMapOf(start to 0)
        val queue: ArrayDeque<Node<T>> = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val currentDistance = distances[current] ?: 0
            for (neighbor in getNeighborhood(current)) {
                if (neighbor !in distances) {
                    distances[neighbor] = currentDistance + 1
                    queue.add(neighbor)
                }
            }
        }
        return distances
    }

    /**
     * The [nodes] inside a subnetwork and relative [diameter].
     */
    interface Subnetwork<T> {
        /**
         * The nodes that belongs to this [Subnetwork].
         */
        val nodes: Set<Node<T>>

        /**
         * The diameter of the [Subnetwork].
         */
        val diameter: Double

        /**
         * Returns true whether the [Subnetwork] contains the [node] passed as input.
         */
        fun contains(node: Node<T>): Boolean = nodes.contains(node)

        fun merge(other: Set<Node<T>>): Set<Node<T>> =
            nodes.toMutableSet().also { it.addAll(other)}

        fun merge(vararg other: Node<T>): Set<Node<T>> =
            nodes.toMutableSet().also { it.addAll(other)}
    }
}
