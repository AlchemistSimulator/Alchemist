/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class FullyConnectedTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private val nodes: List<Node<Any>> = List(3) { GenericNode(environment) }

    init {
        environment.networkModel = with(environment) { fullyConnected() }
        // Deployed in reverse order of creation, hence of id.
        nodes.reversed().forEach { environment.addNode(it, environment.makePosition(it.id * 10, 0)) }
    }

    @Test
    fun `neighbors are sorted regardless of the order nodes are deployed in`() {
        assertEquals(listOf(nodes[1], nodes[2]), environment.neighborhoodOf(nodes[0]).current.neighbors.toList())
    }

    @Test
    fun `neighborhoods equal the standard ones with the same neighbors`() {
        val fullyConnected = environment.neighborhoodOf(nodes[0]).current
        val standard = Neighborhood(nodes[0], setOf(nodes[1], nodes[2]))
        assertEquals(standard, fullyConnected)
        assertEquals(fullyConnected, standard)
        assertEquals(standard.hashCode(), fullyConnected.hashCode())
    }
}
