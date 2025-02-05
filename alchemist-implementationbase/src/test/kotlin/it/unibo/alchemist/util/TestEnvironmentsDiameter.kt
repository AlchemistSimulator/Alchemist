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
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.protelis.ProtelisIncarnation
import it.unibo.alchemist.util.Environments.allSubNetworksByNodeWithHopDistance
import it.unibo.alchemist.util.Environments.allSubNetworksWithHopDistance
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance
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
        assertTrue(networkDiameterByHopDistance().isNaN())
    }

    private infix fun <T> Environment<T, *>.mustHaveMoreSubnetworks(expected: Int) =
        assertEquals<Int>(expected, allSubNetworksWithHopDistance().size)

    private fun <T> Environment<T, *>.specificNodeInASegmentedNetworkShouldHaveDiameter(
        index: Int,
        expected: Double,
    ) = {
        require(index < nodes.size)
        assertEquals<Double>(expected, allSubNetworksByNodeWithHopDistance()[nodes[index]]?.diameter!!)
    }

    @Test
    fun `environments with a single node have diameter 0`() =
        environmentWithNodesAt(0.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 1`() =
        environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `a triangle has hop diameter 1`() =
        environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0, 0.0 to 3.0) mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `three nodes in a row have hop diameter 2`() =
        environmentWithNodesAt(0.0 to 0.0, 4.0 to 0.0, 8.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes connected in a square should have hop diameter 2`() =
        environmentWithNodesAt(0.0 to 0.0, 5.0 to 0.0, 0.0 to 5.0, 5.0 to 5.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
        environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0, 6.0 to 0.0, 3.0 to 2.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        val environment = environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0, 10.0 to 0.0)
        environment.mustBeSegmented()
        environment mustHaveMoreSubnetworks 2
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 0.0)
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        val environment = environmentWithNodesAt(0.0 to 0.0, 3.0 to 0.0, 10.0 to 0.0, 10.0 to 3.0)
        environment.mustBeSegmented()
        environment mustHaveMoreSubnetworks 2
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 1.0)
    }
}
