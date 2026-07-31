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
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.CompositeDisposable
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.merge

class GlobalTestReaction<T>(val environment: Environment<T, *>, override val timeDistribution: TimeDistribution<T>) :
    GlobalReaction<T> {

    override val inputContext: Context = Context.GLOBAL
    override val outputContext: Context = Context.GLOBAL
    override val tau: Observable<Time> get() = timeDistribution.nextOccurence
    override var actions: List<Action<T>> = emptyList()
    private var validity: Observable<Boolean> = MutableObservable.observe(true)
    private val subscriptions = CompositeDisposable()

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            validity.dispose()
            validity = value
                .map(Condition<T>::isValid)
                .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                ?: MutableObservable.observe(true)
        }

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    override fun canExecute(): Observable<Boolean> = validity

    override fun execute() = actions.forEach(Action<T>::execute)

    override fun update(currentTime: Time) = timeDistribution.update(currentTime, this)

    override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        subscriptions.clear()
        conditions.forEach { condition ->
            subscriptions.add(
                condition.dependencies.merge().subscribe(invokeOnSubscription = false) {
                    timeDistribution.reactToUpdate(environment.simulation.time, this)
                },
            )
        }
    }

    override fun dispose() {
        subscriptions.dispose()
        validity.dispose()
        conditions.forEach(Condition<T>::dispose)
        timeDistribution.nextOccurence.dispose()
    }
}
