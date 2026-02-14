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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.util.Environments.allSubNetworksByNodeWithHopDistance
import it.unibo.alchemist.util.Environments.allSubNetworksWithHopDistance
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.Test

object TestEnvironmentsDiameterWithHopDistance {
    private infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksWithHopDistance().size)

    private fun <T> Environment<T, *>.diameterOfSubnetworkWithNode(id: Int): Double =
        diameterOfSubnetworkWithNode(getNodeByID(id))

    private fun <T> Environment<T, *>.diameterOfSubnetworkWithNode(node: Node<T>) =
        allSubNetworksByNodeWithHopDistance().getValue(node).diameter

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveHopDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        assertEquals(expected, allSubNetworksWithHopDistance().single().diameter)
    }

    @Test
    fun `environment with a single node have diameter 0`() =
        singleNodeEnvironment() mustNotBeSegmentedAndHaveHopDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 1`() = twoConnectedNodes mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `a triangle has hop diameter 1`() = nodesInATriangle mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `three nodes in a row have hop diameter 2`() = threeNodesInARow mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes connected in a square should have hop diameter 2`() =
        fourNodesInASquare mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
        fourNodesInATriangle mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        with(twoConnectedNodesAndOneIsolated) {
            mustBeSegmented()
            mustHaveCoherentSubnetworks()
            mustHave(2.subnetworks())
            assertEquals(1.0, diameterOfSubnetworkWithNode(0))
            assertEquals(1.0, diameterOfSubnetworkWithNode(1))
            assertEquals(0.0, diameterOfSubnetworkWithNode(2))
        }
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        with(twoSubnetworksWithTwoNodesEach) {
            mustBeSegmented()
            mustHaveCoherentSubnetworks()
            mustHave(2.subnetworks())
            nodes.forEach {
                assertEquals(1.0, diameterOfSubnetworkWithNode(it))
            }
        }
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() {
        with(singleNodeEnvironment()) {
            mustNotBeSegmentedAndHaveHopDiameter(expected = 0.0)
            addNodeAt(1.0 to 4.0)
            mustNotBeSegmentedAndHaveHopDiameter(expected = 1.0)
            addNodeAt(4.0 to -2.0)
            mustNotBeSegmentedAndHaveHopDiameter(expected = 2.0)
        }
    }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        with(twoSparseSubnetworks) {
            mustBeSegmented()
            mustHaveCoherentSubnetworks()
            mustHave(2.subnetworks())
        }
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        with(threeSparseSubnetworks) {
            mustBeSegmented()
            mustHaveCoherentSubnetworks()
            mustHave(3.subnetworks())
            assertEquals(2.0, diameterOfSubnetworkWithNode(nodes.first()))
            assertEquals(0.0, diameterOfSubnetworkWithNode(nodes.last()))
        }
    }
}
