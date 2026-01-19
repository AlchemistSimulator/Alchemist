/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core.util

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.AbstractReaction
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import org.apache.commons.math3.random.RandomGenerator

object DependencyUtils {

    class SimpleReaction<T>(
        node: Node<T>,
        distribution: TimeDistribution<T>,
        val action: () -> Unit,
    ) : AbstractReaction<T>(node, distribution) {
        override fun updateInternalStatus(
            currentTime: Time,
            hasBeenExecuted: Boolean,
            environment: Environment<T, *>,
        ) = Unit

        override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Reaction<T> = throw NotImplementedError()

        override fun execute() {
            action()
        }
    }

    data class RandomContext(
        val randomGenerator: RandomGenerator,
    )

    data class IncarnationContext<T>(
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

    data class EnvironmentContext<T>(
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

    fun withRandom(randomGenerator: RandomGenerator, block: RandomContext.() -> Unit) {
        RandomContext(randomGenerator).block()
    }

    fun <T> RandomContext.withIncarnation(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        block: IncarnationContext<T>.() -> Unit,
    ) {
        IncarnationContext(randomGenerator, incarnation).block()
    }
}
