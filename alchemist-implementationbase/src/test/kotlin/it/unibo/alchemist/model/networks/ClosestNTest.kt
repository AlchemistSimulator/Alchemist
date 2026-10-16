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

class ClosestNTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private val model = with(environment) { closestN(1) }

    @Test
    fun `a model linking the nodes again links them the same way`() {
        environment.networkModel = model
        // Regression test: this specific test showed a problem with the previous implementation.
        val nodes = listOf(1, 7, 18, 9, 3).map { nodeAt(it, 0) }
        val before = neighborhoods(nodes)
        environment.networkModel = model
        assertEquals(before, neighborhoods(nodes))
    }

    private fun neighborhoods(nodes: List<Node<Any>>): List<Set<Node<Any>>> =
        nodes.map { environment.neighborhoodOf(it).current.neighbors.toSet() }

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }
}
