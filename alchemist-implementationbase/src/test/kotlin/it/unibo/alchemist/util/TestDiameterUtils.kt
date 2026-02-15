/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Environments.allSubNetworksByNode
import it.unibo.alchemist.util.Environments.allSubNetworksByNodeWithHopDistance
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import it.unibo.alchemist.util.Environments.networkDiameter
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Adds a node to the environment at the specified [coordinates].
 */
infix fun Environment<Any, Euclidean2DPosition>.addNodeAt(coordinates: Pair<Double, Double>) = addNode(
    GenericNode(this),
    Euclidean2DPosition(coordinates.first, coordinates.second),
)

/**
 * Creates a new [Continuous2DEnvironment] with nodes at the specified [positions].
 */
fun environmentWithNodesAt(vararg positions: Pair<Double, Double>) =
    Continuous2DEnvironment(ProtelisIncarnation()).apply {
        linkingRule = ConnectWithinDistance(5.0)
        positions.forEach { addNodeAt(it) }
    }

/**
 * Asserts that the environment is segmented and has a NaN network diameter.
 */
fun <T> Environment<T, *>.mustBeSegmented() {
    assertTrue(isNetworkSegmented())
    assertTrue(networkDiameter().isNaN())
}

/**
 *
 */
fun <T> Environment<T, *>.mustHaveCoherentSubnetworks() {
    val networks = allSubNetworksByNodeWithHopDistance()
    assertEquals(nodes.size, networks.size)
    assertEquals(nodes.sorted(), networks.values.flatMap { it.nodes }.distinct().sorted())
    networks.forEach { (pivot, network) ->
        network.nodes.forEach { connectedNode ->
            assertContains(networks[connectedNode]?.nodes.orEmpty(), pivot)
        }
    }
}

/**
 * Creates a [Subnetworks] object with the specified [count].
 */
fun Int.subnetworks(): Subnetworks = Subnetworks(this)

/**
 * Represents the number of subnetworks in an environment.
 */
@JvmInline
value class Subnetworks(val count: Int)

/**
 * The origin of the coordinate system.
 */
val ORIGIN = 0.0 to 0.0

/**
 * Represents a network composed of one node.
 */
fun singleNodeEnvironment() = environmentWithNodesAt(ORIGIN)

/**
 * Represents a network composed of two connected nodes.
 */
val twoConnectedNodes = environmentWithNodesAt(ORIGIN, 3.0 to 0.0)

/**
 * Represents a network composed of three nodes in a triangle formation.
 */
val nodesInATriangle = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 0.0 to 3.0)

/**
 * Represents a network composed of three nodes in a row,
 * with just the central node that has two neighbors.
 */
val threeNodesInARow = environmentWithNodesAt(ORIGIN, 4.0 to 0.0, 8.0 to 0.0)

/**
 * Represents a network composed of four nodes in a square formation.
 */
val fourNodesInASquare = environmentWithNodesAt(ORIGIN, 5.0 to 0.0, 0.0 to 5.0, 5.0 to 5.0)

/**
 * Represents a network composed of four nodes in a triangle formation.
 */
val fourNodesInATriangle = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 6.0 to 0.0, 3.0 to 3.0)

/**
 * Represents a network composed of three nodes,
 * one of which is isolated from the others.
 */
val twoConnectedNodesAndOneIsolated = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0)

/**
 * Represents a network composed of two subnetworks,
 * each with two nodes.
 */
val twoSubnetworksWithTwoNodesEach = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0, 10.0 to 3.0)

private val positionsForTwoSubnets = arrayOf(
    -3.0 to 3.0,
    ORIGIN,
    0.0 to 6.0,
    3.0 to 3.0,
    9.0 to 15.0,
    12.0 to 12.0,
    12.0 to 14.0,
    15.0 to 15.0,
)

/**
 * Represents a network composed of two subnetworks,
 * each with different amount of nodes.
 */
val twoSparseSubnetworks = environmentWithNodesAt(*positionsForTwoSubnets)

/**
 * Represents a network composed of three subnetworks,
 * each with different amount of nodes.
 */
val threeSparseSubnetworks = environmentWithNodesAt(
    *positionsForTwoSubnets,
    25.0 to 25.0,
)

infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
    assertEquals(expected.count, allSubNetworksByNode().values.distinct().size)

infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveDiameter(expected: Double) {
    assertFalse(isNetworkSegmented())
    assertEquals(
        expected,
        allSubNetworksByNode().values.toSet().single().diameter,
        absoluteTolerance = 10e-12,
    )
}
