/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveBinder
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveTimeDistribution
import it.unibo.alchemist.rx.model.adapters.reaction.RxCondition
import it.unibo.alchemist.rx.model.adapters.reaction.RxReaction
import org.apache.commons.math3.random.RandomGenerator

class RxIncarnation<T, P : Position<P>>(private val delegate: Incarnation<T, P>) {

    fun createNode(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        parameter: Any?,
    ): ObservableNode<T> = delegate.createNode(randomGenerator, environment, parameter).asObservableNode()

    fun createReaction(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        node: ObservableNode<T>,
        timeDistribution: ReactiveTimeDistribution<T>,
        parameter: Any?,
    ): RxReaction<T> = delegate.createReaction(
        randomGenerator,
        environment,
        node,
        timeDistribution,
        parameter,
    ).let {
        ReactiveBinder.bindReaction(it, environment)
    }

    @Suppress("complexity:LongParameterList")
    fun createCondition(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        node: ObservableNode<T>,
        timeDistribution: ReactiveTimeDistribution<T>,
        reaction: RxReaction<T>,
        additionalParameters: Any?,
    ): RxCondition<T> = delegate.createCondition(
        randomGenerator,
        environment,
        node,
        timeDistribution,
        reaction,
        additionalParameters,
    ).let {
        ReactiveBinder.bindCondition(it, reaction, environment)
    }

    companion object {
        fun <T, P : Position<P>> Incarnation<T, P>.asReactive(): RxIncarnation<T, P> = RxIncarnation(this)
    }
}
