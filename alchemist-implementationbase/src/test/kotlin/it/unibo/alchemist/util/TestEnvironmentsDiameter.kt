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
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.Test

object TestEnvironmentsDiameter {
    private infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksByNode().size)

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        assertEquals<Double>(
            expected,
            allSubNetworksByNode().values.single().diameter.roundToTwoDecimals(),
        )
    }

    private fun <T> Environment<T, *>.specificNodeInASegmentedNetworkShouldHaveDiameter(index: Int, expected: Double) {
        require(index < nodes.size)
        val diameter = allSubNetworksByNode()[nodes[index]]?.diameter
        if (diameter != null) {
            assertEquals<Double>(
                expected,
                diameter.roundToTwoDecimals(),
            )
        }
    }

    private fun Double.roundToTwoDecimals(): Double = BigDecimal(this).setScale(2, RoundingMode.HALF_UP).toDouble()

    @Test
    fun `environments with a single node have diameter 0`() =
        singleNodeEnvironment() mustNotBeSegmentedAndHaveDiameter 0.0

    @Test
    fun `two connected nodes should have diameter 3`() = twoConnectedNodes mustNotBeSegmentedAndHaveDiameter 3.0

    @Test
    fun `a triangle formation network is not segmented`() = nodesInATriangle mustNotBeSegmentedAndHaveDiameter 4.24

    @Test
    fun `three nodes in a row have diameter 8`() = threeNodesInARow mustNotBeSegmentedAndHaveDiameter 8.0

    @Test
    fun `four nodes connected in a square should have diameter 10`() =
        fourNodesInASquare mustNotBeSegmentedAndHaveDiameter 10.0

    @Test
    fun `four nodes in a triangle should have diameter 6`() = fourNodesInATriangle mustNotBeSegmentedAndHaveDiameter 6.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        with(twoConnectedNodesAndOneIsolated) {
            mustBeSegmented()
            mustHave(2.subnetworks())
            specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
            specificNodeInASegmentedNetworkShouldHaveDiameter(2, 0.0)
        }
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        with(twoSubnetworksWithTwoNodesEach) {
            mustBeSegmented()
            mustHave(2.subnetworks())
            specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
            specificNodeInASegmentedNetworkShouldHaveDiameter(2, 3.0)
        }
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() =
        with(singleNodeEnvironment()) {
            mustNotBeSegmentedAndHaveDiameter(expected = 0.0)
            addNodeAt(1.0 to 4.0)
            mustNotBeSegmentedAndHaveDiameter(expected = 4.12)
            addNodeAt(-4.0 to -2.0)
            mustNotBeSegmentedAndHaveDiameter(expected = 8.60)
        }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        with(twoSparseSubnetworks) {
            mustBeSegmented()
            mustHave(2.subnetworks())
            specificNodeInASegmentedNetworkShouldHaveDiameter(0, 8.49)
            specificNodeInASegmentedNetworkShouldHaveDiameter(1, 6.32)
        }
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        with(threeSparseSubnetworks) {
            mustBeSegmented()
            mustHave(3.subnetworks())
            specificNodeInASegmentedNetworkShouldHaveDiameter(0, 8.49)
            specificNodeInASegmentedNetworkShouldHaveDiameter(1, 6.32)
            specificNodeInASegmentedNetworkShouldHaveDiameter(nodeCount - 1, 0.0)
        }
    }
}
