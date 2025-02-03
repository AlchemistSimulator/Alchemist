/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.Double.Companion.NaN
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDiameter {
    private lateinit var env: Euclidean2DEnvironment<Any>
    private lateinit var node0: Node<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>

    private fun createNode(
        incarnation: Incarnation<Any, Euclidean2DPosition>,
        environment: Euclidean2DEnvironment<Any>,
    ) = GenericNode(incarnation, environment)

    private fun addNodeToEnv(vararg nodePos: Pair<Node<Any>, Euclidean2DPosition>) {
        nodePos.forEach { (node, position) ->
            env.addNode(node, position)
        }
    }

    private fun verifyUnifiedNetworkDiameter(
        expected: Double,
        vararg nodes: Node<Any>,
    ) {
        assertFalse(env.isNetworkSegmented)
        assertEquals(nodes.size, env.nodes.size)
        nodes.forEach { node ->
            assertEquals(expected, env.networkDiameterByHopDistance(node))
        }
        env.allHopDiameters().forEach { subnetwork ->
            assertEquals<Double>(expected, subnetwork.diameter)
        }
    }

    @BeforeTest
    fun beforeTest() {
        val incarnation = SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()
        env = Continuous2DEnvironment(incarnation)
        env.linkingRule = ConnectWithinDistance(5.0)
        node0 = createNode(incarnation, env)
        node1 = createNode(incarnation, env)
        node2 = createNode(incarnation, env)
        node3 = createNode(incarnation, env)
    }

    @Test
    fun testSingleNodeDiameter() {
        addNodeToEnv(node0 to Euclidean2DPosition(0.0, 0.0))
        verifyUnifiedNetworkDiameter(expected = NaN, node0)
    }

    @Test
    fun testTwoConnectedNodesDiameter() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        verifyUnifiedNetworkDiameter(expected = 1.0, node0, node1)
    }

    @Test
    fun testThreeFullyConnectedNodesDiameter() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(0.0, 3 * DEFAULT_SHAPE_SIZE),
        )
        env.allHopDiameters()
        verifyUnifiedNetworkDiameter(expected = 1.0, node0, node1, node2)
    }

    @Test
    fun testThreeNodesInRow() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(4 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(8 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        verifyUnifiedNetworkDiameter(expected = 2.0, node0, node1, node2)
    }

    @Test
    fun testFourNodesWithTwoNeighbors() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(5 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(0.0, 5 * DEFAULT_SHAPE_SIZE),
            node3 to Euclidean2DPosition(5 * DEFAULT_SHAPE_SIZE, 5 * DEFAULT_SHAPE_SIZE),
        )
        verifyUnifiedNetworkDiameter(expected = 2.0, node0, node1, node2, node3)
    }

    @Test
    fun testFourNodesWithThreeNeighbors() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 2 * DEFAULT_SHAPE_SIZE),
        )
        verifyUnifiedNetworkDiameter(expected = 2.0, node0, node1, node2, node3)
    }

    @Test
    fun testIsolatedNodeDiameter() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        assertEquals(3, env.nodes.size)
        assertTrue(env.isNetworkSegmented)
        assertEquals(1.0, env.networkDiameterByHopDistance(node0))
        assertEquals(1.0, env.networkDiameterByHopDistance(node1))
        assertEquals(NaN, env.networkDiameterByHopDistance(node2))
        val subnetworks = env.allHopDiameters()
        assertEquals(2, subnetworks.size)
        assertEquals(1.0, subnetworks.find { it.contains(node0) }?.diameter)
        assertEquals(NaN, subnetworks.find { it.contains(node2) }?.diameter)
    }

    @Test
    fun testTwoSubnetworksWithSameDiameter() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 3.0),
        )
        assertEquals(4, env.nodes.size)
        assertTrue(env.isNetworkSegmented)
        assertEquals(1.0, env.networkDiameterByHopDistance(node0))
        assertEquals(1.0, env.networkDiameterByHopDistance(node1))
        assertEquals(1.0, env.networkDiameterByHopDistance(node2))
        assertEquals(1.0, env.networkDiameterByHopDistance(node3))
        val subnetworks = env.allHopDiameters()
        assertEquals(2, subnetworks.size)
        assertEquals(1.0, subnetworks.find { it.contains(node0) }?.diameter)
        assertEquals(1.0, subnetworks.find { it.contains(node2) }?.diameter)
    }

    companion object {
        private const val DEFAULT_SHAPE_SIZE: Double = 1.0
    }
}
