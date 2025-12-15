/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.adapters.reaction

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.reaction.ReactiveBinder.bindAndGetReactiveCondition
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.merge
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.rx.model.observation.ObservableSet

/**
 * A simple reactive [Condition] which exposes its validity and propensity as
 * [Observable] values. This condition emits new values of those two every
 * time its inbound dependencies emit a change. In this reactive context,
 * inbound dependencies are assumed to be observables which emit values.
 */
interface ReactiveConditionAdapter<T> : Condition<T> {

    /**
     * Observe the validity of this condition. Emits a new value whenever the
     * list of [observableInboundDependencies] changes or its members emit a
     * change.
     */
    val observeValidity: Observable<Boolean>

    /**
     * Observe the propensity contribution of this condition. Emits a new value whenever the
     * list of [observableInboundDependencies] changes or its members emit a
     * change.
     */
    val observePropensityContribution: Observable<Double>

    /**
     * The list of this condition's dependencies as [Observable]. Useful
     * for checking when this condition's inbound dependencies change.
     */
    val observableInboundDependencies: ObservableSet<Observable<*>>
}

/**
 * Converts the given [Condition] to a [ReactiveCondition][ReactiveConditionAdapter].
 *
 * @param environment the [ObservableEnvironment] where the node contains the enclosing reaction of this condition.
 * @param reaction the [Reaction] containing this condition.
 */
fun <T> Condition<T>.asReactive(
    environment: ObservableEnvironment<T, *>,
    reaction: Reaction<T>,
): ReactiveConditionAdapter<T> =
    this as? ReactiveConditionAdapter<T> ?: bindAndGetReactiveCondition(environment, reaction)

internal class ReactiveConditionAdapterImpl<T>(
    private val origin: Condition<T>,
    private val dependencies: List<Observable<*>>,
    private val environment: ObservableEnvironment<T, *>,
) : ReactiveConditionAdapter<T>,
    Condition<T> by origin {

    override val observeValidity: Observable<Boolean> by lazy {
        dependencies.merge().map { isValid }
    }

    override val observePropensityContribution: Observable<Double> by lazy {
        dependencies.merge().map { propensityContribution }
    }

    override val observableInboundDependencies: ObservableSet<Observable<*>> by lazy {
        dependencies.toObservableSet()
    }

    override fun cloneCondition(node: Node<T>, reaction: Reaction<T>): ReactiveConditionAdapter<T> =
        origin.cloneCondition(node, reaction).asReactive(environment, reaction)

    override fun reactionReady() {
        origin.reactionReady()
    }
}
