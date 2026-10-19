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
import org.graphstream.graph.implementations.SingleGraph

class OffsetGraphStreamNetworkModelTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    // A path: 0 - 1 - 2.
    private val graph = SingleGraph("path").apply {
        listOf("0", "1", "2").forEach { addNode(it) }
        addEdge("0-1", "0", "1")
        addEdge("1-2", "1", "2")
    }

    @Test
    fun `nodes are linked as their graph neighbors are deployed`() {
        environment.networkModel = OffsetGraphStreamNetworkModel(environment, 0, graph)
        val first = addNode()
        assertEquals(emptySet(), neighborsOf(first))
        val second = addNode()
        assertEquals(setOf(second), neighborsOf(first))
        assertEquals(setOf(first), neighborsOf(second))
        val third = addNode()
        assertEquals(setOf(second), neighborsOf(first))
        assertEquals(setOf(first, third), neighborsOf(second))
        assertEquals(setOf(second), neighborsOf(third))
    }

    @Test
    fun `nodes are unlinked from a removed graph neighbor`() {
        environment.networkModel = OffsetGraphStreamNetworkModel(environment, 0, graph)
        val (first, second, third) = List(3) { addNode() }
        environment.removeNode(second)
        assertEquals(emptySet(), neighborsOf(first))
        assertEquals(emptySet(), neighborsOf(third))
    }

    @Test
    fun `the offset maps the graph to the nodes deployed after the existing ones`() {
        val existing = addNode()
        environment.networkModel = OffsetGraphStreamNetworkModel(environment, 1, graph)
        val (first, second, third) = List(3) { addNode() }
        assertEquals(emptySet(), neighborsOf(existing))
        assertEquals(setOf(second), neighborsOf(first))
        assertEquals(setOf(first, third), neighborsOf(second))
    }

    private fun addNode(): Node<Any> = GenericNode(environment).also {
        environment.addNode(it, environment.makePosition(environment.nodeCount.current, 0))
    }

    private fun neighborsOf(node: Node<Any>): Set<Node<Any>> =
        environment.neighborhoodOf(node).current.neighbors.toSet()
}
