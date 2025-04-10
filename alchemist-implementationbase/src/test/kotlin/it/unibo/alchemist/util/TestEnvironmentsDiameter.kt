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
import it.unibo.alchemist.util.Environments.allSubNetworksByNode
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

object TestEnvironmentsDiameter {
    private infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksByNode().size)

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        // round to the first two decimals
        assertEquals<Double>(
            expected,
            String.format("%.2f", allSubNetworksByNode().values.single().diameter).toDouble(),
        )
    }
    
    private fun <T> Environment<T, *>.specificNodeInASegmentedNetworkShouldHaveDiameter(index: Int, expected: Double) =
        {
            require(index < nodes.size)
            // round to the first two decimals
            assertEquals<Double>(
                expected,
                String.format("%.2f", allSubNetworksByNode()[nodes[index]]?.diameter!!).toDouble(),
            )
        }

    @Test
    fun `environments with a single node have diameter 0`() =
        singleNodeEnvironment mustNotBeSegmentedAndHaveDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 3`() =
        twoConnectedNodes mustNotBeSegmentedAndHaveDiameter 3.0

    @Test
    fun `a triangle has diameter 1`() =
        nodesInATriangle mustNotBeSegmentedAndHaveDiameter 4.24

    @Test
    fun `three nodes in a row have diameter 8`() =
        threeNodesInARow mustNotBeSegmentedAndHaveDiameter 8.0

    @Test
    fun `four nodes connected in a square should have diameter 2`() =
       fourNodesInASquare mustNotBeSegmentedAndHaveDiameter 10.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
        fourNodesInATriangle mustNotBeSegmentedAndHaveDiameter 6.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        val environment = twoConnectedNodesAndOneIsolated
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 0.0)
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        val environment = twoSubnetworksWithTwoNodesEach
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 3.0)
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() {
        val environment = singleNodeEnvironment
        environment mustNotBeSegmentedAndHaveDiameter 0.0
        environment addNodeAt (1.0 to 4.0)
        environment mustNotBeSegmentedAndHaveDiameter 4.12
        environment addNodeAt (-4.0 to -2.0)
        environment mustNotBeSegmentedAndHaveDiameter 8.60
    }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        val environment = twoSparseSubnetworks
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 5.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(1, 10.0)
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        val environment = threeSparseSubnetworks
        environment.mustBeSegmented()
        environment mustHave 3.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 5.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(1, 10.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(environment.nodeCount - 1, 0.0)
    }
}
