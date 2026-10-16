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

class AdaptiveRangeTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())

    private val model = with(environment) {
        adaptiveRange(
            radius = 1.0,
            desiredNeighbors = 1,
            tolerance = 0,
            minRange = 1.0,
            maxRange = 10.0,
            adjustment = 1.0,
        )
    }

    @Test
    fun `reading the neighborhoods of an unobserved model leaves the ranges untouched`() {
        val first = nodeAt(0, 0)
        val second = nodeAt(3, 0)
        val reads = listOf(first, second, first).map { model.neighborhoodOf(it).current.neighbors.toList() }
        assertEquals(listOf(emptyList(), emptyList(), emptyList()), reads)
    }

    @Test
    fun `static nodes out of the initial range adapt their ranges until they are linked`() {
        environment.networkModel = model
        val first = nodeAt(0, 0)
        val second = nodeAt(3, 0)
        assertEquals(listOf(second), environment.neighborhoodOf(first).current.neighbors.toList())
        assertEquals(listOf(first), environment.neighborhoodOf(second).current.neighbors.toList())
    }

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }
}
