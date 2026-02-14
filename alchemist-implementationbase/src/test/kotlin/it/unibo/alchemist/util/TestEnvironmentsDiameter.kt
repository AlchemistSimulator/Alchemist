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
import it.unibo.alchemist.util.Environments.allSubNetworks
import it.unibo.alchemist.util.Environments.allSubNetworksByNode
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import org.junit.jupiter.api.Test

object TestEnvironmentsDiameter {

    private const val EXPECTED_DIAMETER_LONG = 8.48528137423857
    private const val EXPECTED_DIAMETER_SHORT = 6.324555320336759

    private fun <T> Environment<T, *>.subnetworksDiametersShouldBe(diameters: List<Double>) {
        assertEquals(diameters.sorted(), allSubNetworks().map { it.diameter }.sorted())
    }

    private fun <T> Environment<T, *>.subnetworksDiametersShouldBe(vararg diameters: Double) =
        subnetworksDiametersShouldBe(diameters.toList())

    private fun <T> Environment<T, *>.networkDiameterShouldBe(diameter: Double) =
        subnetworksDiametersShouldBe(listOf(diameter))

    @Test
    fun `environments with a single node have diameter 0`() =
        singleNodeEnvironment() mustNotBeSegmentedAndHaveDiameter 0.0

    @Test
    fun `two connected nodes should have diameter 3`() = twoConnectedNodes mustNotBeSegmentedAndHaveDiameter 3.0

    @Test
    fun `a triangle formation network is not segmented`() =
        nodesInATriangle mustNotBeSegmentedAndHaveDiameter 3 * sqrt(2.0)

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
            subnetworksDiametersShouldBe(listOf(0.0, 3.0))
        }
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        with(twoSubnetworksWithTwoNodesEach) {
            mustBeSegmented()
            mustHave(2.subnetworks())
            subnetworksDiametersShouldBe(listOf(3.0, 3.0))
        }
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() =
        with(singleNodeEnvironment()) {
            networkDiameterShouldBe(0.0)
            addNodeAt(1.0 to 1.0)
            networkDiameterShouldBe(hypot(1.0, 1.0))
            addNodeAt(-1.0 to -1.0)
            networkDiameterShouldBe(2 * sqrt(2.0))
        }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        with(twoSparseSubnetworks) {
            mustBeSegmented()
            mustHave(2.subnetworks())
            subnetworksDiametersShouldBe(EXPECTED_DIAMETER_SHORT, EXPECTED_DIAMETER_LONG)
        }
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        with(threeSparseSubnetworks) {
            mustBeSegmented()
            mustHave(3.subnetworks())
            subnetworksDiametersShouldBe(EXPECTED_DIAMETER_SHORT, EXPECTED_DIAMETER_LONG, 0.0)
        }
    }
}
