/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

private data class RandomContext(
    val randomGenerator: RandomGenerator,
)

private data class IncarnationContext<T>(
    val randomGenerator: RandomGenerator,
    val incarnation: Incarnation<T, Euclidean2DPosition>,
) {
    fun environment(configuration: EnvironmentContext<T>.() -> Unit): Environment<T, Euclidean2DPosition> =
        EnvironmentContext(
            randomGenerator,
            incarnation,
            Continuous2DEnvironment(incarnation).apply {
                linkingRule = ConnectWithinDistance(1.0)
            },
        ).apply(configuration).environment
}

private data class EnvironmentContext<T>(
    val randomGenerator: RandomGenerator,
    val incarnation: Incarnation<T, Euclidean2DPosition>,
    val environment: Environment<T, Euclidean2DPosition>,
) {
    fun Node<T>.reaction(configuration: String): Reaction<T> = incarnation
        .createReaction(
            randomGenerator,
            environment,
            this,
            ExponentialTime(1.0, randomGenerator),
            configuration,
        ).also { addReaction(it) }

    fun node(x: Number, y: Number, configuration: Node<T>.() -> Unit): Node<T> =
        incarnation.createNode(randomGenerator, environment, null).apply {
            configuration()
            environment.addNode(this, environment.makePosition(x, y))
        }
}

private fun withRandom(randomGenerator: RandomGenerator, block: RandomContext.() -> Unit) {
    RandomContext(randomGenerator).block()
}

private fun <T> RandomContext.withIncarnation(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    block: IncarnationContext<T>.() -> Unit,
) {
    IncarnationContext(randomGenerator, incarnation).block()
}

class TestDependencyGraph :
    StringSpec(
        {
            withRandom(MersenneTwister(10)) {
                withIncarnation(BiochemistryIncarnation()) {
                    val reactions: MutableMap<Int, Map<String, Reaction<Double>>> = mutableMapOf()
                    val environment =
                        environment {
                            fun Node<Double>.configureNode(): Map<String, Reaction<Double>> = listOf(
                                "[a]-->[b]",
                                "[a]-->[c]",
                                "[b]-->[c]",
                                "[c]-->[b]",
                            ).associateWith { reaction(it) }
                            node(0, 0) { reactions += id to configureNode() }
                            node(0.5, 0) { reactions += id to configureNode() }
                        }

                    fun String.inNode(id: Int): Reaction<Double> = reactions.getValue(id).getValue(this)
                    with(JGraphTDependencyGraph(environment)) {
                        reactions.asSequence().flatMap { it.value.asSequence() }.map { it.value }.forEach {
                            createDependencies(it)
                        }

                        fun Reaction<Double>.mustHaveOutBoundDependencies(vararg dependencies: Reaction<Double>) =
                            outboundDependencies(this).toList() shouldContainExactlyInAnyOrder dependencies.toList()
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
        },
    )
