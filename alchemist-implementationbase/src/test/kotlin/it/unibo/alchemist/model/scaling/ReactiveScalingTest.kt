/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.scaling

import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.conditions.neighborHasConcentration
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.genericReaction
import it.unibo.alchemist.model.scaling.ReactiveScalingTest.Companion.MOLECULE
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.test.startSimulation
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Deterministic proxies for performance: the cost of local changes must not grow with the population.
 * Wall-clock measurements are meaningless in CI, but the work done (rescheduled reactions, registered observers)
 * is exact and machine-independent.
 */
class ReactiveScalingTest {
    /**
     * Runs [check] once a line of [size] nodes is initialized. Nodes are one unit apart and linked to their closest
     * neighbors, each hosting a reaction gated by a neighbor holding [MOLECULE].
     */
    private fun <R> onLine(
        size: Int,
        check: EuclideanEnvironment<Any, Euclidean2DPosition>.(middle: Node<Any>) -> R,
    ): R {
        var result: Result<R>? = null
        simulation2D(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()) {
            environment {
                networkModel(ConnectWithinDistance(1.5))
                deployments {
                    deploy(grid(0.0, 0.0, size - 0.5, 0.5, 1.0, 1.0)) {
                        withTimeDistribution(DiracComb<Any>(1.0)) {
                            program(genericReaction()) {
                                condition(neighborHasConcentration(MOLECULE, 1.0))
                            }
                        }
                    }
                }
            }
        }.getDefault<Any, Euclidean2DPosition>().startSimulation(
            steps = 1,
            onceInitialized = { environment ->
                assertEquals(size, environment.nodeCount.current)
                val middle = environment.nodes.current.single { environment.getCurrentPosition(it).x == size / 2.0 }
                result = runCatching { environment.check(middle) }
            },
        )
        return checkNotNull(result).getOrThrow()
    }

    /**
     * The offsets, relative to the position of [middle] before [mutation], of the nodes whose reactions get
     * rescheduled by [mutation].
     */
    private fun EuclideanEnvironment<Any, Euclidean2DPosition>.rescheduledBy(
        middle: Node<Any>,
        mutation: () -> Unit,
    ): List<Int> {
        val rescheduled = mutableListOf<Int>()
        val origin = getCurrentPosition(middle).x
        val subscriptions = nodes.current.flatMap { node ->
            node.reactions.current.map { reaction ->
                reaction.nextOccurrence.subscribe(invokeOnSubscription = false) {
                    rescheduled += (getCurrentPosition(node).x - origin).roundToInt()
                }
            }
        }
        mutation()
        subscriptions.forEach { it.dispose() }
        return rescheduled.sorted()
    }

    @Test
    fun `a local concentration change reschedules the same reactions whatever the population`() {
        val change: EuclideanEnvironment<Any, Euclidean2DPosition>.(Node<Any>) -> List<Int> = { middle ->
            rescheduledBy(middle) { middle.setConcentration(MOLECULE, 1.0) }
        }
        val small = onLine(SMALL, change)
        assertEquals(listOf(-1, 1), small)
        assertEquals(small, onLine(LARGE, change))
    }

    @Test
    fun `a local movement reschedules the same reactions whatever the population`() {
        // The middle node leaves the range of its left neighbor and enters the one of the node two steps right.
        val movement: EuclideanEnvironment<Any, Euclidean2DPosition>.(Node<Any>) -> List<Int> = { middle ->
            middle.setConcentration(MOLECULE, 1.0)
            rescheduledBy(middle) { moveNodeBy(middle, Euclidean2DPosition(0.6, 0.0)) }
        }
        val small = onLine(SMALL, movement)
        assertEquals(listOf(-1, 2), small)
        assertEquals(small, onLine(LARGE, movement))
    }

    @Test
    fun `a node is observed only by its neighbors whatever the population`() {
        val observers: EuclideanEnvironment<Any, Euclidean2DPosition>.(Node<Any>) -> Int = { middle ->
            middle.observeConcentration(MOLECULE).observers.size
        }
        assertEquals(onLine(SMALL, observers), onLine(LARGE, observers))
    }

    @Test
    fun `removing the reactions releases every subscription to node contents`() {
        val remaining = onLine(SMALL) {
            nodes.current.forEach { node -> node.reactions.current.forEach(node::removeReaction) }
            nodes.current.sumOf { node -> node.observeConcentration(MOLECULE).observers.size }
        }
        assertEquals(0, remaining)
    }

    private companion object {
        private const val SMALL = 100
        private const val LARGE = 1000
        private val MOLECULE = SimpleMolecule("m")
    }
}
