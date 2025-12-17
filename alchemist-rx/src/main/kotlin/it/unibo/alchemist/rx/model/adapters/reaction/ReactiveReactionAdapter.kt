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
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.observation.EventObservable
import it.unibo.alchemist.rx.model.observation.MutableObservable
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.combineLatest
import java.util.Collections

/**
 * A reactive version of [Reaction] capable of * being observed for rescheduling requests
 * along with having a reactive validity trigger based on [condition][ReactiveConditionAdapter]'s
 * inbound dependencies' changes.
 */
interface ReactiveReactionAdapter<T> : Reaction<T> {
    /**
     * Used by the scheduler to learn when this reaction needs to be rescheduled.
     */
    val rescheduleRequest: Observable<Unit>
}

/**
 * Converts this [Reaction] into a [reactive reaction][ReactiveReactionAdapter].
 *
 * @param environment the environment where the node containing this reaction is placed.
 */
fun <T> Reaction<T>.asReactive(environment: ObservableEnvironment<T, *>): ReactiveReactionAdapter<T> =
    ReactiveBinder.bindAndGetReactiveReaction(this, environment)

internal class ReactiveReactionAdapterImpl<T>(
    private val origin: Reaction<T>,
    private val environment: ObservableEnvironment<T, *>,
) : ReactiveReactionAdapter<T>,
    Reaction<T> by origin {

    override val rescheduleRequest = EventObservable()

    private val _conditions = ArrayList<ReactiveConditionAdapter<T>>()

    private val conditionsAggregateObservable: MutableObservable<Boolean> = observe(true)

    override var conditions: List<Condition<T>>
        get() = Collections.unmodifiableList(_conditions)
        set(value) {
            _conditions.forEach {
                it.observeValidity.stopWatching(this)
                it.observePropensityContribution.stopWatching(this)
                it.observableInboundDependencies.stopWatching(this)
            }

            _conditions.clear()

            value.map { it.asReactive(environment, this) }.also { newConditions ->
                _conditions.addAll(newConditions)
                newConditions.forEach { condition ->
                    condition.observableInboundDependencies.merge().onChange(this) {
                        rescheduleRequest.emit() // the scheduler will take care to call `update(..., false, ...)`
                    }
                }

                newConditions.map { it.observeValidity }
                    .combineLatest { validities -> validities.all { it } }
                    .apply {
                        onChange(this) { _ ->
                            conditionsAggregateObservable.update { it }
                        }
                    }
            }

            rescheduleRequest.emit()
        }

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): ReactiveReactionAdapter<T> =
        origin.cloneOnNewNode(node, currentTime).asReactive(environment)

    override fun canExecute(): Boolean = conditionsAggregateObservable.current &&
        origin.canExecute() // when and if this class is abstract, let's mimic [AbstractReaction<T>]

    override fun toString(): String = "Rx-$origin"
}
