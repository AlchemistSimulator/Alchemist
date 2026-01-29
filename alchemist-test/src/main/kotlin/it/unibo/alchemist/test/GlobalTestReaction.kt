/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.EventObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableMutableSet
import org.danilopianini.util.ListSet
import org.danilopianini.util.ListSets

class GlobalTestReaction<T>(val environment: Environment<T, *>, override val timeDistribution: TimeDistribution<T>) :
    GlobalReaction<T> {
    override fun compareTo(other: Actionable<T>): Int = tau.compareTo(other.tau)

    override fun canExecute(): Boolean = conditions.all { it.isValid }

    override fun observeCanExecute(): Observable<Boolean> = validity

    override fun execute() = timeDistribution.update(timeDistribution.nextOccurence, true, 1.0, environment)

    override var actions: List<Action<T>> = emptyList()

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            observableConditions.clearAndAddAll(value.toSet())
        }

    private val observableConditions: ObservableMutableSet<Condition<T>> = ObservableMutableSet()

    private val validity = observableConditions.combineLatest({
        it.observeValidity()
    }) { validities -> validities.all { it } }

    override val outboundDependencies: ListSet<out Dependency> = ListSets.emptyListSet()

    override val inboundDependencies: ListSet<out Dependency> = ListSets.emptyListSet()

    override val rescheduleRequest: Observable<Unit> = EventObservable()

    override fun update(currentTime: Time, hasBeenExecuted: Boolean, environment: Environment<T, *>) = Unit

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    override fun dispose() {
        observableConditions.dispose()
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        rescheduleRequest.dispose()
    }
}
