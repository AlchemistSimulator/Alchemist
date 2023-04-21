/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import it.unibo.alchemist.core.DependencyGraph
import it.unibo.alchemist.core.implementations.JGraphTDependencyGraph
import it.unibo.alchemist.model.BiochemistryIncarnation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.timedistributions.ExponentialTime
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

private fun <T> Incarnation<T, Euclidean2DPosition>.environment(
    configuration: Environment<T, Euclidean2DPosition>.() -> Unit,
): Environment<T, Euclidean2DPosition> = Continuous2DEnvironment(this@Incarnation).apply {
    linkingRule = ConnectWithinDistance(1.0)
    configuration()
}

context(RandomGenerator, Incarnation<T, Euclidean2DPosition>)
private fun <T> Environment<T, Euclidean2DPosition>.node(
    x: Number,
    y: Number,
    configuration: Node<T>.() -> Unit,
): Node<T> = createNode(this@RandomGenerator, this, null).apply {
    configuration()
    addNode(this, makePosition(x, y))
}

context(RandomGenerator, Incarnation<T, Euclidean2DPosition>, Environment<T, Euclidean2DPosition>)
private fun <T> Node<T>.reaction(configuration: String): Reaction<T> = createReaction(
    this@RandomGenerator,
    this@Environment,
    this,
    ExponentialTime(1.0, this@RandomGenerator),
    configuration,
).also { addReaction(it) }

context(Map<Int, Map<String, Reaction<Double>>>)
private fun String.inNode(id: Int): Reaction<Double> = this@Map[id]!![this]!!

context(StringSpecScope, Map<Int, Map<String, Reaction<Double>>>, DependencyGraph<Double>)
private fun Reaction<Double>.mustHaveOutBoundDependencies(vararg dependencies: Reaction<Double>) =
    outboundDependencies(this).toList() shouldContainExactlyInAnyOrder dependencies.toList()

class TestDependencyGraph : StringSpec(
    {
        with(MersenneTwister(10)) {
            with(BiochemistryIncarnation()) {
                val reactions: MutableMap<Int, Map<String, Reaction<Double>>> = mutableMapOf()
                val environment = environment {
                    fun Node<Double>.configureNode(): Map<String, Reaction<Double>> = listOf(
                        "[a]-->[b]",
                        "[a]-->[c]",
                        "[b]-->[c]",
                        "[c]-->[b]",
                    ).associateWith { reaction(it) }
                    node(0, 0) { reactions += id to configureNode() }
                    node(0.5, 0) { reactions += id to configureNode() }
                }
                with(reactions) {
                    with(JGraphTDependencyGraph(environment)) {
                        asSequence().flatMap { it.value.asSequence() }.map { it.value }.forEach {
                            createDependencies(it)
                        }
                        "local reactions on separate nodes should be isolated" {
                            (0..1).forEach { id ->
                                "[a]-->[b]".inNode(id).mustHaveOutBoundDependencies(
                                    "[a]-->[c]".inNode(id),
                                    "[b]-->[c]".inNode(id),
                                )
                                "[a]-->[c]".inNode(id).mustHaveOutBoundDependencies(
                                    "[a]-->[b]".inNode(id),
                                    "[c]-->[b]".inNode(id),
                                )
                                "[b]-->[c]".inNode(id).mustHaveOutBoundDependencies(
                                    "[c]-->[b]".inNode(id),
                                )
                                "[c]-->[b]".inNode(id).mustHaveOutBoundDependencies(
                                    "[b]-->[c]".inNode(id),
                                )
                            }
                        }
                    }
                }
            }
        }
    },
)
