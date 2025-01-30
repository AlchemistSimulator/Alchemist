/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import kotlin.test.BeforeTest
import kotlin.test.Test
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.physics.environments.ContinuousPhysics2DEnvironment
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.physics.properties.CircularArea
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.Double.Companion.NaN
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestDiameter {
    private lateinit var env: Physics2DEnvironment<Any>
    private lateinit var node1: Node<Any>
    private lateinit var node2: Node<Any>
    private lateinit var node3: Node<Any>
    private lateinit var node4: Node<Any>

    private fun createCircleNode(
        incarnation: Incarnation<Any, Euclidean2DPosition>,
        environment: Physics2DEnvironment<Any>,
        radius: Double,
    ) = GenericNode(incarnation, environment).apply {
        addProperty(CircularArea(environment, this, radius))
    }

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
            assertEquals(expected, env.networkDiameter(node))
        }
        env.allHopDiameters().forEach { subnetwork ->
            assertEquals<Double>(expected, subnetwork.diameter)
        }
    }

    @BeforeTest
    fun beforeTest() {
        val incarnation = SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()
        env = ContinuousPhysics2DEnvironment(incarnation)
        env.linkingRule = ConnectWithinDistance(5.0)
        node1 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node2 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node3 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
        node4 = createCircleNode(incarnation, env, DEFAULT_SHAPE_SIZE / 2)
    }

    @Test
    fun testSingleNodeDiameter() {
        addNodeToEnv(node1 to Euclidean2DPosition(0.0, 0.0))
        verifyUnifiedNetworkDiameter(expected = NaN, node1)
    }

    @Test
    fun testTwoConnectedNodesDiameter() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        verifyUnifiedNetworkDiameter(expected = 1.0, node1, node2)
    }

    @Test
    fun testThreeFullyConnectedNodesDiameter() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(0.0, 3 * DEFAULT_SHAPE_SIZE),
        )
        env.allHopDiameters()
        verifyUnifiedNetworkDiameter(expected = 1.0, node1, node2, node3)
    }

    @Test
    fun testThreeNodesInRow() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        verifyUnifiedNetworkDiameter(expected = 2.0, node1, node2, node3)
    }

    @Test
    fun testFourConnectedNodesDiameter() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
            node4 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 2 * DEFAULT_SHAPE_SIZE),
        )
        verifyUnifiedNetworkDiameter(expected = 2.0, node1, node2, node3, node4)
    }

    @Test
    fun testIsolatedNodeDiameter() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        assertEquals(3, env.nodes.size)
        println(env.allHopDiameters())
        assertTrue(env.isNetworkSegmented)
        assertEquals(1.0, env.networkDiameter(node1))
        assertEquals(1.0, env.networkDiameter(node2))
        assertEquals(NaN, env.networkDiameter(node3))
        val subnetworks = env.allHopDiameters()
        assertEquals(2, subnetworks.size)
        assertEquals(1.0, subnetworks.find { it.contains(node1) }?.diameter)
        assertEquals(NaN, subnetworks.find { it.contains(node3) }?.diameter)
    }

    @Test
    fun testTwoSubnetworksWithSameDiameter() {
        addNodeToEnv(
            node1 to Euclidean2DPosition(0.0, 0.0),
            node2 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 0.0),
            node4 to Euclidean2DPosition(10 * DEFAULT_SHAPE_SIZE, 3.0),
        )
        assertEquals(4, env.nodes.size)
        println(env.allHopDiameters())
        assertTrue(env.isNetworkSegmented)
        assertEquals(1.0, env.networkDiameter(node1))
        assertEquals(1.0, env.networkDiameter(node2))
        assertEquals(1.0, env.networkDiameter(node3))
        assertEquals(1.0, env.networkDiameter(node4))
        val subnetworks = env.allHopDiameters()
        assertEquals(2, subnetworks.size)
        assertEquals(1.0, subnetworks.find { it.contains(node1) }?.diameter)
        assertEquals(1.0, subnetworks.find { it.contains(node3) }?.diameter)
    }

    companion object {
        private const val DEFAULT_SHAPE_SIZE: Double = 1.0
    }
}
