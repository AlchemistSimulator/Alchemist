/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.EventObservable
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.observation.lifecycle.LifecycleRegistry
import it.unibo.alchemist.model.observation.lifecycle.LifecycleState
import it.unibo.alchemist.model.observation.lifecycle.bindTo
import it.unibo.alchemist.model.physics.PhysicsDependency
import it.unibo.alchemist.model.physics.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.timedistributions.DiracComb
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListSet

/**
 * A global Reaction responsible for updating the physics of an [Dynamics2DEnvironment].
 */
class PhysicsUpdate<T>(
    /**
     * The environment to update.
     */
    val environment: Dynamics2DEnvironment<T>,
    override val timeDistribution: TimeDistribution<T> = DiracComb(DEFAULT_RATE),
) : GlobalReaction<T> {

    constructor(
        environment: Dynamics2DEnvironment<T>,
        updateRate: Double,
    ) : this(environment, DiracComb(updateRate))

    override val outboundDependencies: ListSet<out Dependency> = ListSet.of(PhysicsDependency)
        get() = ImmutableListSet.copyOf(field)

    override val inboundDependencies: ListSet<out Dependency> = ListSet.of()
        get() = ImmutableListSet.copyOf(field)

    override val rate: Double get() = timeDistribution.rate

    override val tau: Time get() = timeDistribution.nextOccurence

    override val rescheduleRequest: EventObservable = EventObservable()

    override val lifecycle: LifecycleRegistry = LifecycleRegistry()

    override var actions: List<Action<T>> = listOf()

    private var validity: Observable<Boolean> = observe(true)

    private var canExecute: Boolean = true

    override var conditions: List<Condition<T>> = listOf()
        set(value) {
            field = value
            field.forEach(Disposable::dispose)

            validity.dispose()

            value.forEach { condition ->
                condition.observeInboundDependencies().merge().bindTo(this) {
                    rescheduleRequest.emit()
                }
            }

            validity = value.takeIf { it.isNotEmpty() }
                ?.map { it.observeValidity() }
                ?.combineLatest { validities -> validities.all { it } }
                ?.map { it.getOrElse { true } } // none means empty set of conditions i.e. always true.
                ?.apply {
                    bindTo(this@PhysicsUpdate) { canExecute = it }
                } ?: observe(true)

            rescheduleRequest.emit()
        }

    override fun compareTo(other: Actionable<T>): Int = tau.compareTo(other.tau)

    override fun canExecute(): Boolean = canExecute

    override fun observeCanExecute(): Observable<Boolean> = validity

    override fun execute() {
        environment.updatePhysics(1 / rate)
        timeDistribution.update(timeDistribution.nextOccurence, true, 1.0, environment)
    }

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        lifecycle.markState(LifecycleState.STARTED)
    }

    override fun dispose() {
        lifecycle.markState(LifecycleState.DESTROYED)
        validity.dispose()
        conditions.forEach(Disposable::dispose)
        rescheduleRequest.dispose()
    }

    private companion object {
        const val DEFAULT_RATE = 30.0
    }
}
