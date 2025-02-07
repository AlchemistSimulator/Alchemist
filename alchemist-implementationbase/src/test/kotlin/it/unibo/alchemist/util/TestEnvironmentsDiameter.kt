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
import it.unibo.alchemist.util.Environments.networkDiameter
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

object TestEnvironmentsDiameter {
    val ORIGIN = 0.0 to 0.0

    private infix fun Environment<Any, Euclidean2DPosition>.addNodeAt(coordinates: Pair<Double, Double>) =
        addNode(
            GenericNode(ProtelisIncarnation(), this),
            Euclidean2DPosition(coordinates.first, coordinates.second),
        )

    private fun environmentWithNodesAt(vararg positions: Pair<Double, Double>) =
        Continuous2DEnvironment(ProtelisIncarnation()).apply {
            linkingRule = ConnectWithinDistance(5.0)
            positions.forEach { addNodeAt(it) }
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

    private infix fun <T> Environment<T, *>.mustHave(expected: Subnetworks) =
        assertEquals<Int>(expected.count, allSubNetworksWithHopDistance().size)

    fun Int.subnetworks(): Subnetworks = Subnetworks(this)

    @JvmInline
    value class Subnetworks(val count: Int)

    private fun <T> Environment<T, *>.specificNodeInASegmentedNetworkShouldHaveDiameter(
        index: Int,
        expected: Double,
    ) = {
        require(index < nodes.size)
        assertEquals<Double>(expected, allSubNetworksByNodeWithHopDistance()[nodes[index]]?.diameter!!)
    }

    @Test
    fun `environments with a single node have diameter 0`() =
        environmentWithNodesAt(ORIGIN) mustNotBeSegmentedAndHaveHopDiameter 0.0

    @Test
    fun `two connected nodes should have hop diameter 1`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `a triangle has hop diameter 1`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 0.0 to 3.0) mustNotBeSegmentedAndHaveHopDiameter 1.0

    @Test
    fun `three nodes in a row have hop diameter 2`() =
        environmentWithNodesAt(ORIGIN, 4.0 to 0.0, 8.0 to 0.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes connected in a square should have hop diameter 2`() =
        environmentWithNodesAt(ORIGIN, 5.0 to 0.0, 0.0 to 5.0, 5.0 to 5.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `four nodes in a triangle should have hop diameter 2`() =
        environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 6.0 to 0.0, 3.0 to 2.0) mustNotBeSegmentedAndHaveHopDiameter 2.0

    @Test
    fun `a network of three nodes with one isolated should be considered segmented`() {
        val environment = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0)
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 0.0)
    }

    @Test
    fun `a network of four nodes connected by two should be considered segmented with the same diameter`() {
        val environment = environmentWithNodesAt(ORIGIN, 3.0 to 0.0, 10.0 to 0.0, 10.0 to 3.0)
        environment.mustBeSegmented()
        environment mustHave 2.subnetworks()
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 1.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(2, 1.0)
    }

    @Test
    fun `a network of three nodes added dynamically and not in order should adapt accordingly`() {
        val environment = environmentWithNodesAt(ORIGIN)
        environment mustNotBeSegmentedAndHaveHopDiameter 0.0
        environment addNodeAt (1.0 to 4.0)
        environment mustNotBeSegmentedAndHaveHopDiameter 1.0
        environment addNodeAt (4.0 to -2.0)
        environment mustNotBeSegmentedAndHaveHopDiameter 2.0
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
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(0, 2.0)
        environment.specificNodeInASegmentedNetworkShouldHaveDiameter(1, 1.0)
    }
}
