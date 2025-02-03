/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import io.kotest.mpp.env
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.protelis.ProtelisIncarnation
import it.unibo.alchemist.util.Environments.allSubNetworksWithHopDistance
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance
import it.unibo.alchemist.util.Environments.networkDiameter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestEnvironmentsDiameter {

    private fun environmentWithNodesAt(vararg positions: Pair<Double, Double>) =
        Continuous2DEnvironment(ProtelisIncarnation()).apply {
            linkingRule = ConnectWithinDistance(5.0)
            positions.forEach { (x, y) -> addNode(GenericNode(ProtelisIncarnation(), this), Euclidean2DPosition(x, y)) }
        }

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveHopDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        assertEquals<Double>(expected, allSubNetworksWithHopDistance().single().diameter)
    }

    private fun <T> Environment<T, *>.mustBeSegmented() {
        assertTrue(isNetworkSegmented())
        assertTrue(networkDiameter().isNaN())
        assertTrue(networkDiameterByHopDistance().isNaN())
    }

    @Test
    fun `environments with a single node have diameter 0`() {
        environmentWithNodesAt(0.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 0.0
    }

    @Test
    fun `two connected nodes should have hop diameter 1`() {
        environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter(1.0)
    }

    @Test
    fun `a triangle has hop diameter 1`() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(0.0, 3 * DEFAULT_SHAPE_SIZE),
        )
        env.allHopDiameters()
        mustNotBeSegmentedAndHaveHopDiameter(expected = 1.0, node0, node1, node2)
    }

    @Test
    fun testThreeNodesInRow() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(4 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(8 * DEFAULT_SHAPE_SIZE, 0.0),
        )
        mustNotBeSegmentedAndHaveHopDiameter(expected = 2.0, node0, node1, node2)
    }

    @Test
    fun testFourNodesWithTwoNeighbors() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(5 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(0.0, 5 * DEFAULT_SHAPE_SIZE),
            node3 to Euclidean2DPosition(5 * DEFAULT_SHAPE_SIZE, 5 * DEFAULT_SHAPE_SIZE),
        )
        mustNotBeSegmentedAndHaveHopDiameter(expected = 2.0, node0, node1, node2, node3)
    }

    @Test
    fun testFourNodesWithThreeNeighbors() {
        addNodeToEnv(
            node0 to Euclidean2DPosition(0.0, 0.0),
            node1 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 0.0),
            node2 to Euclidean2DPosition(6 * DEFAULT_SHAPE_SIZE, 0.0),
            node3 to Euclidean2DPosition(3 * DEFAULT_SHAPE_SIZE, 2 * DEFAULT_SHAPE_SIZE),
        )
        mustNotBeSegmentedAndHaveHopDiameter(expected = 2.0, node0, node1, node2, node3)
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
        assertEquals(Double.Companion.NaN, env.networkDiameterByHopDistance(node2))
        val subnetworks = env.allHopDiameters()
        assertEquals(2, subnetworks.size)
        assertEquals(1.0, subnetworks.find { it.contains(node0) }?.diameter)
        assertEquals(Double.Companion.NaN, subnetworks.find { it.contains(node2) }?.diameter)
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
