/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import org.danilopianini.util.ListSets
import org.junit.jupiter.api.Test

/**
 * Tests for [SimpleNetworkArrivals].
 */
class TestSimpleNetworkArrivals {

    @Test
    fun `SimpleNetworkArrivals should compute rate correctly with constant values`() {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()
        every { environment.getNeighborhood(node).current.neighbors } returns ListSets.emptyListSet()
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
        val expectedRate = 1.0 / (propagationDelay + packetSize / bandwidth)
        assertEquals(expectedRate, distribution.rate)
    }

    @Test
    fun `SimpleNetworkArrivals should support cloning`() {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node1 = mockk<Node<Any>>()
        val node2 = mockk<Node<Any>>()
        every { environment.getNeighborhood(node1).current.neighbors } returns ListSets.emptyListSet()
        every { environment.getNeighborhood(node2).current.neighbors } returns ListSets.emptyListSet()
        val distribution = SimpleNetworkArrivals(
            incarnation = incarnation,
            node = node1,
            environment = environment,
            propagationDelay = 0.1,
            packetSize = 1000.0,
            bandwidth = 1000.0,
        )
        val cloned = distribution.cloneOnNewNode(node2, Time.ZERO)
        assertNotEquals(distribution, cloned)
        assertEquals(node2, cloned.node)
        assertEquals(distribution.incarnation, cloned.incarnation)
        assertEquals(distribution.environment, cloned.environment)
        assertEquals(distribution.rate, cloned.rate)
    }

    @Test
    fun `SimpleNetworkArrivals should handle bandwidth calculation`() {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()
        every { environment.getNeighborhood(node).current.neighbors } returns ListSets.emptyListSet()
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
}
