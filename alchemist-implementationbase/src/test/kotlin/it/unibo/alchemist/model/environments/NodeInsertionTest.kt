/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeInsertionTest {

    /**
     * An environment that inserts every node [SHIFT] units to the right of the requested position.
     */
    private class ShiftingEnvironment :
        Continuous2DEnvironment<Any>(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()) {
        val notifiedPositions = mutableListOf<Euclidean2DPosition>()

        override fun computeActualInsertionPosition(node: Node<Any>, originalPosition: Euclidean2DPosition) =
            makePosition(originalPosition.x + SHIFT, originalPosition.y)

        override fun nodeAdded(node: Node<Any>, position: Euclidean2DPosition, neighborhood: Neighborhood<Any>) {
            notifiedPositions += position
            super.nodeAdded(node, position, neighborhood)
        }
    }

    @Test
    fun `node addition notifies and bounds the actual insertion position`() {
        val environment = ShiftingEnvironment()
        val node = GenericNode(environment)
        environment.addNode(node, environment.makePosition(0, 0))
        val actual = environment.makePosition(SHIFT, 0)
        assertEquals(actual, environment.currentPositionOf(node))
        assertEquals(listOf(actual), environment.notifiedPositions)
        // The environment bounds are widened by one ulp around each included object.
        assertEquals(SHIFT, environment.offset[0], TOLERANCE)
        assertEquals(0.0, environment.offset[1], TOLERANCE)
    }

    @Test
    fun `node list subscribers can read the neighborhood of a node being added`() {
        val environment = Continuous2DEnvironment(
            SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow(),
        ).apply { networkModel = connectWithinDistance(1.0) }
        val first = GenericNode(environment).also { environment.addNode(it, environment.makePosition(0, 0)) }
        val neighborsOnArrival = mutableListOf<List<Node<Any>>>()
        environment.nodes.subscribe(invokeOnSubscription = false) { nodes ->
            neighborsOnArrival += environment.neighborhoodOf(nodes.last()).current.neighbors.toList()
        }
        val second = GenericNode(environment).also { environment.addNode(it, environment.makePosition(0.5, 0)) }
        // The node is published already linked.
        assertEquals<List<List<Node<Any>>>>(listOf(listOf(first)), neighborsOnArrival)
        assertEquals(listOf(first), environment.neighborhoodOf(second).current.neighbors.toList())
    }

    private companion object {
        const val SHIFT = 10.0
        const val TOLERANCE = 1e-9
    }
}
