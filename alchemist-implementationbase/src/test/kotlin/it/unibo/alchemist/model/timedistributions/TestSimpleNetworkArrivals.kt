/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

/**
 * Tests for [SimpleNetworkArrivals].
 */
class TestSimpleNetworkArrivals : StringSpec({

    "SimpleNetworkArrivals should compute rate correctly with constant values" {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()

        every { environment.getNeighborhood(node).neighbors } returns ListSets.emptyListSet()

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
        distribution.rate shouldBeExactly expectedRate
    }

    "SimpleNetworkArrivals should support cloning" {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node1 = mockk<Node<Any>>()
        val node2 = mockk<Node<Any>>()

        every { environment.getNeighborhood(node1).neighbors } returns ListSets.emptyListSet()
        every { environment.getNeighborhood(node2).neighbors } returns ListSets.emptyListSet()

        val distribution = SimpleNetworkArrivals(
            incarnation = incarnation,
            node = node1,
            environment = environment,
            propagationDelay = 0.1,
            packetSize = 1000.0,
            bandwidth = 1000.0,
        )

        val cloned = distribution.cloneOnNewNode(node2, Time.ZERO)

        cloned shouldNotBe distribution
        cloned.node shouldBe node2
        cloned.incarnation shouldBe distribution.incarnation
        cloned.environment shouldBe distribution.environment
        cloned.rate shouldBeExactly distribution.rate
    }

    "SimpleNetworkArrivals should handle bandwidth calculation" {
        val incarnation = mockk<Incarnation<Any, Euclidean2DPosition>>()
        val environment = mockk<Environment<Any, Euclidean2DPosition>>()
        val node = mockk<Node<Any>>()

        every { environment.getNeighborhood(node).neighbors } returns ListSets.emptyListSet()

        val distribution = SimpleNetworkArrivals(
            incarnation = incarnation,
            node = node,
            environment = environment,
            propagationDelay = 0.1,
            packetSize = 1000.0,
            bandwidth = 1000.0,
        )

        distribution.bandwidth shouldBeExactly 1000.0
        distribution.packetSize shouldBeExactly 1000.0
        distribution.propagationDelay shouldBeExactly 0.1
    }
})
