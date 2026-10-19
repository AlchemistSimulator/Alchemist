/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.timedistributions.AbstractDistribution

class EnvironmentTestReaction<T>(
    val environment: Environment<T, *>,
    override val timeDistribution: TimeDistribution<T>,
) : TimeDistributedReaction<T> {

    override val canExecute: Observable<Boolean> get() = validity

    override val host: Environment<T, *> get() = environment

    override val meanDelay: Double get() = timeDistribution.meanDelay

    private val mutableNextOccurrence = observe(timeDistribution.startTime, false)
    override val nextOccurrence: Observable<Time> = mutableNextOccurrence.map { it }

    override var actions: List<Action<T>> = emptyList()
        set(value) {
            check(configurable) { "Actions of $this cannot change once the reaction is initialized or disposed" }
            field = value
        }

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            check(configurable) { "Conditions of $this cannot change once the reaction is initialized or disposed" }
            field = value
            validity.dispose()
            validity = value
                .map { it.isValid }
                .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                ?: observe(true)
        }

    // Like every reaction, it can only be configured until it is initialized or disposed.
    private var configurable = true

    private var validity: Observable<Boolean> = observe(true)

    override fun compareTo(other: Reaction<T>): Int = nextOccurrence.current.compareTo(other.nextOccurrence.current)

    override fun dispose() {
        configurable = false
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        nextOccurrence.dispose()
        mutableNextOccurrence.dispose()
    }

    override fun execute() {
        conditions.forEach(Condition<T>::beforeReactionFires)
        actions.forEach(Action<T>::execute)
        updateSchedulingAfterFiring(environment.simulationOrNull?.time ?: nextOccurrence.current)
    }

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        configurable = false
    }

    override fun updateSchedulingAfterFiring(currentTime: Time) {
        val sample = timeDistribution.sample()
        check(sample.isFinite && sample >= Time.ZERO) { "$timeDistribution generated an invalid delay: $sample" }
        mutableNextOccurrence.current = currentTime.plus(sample)
    }

    override fun updateSchedulingAfterInvalidation(currentTime: Time) {
        if (canExecute.current) {
            updateSchedulingAfterFiring(currentTime)
        } else {
            mutableNextOccurrence.current = Time.INFINITY
        }
    }

    private companion object {
        private val TimeDistribution<*>.startTime: Time
            get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO
    }
}
