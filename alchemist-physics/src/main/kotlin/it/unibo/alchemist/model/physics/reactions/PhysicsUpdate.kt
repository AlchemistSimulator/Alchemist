/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.reactions

import arrow.core.getOrElse
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.physics.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.timedistributions.AbstractDistribution
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.SimpleNetworkArrivals
import it.unibo.alchemist.model.timedistributions.WeibullTime

/**
 * A global reaction responsible for updating a [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T>(
    /** The physics environment advanced by this reaction. */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution = DiracComb(DEFAULT_RATE),
) : GlobalReaction<T> {

    constructor(environment: Dynamics2DEnvironment<T>, updateRate: Double) : this(environment, DiracComb(updateRate))

    override val inputContext: Context = Context.GLOBAL
    override val outputContext: Context = Context.GLOBAL
    override val rate: Double get() = timeDistribution.expectedRate
    private val mutableNextOccurrence = observe(timeDistribution.startTime, false)
    override val tau: Observable<Time> = mutableNextOccurrence.map { it }

    override var actions: List<Action<T>> = emptyList()

    private var validity: Observable<Boolean> = observe(true)
    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            validity.dispose()
            validity = value
                .map(Condition<T>::isValid)
                .combineLatest { validities -> validities.all { it } }
                .map { it.getOrElse { true } }
        }

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    override fun canExecute(): Observable<Boolean> = validity

    override fun execute() {
        environment.updatePhysics(1 / rate)
    }

    override fun update(currentTime: Time) {
        val sample = timeDistribution.sample()
        check(sample.isFinite && sample >= Time.ZERO) { "$timeDistribution generated an invalid delay: $sample" }
        mutableNextOccurrence.current = currentTime.plus(sample)
    }

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    override fun dispose() {
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        tau.dispose()
        mutableNextOccurrence.dispose()
    }

    private companion object {
        const val DEFAULT_RATE = 30.0

        private val TimeDistribution.startTime: Time
            get() = (this as? AbstractDistribution)?.startTime ?: Time.ZERO

        private val TimeDistribution.expectedRate: Double
            get() = when (this) {
                is DiracComb -> frequency
                is ExponentialTime -> lambda
                is AnyRealDistribution -> mean
                is WeibullTime -> mean
                is SimpleNetworkArrivals<*> -> expectedRate
                else -> Double.NaN
            }
    }
}
