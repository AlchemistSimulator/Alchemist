/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.GenericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ObservableEnvironmentStateTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())
            .apply { linkingRule = ConnectWithinDistance(1.0) }

    @Test
    fun `a neighborhood follows nodes entering, leaving, and being removed`() {
        val center = nodeAt(0.0)
        val neighbors = environment.getNeighborhood(center).map { it.neighbors.toSet() }.record()
        val near = nodeAt(0.5)
        val far = nodeAt(5.0)
        environment.moveNodeTo(far, Euclidean2DPosition(0.0, 0.5))
        environment.moveNodeTo(near, Euclidean2DPosition(3.0, 0.0))
        environment.removeNode(far)
        assertEquals(listOf(emptySet(), setOf(near), setOf(near, far), setOf(far), emptySet()), neighbors)
    }

    @Test
    fun `a node already in the environment cannot be added again`() {
        val node = nodeAt(0.0)
        listOf(node, GenericNode(environment, node.id)).forEach { duplicate ->
            assertFailsWith<IllegalArgumentException> { environment.addNode(duplicate, Euclidean2DPosition(1.0, 0.0)) }
        }
        assertEquals(listOf(node), environment.nodes.current)
        assertEquals(Euclidean2DPosition(0.0, 0.0), environment.getCurrentPosition(node))
    }

    @Test
    fun `a position follows the movements of its node`() {
        val node = nodeAt(0.0)
        val positions = environment.getPosition(node).record()
        environment.moveNodeTo(node, Euclidean2DPosition(1.0, 1.0))
        environment.moveNodeTo(node, Euclidean2DPosition(2.0, 0.0))
        assertEquals(
            listOf(Euclidean2DPosition(0.0, 0.0), Euclidean2DPosition(1.0, 1.0), Euclidean2DPosition(2.0, 0.0)),
            positions,
        )
    }

    @Test
    fun `looking up a node that is not in the environment fails`() {
        val removed = nodeAt(0.0)
        environment.removeNode(removed)
        val neverAdded = GenericNode(environment)
        listOf(removed, neverAdded).forEach { node ->
            assertFailsWith<IllegalArgumentException> { environment.getNodeByID(node.id) }
        }
    }

    @Test
    fun `nodes are found by id whatever their insertion order`() {
        val nodes = List(3) { GenericNode(environment) }
        nodes.reversed().forEachIndexed { index, node ->
            environment.addNode(node, Euclidean2DPosition(index * 0.5, 0.0))
        }
        nodes.forEach { node -> assertSame(node, environment.getNodeByID(node.id)) }
        // Changing the linking rule recomputes every neighborhood.
        environment.linkingRule = ConnectWithinDistance(0.1)
        nodes.forEach { node -> assertSame(node, environment.getNodeByID(node.id)) }
    }

    @Test
    fun `the node list and count follow additions and removals`() {
        val nodes = environment.nodes.record()
        val counts = environment.nodeCount.record()
        val first = nodeAt(0.0)
        val second = nodeAt(5.0)
        environment.removeNode(first)
        assertEquals(listOf(emptyList(), listOf(first), listOf(first, second), listOf(second)), nodes)
        assertEquals(listOf(0, 1, 2, 1), counts)
    }

    @Test
    fun `the reactions of a host follow additions and removals`() {
        val node = nodeAt(0.0)
        val reactions = node.reactions.record()
        val first = GenericReaction(node, DiracComb(1.0))
        val second = GenericReaction(node, DiracComb(1.0))
        node.addReaction(first)
        node.addReaction(second)
        node.removeReaction(first)
        assertEquals(listOf(emptyList(), listOf(first), listOf(first, second), listOf(second)), reactions)
    }

    private fun nodeAt(x: Double, y: Double = 0.0): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, Euclidean2DPosition(x, y)) }

    private fun <T> Observable<T>.record(): List<T> = mutableListOf<T>().also { values -> subscribe { values += it } }
}
