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
import it.unibo.alchemist.util.Environments.allSubNetworksByNode
import it.unibo.alchemist.util.Environments.isNetworkSegmented
import it.unibo.alchemist.util.Environments.networkDiameter
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object TestEnvironmentsDiameter {
    val ORIGIN = 0.0 to 0.0

    private infix fun Environment<Any, Euclidean2DPosition>.addNodeAt(coordinates: Pair<Double, Double>) = addNode(
        GenericNode(ProtelisIncarnation(), this),
        Euclidean2DPosition(coordinates.first, coordinates.second),
    )

    private fun environmentWithNodesAt(vararg positions: Pair<Double, Double>) =
        Continuous2DEnvironment(ProtelisIncarnation()).apply {
            linkingRule = ConnectWithinDistance(5.0)
            positions.forEach { addNodeAt(it) }
        }

    private infix fun <T> Environment<T, *>.mustNotBeSegmentedAndHaveDiameter(expected: Double) {
        assertFalse(isNetworkSegmented())
        // round to the first two decimals
        assertEquals<Double>(
            expected,
            String.format("%.2f", allSubNetworksByNode().values.single().diameter).toDouble(),
        )
    }

    private fun <T> Environment<T, *>.mustBeSegmented() {
        assertTrue(isNetworkSegmented())
        assertTrue(networkDiameter().isNaN())
    }

    private infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksByNode().size)

    fun Int.subnetworks(): Subnetworks = Subnetworks(this)

    @JvmInline
    value class Subnetworks(val count: Int)

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
        environmentWithNodesAt(ORIGIN) mustNotBeSegmentedAndHaveDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 3`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0) mustNotBeSegmentedAndHaveDiameter 3.0

    @Test
    fun `a triangle has diameter 1`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 0.0 to 3.0) mustNotBeSegmentedAndHaveDiameter 4.24

    @Test
    fun `three nodes in a row have diameter 8`() =
        environmentWithNodesAt(ORIGIN, 4.0 to 0.0, 8.0 to 0.0) mustNotBeSegmentedAndHaveDiameter 8.0

    @Test
    fun `four nodes connected in a square should have diameter 2`() =
        environmentWithNodesAt(ORIGIN, 5.0 to 0.0, 0.0 to 5.0, 5.0 to 5.0) mustNotBeSegmentedAndHaveDiameter 10.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 6.0 to 0.0, 3.0 to 3.0) mustNotBeSegmentedAndHaveDiameter 6.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        val environment = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0)
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 0.0)
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        val environment = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0, 10.0 to 3.0)
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 3.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 3.0)
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() {
        val environment = environmentWithNodesAt(ORIGIN)
        environment mustNotBeSegmentedAndHaveDiameter 0.0
        environment addNodeAt (1.0 to 4.0)
        environment mustNotBeSegmentedAndHaveDiameter 4.12
        environment addNodeAt (-4.0 to -2.0)
        environment mustNotBeSegmentedAndHaveDiameter 8.60
    }

    @Test
    fun `two sparse subnetworks should be considered segmented`() {
        val environment =
            environmentWithNodesAt(
                ORIGIN,
                12.0 to 12.0,
                0.0 to 6.0,
                12.0 to 14.0,
                -3.0 to 3.0,
                9.0 to 15.0,
                3.0 to 3.0,
                15.0 to 15.0,
            )
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 5.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(1, 10.0)
    }

    @Test
    fun `three sparse subnetworks should be considered segmented`() {
        val environment =
            environmentWithNodesAt(
                ORIGIN,
                12.0 to 12.0,
                0.0 to 6.0,
                12.0 to 14.0,
                -3.0 to 3.0,
                9.0 to 15.0,
                3.0 to 3.0,
                15.0 to 15.0,
                25.0 to 25.0,
            )
        environment.mustBeSegmented()
        environment mustHave 3.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 5.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(1, 10.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(environment.nodeCount - 1, 0.0)
    }
}
