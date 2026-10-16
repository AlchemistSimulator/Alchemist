/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class CombinedNetworkModelTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    @Test
    fun `a combination including a fully connected model links every node`() {
        environment.networkModel = with(environment) {
            CombinedNetworkModel(listOf(connectWithinDistance(1.0), fullyConnected(), NoLinks()))
        }
        val (first, second, third) = listOf(0, 10, 20).map { nodeAt(it) }
        assertEquals(setOf(second, third), neighborsOf(first))
    }

    @Test
    fun `a nested combination links the union of all its models`() {
        environment.networkModel = with(environment) {
            val nested = CombinedNetworkModel(listOf(connectWithinDistance(1.0), connectWithinDistance(1.0)))
            CombinedNetworkModel(listOf(nested, connectWithinDistance(3.0)))
        }
        val (first, second, third) = listOf(0, 1, 3).map { nodeAt(it) }
        assertEquals(setOf(second, third), neighborsOf(first))
        assertEquals(setOf(first, second), neighborsOf(third))
    }

    private fun neighborsOf(node: Node<Any>): Set<Node<Any>> =
        environment.neighborhoodOf(node).current.neighbors.toSet()

    private fun nodeAt(x: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, 0)) }
}
