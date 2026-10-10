/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * Tests for [SimpleNetworkArrivals].
 */
class TestSimpleNetworkArrivals {

    @Test
    fun `SimpleNetworkArrivals should compute rate correctly with constant values`() {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()
        every { environment.getNeighborhood(node).current.neighbors } returns persistentListOf()
        val propagationDelay = 0.1
        val packetSize = 1000.0
        val bandwidth = 1000.0
        val distribution = SimpleNetworkArrivals(
            incarnation = incarnation,
            node = node,
            environment = environment,
            propagationDelay = propagationDelay,
            packetSize = packetSize,
            bandwidth = bandwidth,
        )
        // Rate should be 1 / (propagationDelay + packetSize / bandwidth)
        val expectedDelay = propagationDelay + packetSize / bandwidth
        val expectedRate = 1.0 / expectedDelay
        assertEquals(expectedRate, distribution.expectedRate)
        assertEquals(expectedDelay, distribution.sample().toDouble())
    }

    @Test
    fun `SimpleNetworkArrivals should handle bandwidth calculation`() {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()
        every { environment.getNeighborhood(node).current.neighbors } returns persistentListOf()
        val distribution = SimpleNetworkArrivals(
            incarnation = incarnation,
            node = node,
            environment = environment,
            propagationDelay = 0.1,
            packetSize = 1000.0,
            bandwidth = 1000.0,
        )
        assertEquals(1000.0, distribution.bandwidth)
        assertEquals(1000.0, distribution.packetSize)
        assertEquals(0.1, distribution.propagationDelay)
    }

    @Test
    fun `molecule-driven parameters are read again at every sample`() {
        val node = mockk<Node<Any>>()
        val values = mutableMapOf(DELAY to 0.5, SIZE to 100.0, BANDWIDTH to 50.0)
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        every { incarnation.getProperty(node, any(), PROPERTY) } answers { values.getValue(secondArg()) }
        val distribution = SimpleNetworkArrivals(
            incarnation,
            mockk<Environment<Any, Euclidean2DPosition>>(),
            node,
            DELAY,
            PROPERTY,
            SIZE,
            PROPERTY,
            BANDWIDTH,
            PROPERTY,
        )
        assertEquals(2.5, distribution.sample().toDouble())
        values[BANDWIDTH] = 200.0
        values[DELAY] = 1.0
        assertEquals(1.5, distribution.sample().toDouble())
        listOf(Double.NaN, -1.0, Double.POSITIVE_INFINITY).forEach { invalidPacketSize ->
            values[SIZE] = invalidPacketSize
            assertEquals(1.0, distribution.packetSize)
            assertEquals(1.005, distribution.sample().toDouble())
        }
    }

    @Test
    fun `access points share their bandwidth among the nodes they serve`() {
        val accessPoint = mockk<Node<Any>>()
        val otherAccessPoint = mockk<Node<Any>>()
        val served = mockk<Node<Any>>()
        val peer = mockk<Node<Any>>()
        val bridge = mockk<Node<Any>>()
        val isolated = mockk<Node<Any>>()
        val neighborhoods = mutableMapOf(
            accessPoint to listOf(served, peer, bridge),
            otherAccessPoint to listOf(bridge),
            served to listOf(accessPoint, peer),
            peer to listOf(served),
            bridge to listOf(accessPoint, otherAccessPoint),
            isolated to emptyList(),
        )
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        neighborhoods.keys.forEachIndexed { id, node ->
            every { node.id } returns id
            every { node.contains(ACCESS_POINT) } returns (node == accessPoint || node == otherAccessPoint)
            every { environment.getNeighborhood(node).current.neighbors } answers {
                neighborhoods.getValue(node).toPersistentList()
            }
        }
        fun bandwidthOf(node: Node<Any>) = SimpleNetworkArrivals(
            mockk<Incarnation<Any, Euclidean2DPosition>>(),
            node,
            environment,
            0.0,
            1.0,
            1200.0,
            ACCESS_POINT,
        ).bandwidth
        assertEquals(400.0, bandwidthOf(accessPoint))
        assertEquals(400.0, bandwidthOf(served))
        assertEquals(1200.0, bandwidthOf(peer))
        assertEquals(1200.0, bandwidthOf(isolated))
        assertFailsWith<IllegalStateException> { bandwidthOf(bridge) }
        neighborhoods[accessPoint] = listOf(served, peer)
        assertEquals(600.0, bandwidthOf(served))
    }

    private companion object {
        const val PROPERTY = "value"
        val DELAY: Molecule = SimpleMolecule("delay")
        val SIZE: Molecule = SimpleMolecule("size")
        val BANDWIDTH: Molecule = SimpleMolecule("bandwidth")
        val ACCESS_POINT: Molecule = SimpleMolecule("accessPoint")
    }
}
