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
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.timedistributions.AbstractDistribution
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.SimpleNetworkArrivals
import it.unibo.alchemist.model.timedistributions.WeibullTime

class EnvironmentTestReaction<T>(
    val environment: Environment<T, *>,
    override val timeDistribution: TimeDistribution<T>,
) : TimeDistributedReaction<T> {

    override val rate: Double get() = timeDistribution.expectedRate
    private val mutableNextOccurrence = MutableObservable.observe(timeDistribution.startTime, false)
    override val nextOccurrence: Observable<Time> = mutableNextOccurrence.map { it }
    override var actions: List<Action<T>> = emptyList()
    private var validity: Observable<Boolean> = MutableObservable.observe(true)

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            validity.dispose()
            validity = value
                .map(Condition<T>::isValid)
                .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                ?: MutableObservable.observe(true)
        }

    override fun compareTo(other: Reaction<T>): Int = nextOccurrence.current.compareTo(other.nextOccurrence.current)

    override fun canExecute(): Observable<Boolean> = validity

    override fun execute() {
        conditions.forEach(Condition<T>::reactionReady)
        actions.forEach(Action<T>::execute)
        updateSchedulingAfterFiring(environment.simulationOrNull?.time ?: nextOccurrence.current)
    }

    override fun updateSchedulingAfterFiring(currentTime: Time) {
        val sample = timeDistribution.sample()
        check(sample.isFinite && sample >= Time.ZERO) { "$timeDistribution generated an invalid delay: $sample" }
        mutableNextOccurrence.current = currentTime.plus(sample)
    }

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    override fun dispose() {
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        nextOccurrence.dispose()
        mutableNextOccurrence.dispose()
    }

    private companion object {
        private val TimeDistribution<*>.startTime: Time
            get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO

        private val TimeDistribution<*>.expectedRate: Double
            get() = when (this) {
                is DiracComb<*> -> frequency
                is ExponentialTime<*> -> lambda
                is AnyRealDistribution<*> -> mean
                is WeibullTime<*> -> mean
                is SimpleNetworkArrivals<*> -> expectedRate
                else -> Double.NaN
            }
    }
}
