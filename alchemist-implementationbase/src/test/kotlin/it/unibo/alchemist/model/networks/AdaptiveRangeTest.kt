/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.Timeout

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

    @Test
    fun `a growing range overshooting the tolerance keeps the count closest to the desired one`() {
        // From range 2 (1 neighbor) to range 3 (5 neighbors), with 2 desired: 1 is closer.
        val center = centerAmong(range = 1.0, nodes = listOf(1.5 to 0.0) + atDistance(2.5, count = 4))
        assertEquals(1, center.neighborCount())
    }

    @Test
    fun `a growing range overshooting the tolerance keeps the larger range on a tie`() {
        // From range 2 (1 neighbor) to range 3 (3 neighbors), with 2 desired: both are 1 away.
        val center = centerAmong(range = 1.0, nodes = listOf(1.5 to 0.0) + atDistance(2.5, count = 2))
        assertEquals(3, center.neighborCount())
    }

    @Test
    fun `a shrinking range overshooting the tolerance keeps the count closest to the desired one`() {
        // From range 3 (4 neighbors) to range 2 (1 neighbor), with 2 desired: 1 is closer.
        val center = centerAmong(range = 4.0, nodes = listOf(1.5 to 0.0) + atDistance(2.5, count = 3))
        assertEquals(1, center.neighborCount())
    }

    @Test
    fun `a shrinking range overshooting the tolerance keeps the larger range on a tie`() {
        // From range 3 (3 neighbors) to range 2 (1 neighbor), with 2 desired: both are 1 away.
        val center = centerAmong(range = 4.0, nodes = listOf(1.5 to 0.0) + atDistance(2.5, count = 2))
        assertEquals(3, center.neighborCount())
    }

    @Test
    @Timeout(RANDOM_LAYOUTS_TIMEOUT_SECONDS)
    fun `ranges influencing each other always stabilize`() {
        repeat(RANDOM_LAYOUTS) { seed ->
            val random = Random(seed)
            val layout = List(RANDOM_LAYOUT_NODES) { random.nextDouble(0.0, 10.0) to random.nextDouble(0.0, 10.0) }
            val desired = random.nextInt(1, 5)
            val tolerance = random.nextInt(0, 2)
            // Deploying the same layout twice links it the same way: the ranges reach the same stable state.
            assertEquals(stableLinks(layout, desired, tolerance), stableLinks(layout, desired, tolerance), "seed $seed")
        }
    }

    /*
     * Deploys nodes at the given coordinates, then a center at the origin, with links depending only on the range of
     * their center, so that the range of the center adapts to the given nodes alone.
     */
    private fun centerAmong(range: Double, nodes: List<Pair<Double, Double>>): Node<Any> {
        environment.networkModel = object : AdaptiveRange<Any, Euclidean2DPosition>(
            environment,
            radius = range,
            desiredNeighbors = 2,
            tolerance = 0,
            minRange = 1.0,
            maxRange = 10.0,
            adjustment = 1.0,
        ) {
            override fun conditionForRemoval(
                environment: Environment<Any, Euclidean2DPosition>,
                center: Node<Any>,
                neighbor: Node<Any>,
                centerRange: Double,
                neighRange: Double,
            ): Boolean = false
        }
        nodes.forEach { (x, y) -> nodeAt(x, y) }
        return nodeAt(0, 0)
    }

    private fun Node<Any>.neighborCount(): Int = environment.neighborhoodOf(this).current.neighbors.size

    private fun stableLinks(layout: List<Pair<Double, Double>>, desired: Int, tolerance: Int): List<List<Int>> {
        val environment =
            Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())
        environment.networkModel = with(environment) {
            adaptiveRange(
                radius = 1.0,
                desiredNeighbors = desired,
                tolerance = tolerance,
                minRange = 0.5,
                maxRange = 10.0,
                adjustment = 0.5,
            )
        }
        val nodes = layout.map { (x, y) ->
            GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }
        }
        return nodes.map { node -> environment.neighborhoodOf(node).current.neighbors.map { it.id - nodes.first().id } }
    }

    private fun nodeAt(x: Number, y: Number): Node<Any> =
        GenericNode(environment).also { environment.addNode(it, environment.makePosition(x, y)) }

    private companion object {
        const val RANDOM_LAYOUTS = 200
        const val RANDOM_LAYOUT_NODES = 30
        const val RANDOM_LAYOUTS_TIMEOUT_SECONDS = 60L

        // Coordinates of count points at the given distance from the origin, evenly spread around it.
        fun atDistance(distance: Double, count: Int): List<Pair<Double, Double>> = List(count) { index ->
            val angle = 2 * PI * index / count + PI / 2
            distance * cos(angle) to distance * sin(angle)
        }
    }
}
