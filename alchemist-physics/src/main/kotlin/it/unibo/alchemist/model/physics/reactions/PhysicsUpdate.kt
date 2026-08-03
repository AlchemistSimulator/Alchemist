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
import it.unibo.alchemist.model.observation.CompositeDisposable
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.physics.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.timedistributions.DiracComb

/**
 * A global reaction responsible for updating a [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T>(
    /** The physics environment advanced by this reaction. */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T> = DiracComb(DEFAULT_RATE),
) : GlobalReaction<T> {

    constructor(environment: Dynamics2DEnvironment<T>, updateRate: Double) : this(environment, DiracComb(updateRate))

    override val inputContext: Context = Context.GLOBAL
    override val outputContext: Context = Context.GLOBAL
    override val rate: Double get() = timeDistribution.rate
    override val tau: Observable<Time> get() = timeDistribution.nextOccurence

    override var actions: List<Action<T>> = emptyList()

    private var validity: Observable<Boolean> = observe(true)
    private var initialized = false
    private val subscriptions = CompositeDisposable()

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            validity.dispose()
            validity = value
                .map(Condition<T>::isValid)
                .combineLatest { validities -> validities.all { it } }
                .map { it.getOrElse { true } }
            if (initialized) {
                initializeSubscriptions()
                timeDistribution.reactToUpdate(environment.simulation.time, this)
            }
        }

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    override fun canExecute(): Observable<Boolean> = validity

    override fun execute() {
        environment.updatePhysics(1 / rate)
    }

    override fun update(currentTime: Time) {
        timeDistribution.update(currentTime, this)
    }

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        initialized = true
        initializeSubscriptions()
    }

    override fun dispose() {
        initialized = false
        subscriptions.dispose()
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        timeDistribution.nextOccurence.dispose()
    }

    private fun initializeSubscriptions() {
        subscriptions.clear()
        conditions.forEach { condition ->
            subscriptions.add(
                condition.getDependencies().merge().subscribe(invokeOnSubscription = false) {
                    timeDistribution.reactToUpdate(environment.simulation.time, this)
                },
            )
        }
    }

    private companion object {
        const val DEFAULT_RATE = 30.0
    }
}
