/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.rx.model.adapters.ObservableEnvironment
import it.unibo.alchemist.rx.model.adapters.ObservableNode
import it.unibo.alchemist.rx.model.observation.EventObservable
import it.unibo.alchemist.rx.model.observation.Observable
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.combineLatest
import java.util.ArrayList
import java.util.Collections
import org.danilopianini.util.Hashes

/**
 * An abstract implementation of [ReactiveReaction].
 *
 * This reaction implementation leverages the reactive architecture to automatically
 * schedule updates when its conditions' dependencies change. It listens to both validity and
 * propensity changes of its [ReactiveCondition]s to trigger rescheduling.
 *
 * @param T the concentration type.
 * @param node the node where this reaction is defined.
 * @param timeDistribution the time distribution of this reaction.
 */
abstract class AbstractReactiveReaction<T>(
    override val node: ObservableNode<T>,
    override val timeDistribution: TimeDistribution<T>,
) : ReactiveReaction<T> {

    override val rescheduleRequest = EventObservable()

    private val _actions = ArrayList<ReactiveAction<T>>()
    private val _conditions = ArrayList<ReactiveCondition<T>>()
    private var canExecute: Boolean = true
    private var conditionsAggregateObservable: Observable<Boolean>? = null

    private val hash: Int = Hashes.hash32(node.hashCode(), node.moleculeCount, node.reactions.size)

    private var stringLength: Int = Byte.MAX_VALUE.toInt()

    override var actions: List<ReactiveAction<T>>
        get() = Collections.unmodifiableList(_actions)
        set(value) {
            _actions.clear()
            _actions.addAll(value)
        }

    override var conditions: List<ReactiveCondition<T>>
        get() = Collections.unmodifiableList(_conditions)
        set(value) {
            // avoiding leaking subscriptions
            _conditions.forEach {
                it.isValid.stopWatching(this)
                it.propensityContribution.stopWatching(this)
                it.observableInboundDependencies.stopWatching(this)
            }

            conditionsAggregateObservable?.stopWatching(this)

            _conditions.clear()
            _conditions.addAll(value)

            value.forEach { condition ->
                condition.observableInboundDependencies.merge().onChange(this) {
                    requestReschedule()
                }
            }

            conditionsAggregateObservable = value.takeIf { it.isNotEmpty() }
                ?.map { it.isValid }
                ?.combineLatest { validities -> validities.all { it } }
                ?.apply { onChange(this) { canExecute = it } }

            requestReschedule()
        }

    override val rate: Double get() = timeDistribution.rate

    override val tau: Time get() = timeDistribution.nextOccurence

    override fun canExecute(): Boolean = conditionsAggregateObservable?.current ?: true

    override fun execute() {
        actions.forEach(ReactiveAction<T>::execute)
    }

    override fun initializationComplete(atTime: Time, environment: ObservableEnvironment<T, *>) { }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: ObservableEnvironment<T, *>) {
        updateInternalStatus(currentTime, hasBeenExecuted, environment)
        timeDistribution.update(currentTime, hasBeenExecuted, rate, environment)
    }

    /**
     * This method gets called as soon as [update] is called. It is useful to
     * update the internal status of the reaction.
     *
     * @param currentTime the current simulation time
     * @param hasBeenExecuted true if this reaction has just been executed,
     *                        false if the update has been triggered due to a dependency.
     * @param environment the current environment.
     */
    protected abstract fun updateInternalStatus(
        currentTime: Time,
        hasBeenExecuted: Boolean,
        environment: ObservableEnvironment<T, *>,
    )

    protected fun <R : ReactiveReaction<T>> makeClone(builder: () -> R): R = builder().apply {
        conditions = conditions.map { it.cloneCondition(node, this) }.toList()
        actions = actions.map { it.cloneAction(node, this) }.toList()
    }

    override fun toString(): String = buildString {
        append(this@AbstractReactiveReaction::class.simpleName)
        append('@')
        append(tau)
        append(':')
        append(conditions)
        append('-')
        append(timeDistribution.rate.toString())
        append("->")
        append(actions)
    }.also { stringLength = it.length }

    override fun compareTo(other: ReactiveReaction<T>): Int = tau.compareTo(other.tau)

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean = this === other

    private fun requestReschedule() {
        rescheduleRequest.emit()
    }

    /**
     * AbstractReactiveReaction companion object for static holders
     */
    companion object {

        private const val MARGIN: Byte = 20

        private const val serialVersionUID: Long = 1L
    }
}
