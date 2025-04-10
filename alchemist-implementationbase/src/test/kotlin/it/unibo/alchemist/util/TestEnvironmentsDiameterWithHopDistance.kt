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
import it.unibo.alchemist.util.Environments.allSubNetworksByNodeWithHopDistance
import it.unibo.alchemist.util.Environments.allSubNetworksWithHopDistance
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

object TestEnvironmentsDiameterWithHopDistance {
    private infix fun <T> Environment<T, *>.withHopDistanceMustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksWithHopDistance().size)

    private fun <T> Environment<T, *>.specificNodeInASegmentedNetworkShouldHaveHopDiameter(index: Int, expected: Double) =
        {
            require(index < nodes.size)
            assertEquals<Double>(expected, allSubNetworksByNodeWithHopDistance()[nodes[index]]?.diameter!!)
        }

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveHopDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        assertEquals<Double>(expected, allSubNetworksWithHopDistance().single().diameter)
    }

    @Test
    fun `environments with a single node have diameter 0`() =
        singleNodeEnvironment mustNotBeSegmentedAndHaveHopDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 1`() =
         twoConnectedNodes mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `a triangle has hop diameter 1`() =
        nodesInATriangle mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `three nodes in a row have hop diameter 2`() =
        threeNodesInARow mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes connected in a square should have hop diameter 2`() =
         fourNodesInASquare mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
         fourNodesInATriangle mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        val environment = twoConnectedNodesAndOneIsolated
        environment.mustBeSegmented()
        environment withHopDistanceMustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(2, 0.0)
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        val environment = twoSubnetworksWithTwoNodesEach
        environment.mustBeSegmented()
        environment withHopDistanceMustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(2, 1.0)
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() {
        val environment = singleNodeEnvironment
        environment mustNotBeSegmentedAndHaveHopDiameter 0.0
        environment addNodeAt (1.0 to 4.0)
        environment mustNotBeSegmentedAndHaveHopDiameter 1.0
        environment addNodeAt (4.0 to -2.0)
        environment mustNotBeSegmentedAndHaveHopDiameter 2.0
    }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        val environment = twoSparseSubnetworks
        environment.mustBeSegmented()
        environment withHopDistanceMustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(0, 2.0)
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(1, 1.0)
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        val environment = threeSparseSubnetworks
        environment.mustBeSegmented()
        environment withHopDistanceMustHave 3.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(0, 2.0)
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(1, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveHopDiameter(environment.nodeCount - 1, 0.0)
    }
}
