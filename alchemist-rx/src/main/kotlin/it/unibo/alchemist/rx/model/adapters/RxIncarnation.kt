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
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.rx.model.adapters.ObservableNode.NodeExtension.asObservableNode
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveBinder
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveBinder.bindAndGetReactiveCondition
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveConditionAdapter
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveReactionAdapter
import org.apache.commons.math3.random.RandomGenerator

class RxIncarnation<T, P : Position<out P>>(private val delegate: Incarnation<T, P>) {

    fun createNode(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        parameter: Any?,
    ): ObservableNode<T> = delegate.createNode(randomGenerator, environment, parameter).asObservableNode()

    fun createReaction(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        node: ObservableNode<T>,
        timeDistribution: TimeDistribution<T>,
        parameter: Any?,
    ): ReactiveReactionAdapter<T> = delegate.createReaction(
        randomGenerator,
        environment,
        node,
        timeDistribution,
        parameter,
    ).let {
        ReactiveBinder.bindAndGetReactiveReaction(it, environment)
    }

    @Suppress("complexity:LongParameterList")
    fun createCondition(
        randomGenerator: RandomGenerator,
        environment: ObservableEnvironment<T, P>,
        node: ObservableNode<T>,
        timeDistribution: TimeDistribution<T>,
        reaction: Reaction<T>,
        additionalParameters: Any?,
    ): ReactiveConditionAdapter<T> = delegate.createCondition(
        randomGenerator,
        environment,
        node,
        timeDistribution,
        reaction,
        additionalParameters,
    ).bindAndGetReactiveCondition(environment, reaction)

    companion object {
        fun <T, P : Position<out P>> Incarnation<T, P>.asReactive(): RxIncarnation<T, P> = RxIncarnation(this)
    }
}
