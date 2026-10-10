/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.neighborhoods

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests pertaining to the `it.unibo.alchemist.model.implementations.neighborhoods` package.
 */
class TestNeighborhood {
    private fun createIntNode(environment: Environment<Int, Euclidean2DPosition>): Node<Int> =
        object : GenericNode<Int>(environment) {
            override fun createT(): Int = 0
        }

    /**
     * Tests whether the clone function of the
     * [SimpleNeighborhood] class works as expected.
     */
    @Test
    fun `neighborhoods can be cloned`() {
        val environment = Continuous2DEnvironment(incarnation)
        val n1 = createIntNode(environment)
        val n2 = createIntNode(environment)
        val neigh1 = Neighborhoods.make(environment, n1, mutableListOf(n2))
        val neigh2 = neigh1.remove(n2)
        assertEquals(0, neigh2.size())
        assertTrue(neigh1.neighbors.contains(n2))
    }

    /**
     * Tests that topology changes publish replacement neighborhoods instead of mutating published snapshots.
     */
    @Test
    fun `topology changes publish replacement neighborhoods instead of mutating published snapshots`() {
        val environment = Continuous2DEnvironment(incarnation)
        environment.linkingRule = ConnectWithinDistance(1.0)
        val center = createIntNode(environment)
        val near = createIntNode(environment)
        val far = createIntNode(environment)
        environment.addNode(center, environment.makePosition(0, 0))
        environment.addNode(near, environment.makePosition(0.5, 0))
        environment.addNode(far, environment.makePosition(5, 0))
        val observable = environment.getNeighborhood(center)
        val published = observable.current
        val received = mutableListOf<Neighborhood<Int>>()
        val subscription = observable.subscribe(invokeOnSubscription = false) { received += it }
        try {
            environment.moveNodeTo(far, environment.makePosition(0, 0.5))
            assertEquals(listOf(near), published.neighbors)
            val afterApproach = received.last()
            assertNotSame(published, afterApproach)
            assertEquals(setOf(near, far), afterApproach.neighbors.toSet())
            environment.moveNodeTo(near, environment.makePosition(5, 0))
            assertEquals(listOf(near), published.neighbors)
            assertEquals(setOf(near, far), afterApproach.neighbors.toSet())
            val afterDeparture = received.last()
            assertNotSame(afterApproach, afterDeparture)
            assertEquals(listOf(far), afterDeparture.neighbors)
            assertSame(afterDeparture, observable.current)
        } finally {
            subscription.dispose()
        }
    }

    private companion object {
        val incarnation: Incarnation<Int, Euclidean2DPosition> = SupportedIncarnations.get<Int, Euclidean2DPosition>(
            "protelis",
        ).orElseThrow()
    }
}
