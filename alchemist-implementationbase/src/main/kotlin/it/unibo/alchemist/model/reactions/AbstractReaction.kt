/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observation.CompositeDisposable
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import javax.annotation.Nonnull

/**
 * Owner-neutral implementation of the reactive state shared by all [Reaction]s.
 *
 * @param T concentration type
 * @param initialOccurrence the occurrence exposed before engine initialization
 */
abstract class AbstractReaction<T>(initialOccurrence: Time) : Reaction<T> {

    override var actions: List<Action<T>> = emptyList()

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            validateConditions(value)
            field = value
            canExecute.dispose()
            canExecute = if (conditionsGateScheduling) {
                value.map(Condition<T>::isValid)
                    .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                    ?: MutableObservable.observe(true)
            } else {
                MutableObservable.observe(true)
            }
            initializedEnvironment?.let {
                initializeDependencySubscriptions()
                if (conditionsGateScheduling) {
                    reactToModelUpdate(it)
                }
            }
        }

    private var canExecute: Observable<Boolean> = MutableObservable.observe(true)

    /** Whether installed conditions reactively gate [nextOccurrence]. */
    protected open val conditionsGateScheduling: Boolean = true

    @Transient
    private var dependencySubscriptions: CompositeDisposable? = null

    @Transient
    protected var initializedEnvironment: Environment<T, *>? = null
        private set

    protected var lastKnownTime = Time.ZERO

    private var disposed = false

    /** Whether this reaction has already released its observable state. */
    protected val isDisposed: Boolean get() = disposed

    private val mutableNextOccurrence = MutableObservable.observe(initialOccurrence, false)

    private val observableNextOccurrence = mutableNextOccurrence.map { it }

    final override val nextOccurrence: Observable<Time> get() = observableNextOccurrence

    override fun canExecute(): Observable<Boolean> = canExecute

    final override fun compareTo(other: Reaction<T>): Int =
        nextOccurrence.current.compareTo(other.nextOccurrence.current)

    /** The default execution prepares valid conditions and then applies the model mutation. */
    override fun execute() {
        signalConditionsReady()
        executeReaction()
    }

    /** Performs this reaction's model mutation. */
    protected open fun executeReaction() = actions.forEach(Action<T>::execute)

    /** Notifies conditions immediately before their valid reaction executes. */
    protected fun signalConditionsReady() = conditions.forEach(Condition<T>::reactionReady)

    /** The scheduling information appended by [toString], or `null` when none exists. */
    protected open val rateAsString: String? get() = null

    /** The name used by [toString]. */
    protected val reactionName: String get() = javaClass.simpleName

    /** Initializes reactive inputs after the model has been fully assembled. */
    final override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        check(!disposed) { "A disposed reaction cannot be initialized again: $this" }
        lastKnownTime = atTime
        initializedEnvironment = environment
        initializeDependencySubscriptions()
        onInitializationComplete(atTime, environment)
        afterInitializationComplete(atTime, environment)
        if (conditionsGateScheduling && !canExecute.current) {
            suspendScheduling()
        }
    }

    /** Called once reactive dependencies have been activated. */
    protected open fun onInitializationComplete(@Nonnull atTime: Time, @Nonnull environment: Environment<T, *>) = Unit

    /** Called after reaction-specific initialization completes. */
    protected open fun afterInitializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    /** Rejects unsupported conditions before they become observable reaction state. */
    protected open fun validateConditions(conditions: List<Condition<T>>) = Unit

    /** Recomputes reaction-specific state before scheduling policy is applied. */
    protected open fun refreshReactionState(currentTime: Time, environment: Environment<T, *>) = Unit

    /** Applies scheduling policy after a reactive invalidation without firing the reaction. */
    protected abstract fun updateSchedulingAfterInvalidation(currentTime: Time)

    /** Suspends this reaction without advancing its scheduling policy or consuming another sample. */
    protected open fun suspendScheduling() {
        if (nextOccurrence.current != Time.INFINITY) {
            setNextOccurrence(Time.INFINITY)
        }
    }

    /** Changes the reaction-owned absolute occurrence time. */
    protected fun setNextOccurrence(nextOccurrence: Time) {
        mutableNextOccurrence.current = nextOccurrence
    }

    override fun dispose() {
        if (!disposed) {
            disposed = true
            initializedEnvironment = null
            dependencySubscriptions?.dispose()
            dependencySubscriptions = null
            conditions.forEach(Condition<T>::dispose)
            conditions = emptyList()
            actions = emptyList()
            canExecute.dispose()
            observableNextOccurrence.dispose()
            mutableNextOccurrence.dispose()
        }
    }

    override fun toString(): String = buildString {
        append(reactionName)
        append('@')
        append(nextOccurrence.current)
        append(':')
        append(conditions)
        rateAsString?.let {
            append('-')
            append(it)
        }
        append("->")
        append(actions)
    }

    private fun initializeDependencySubscriptions() {
        dependencySubscriptions?.dispose()
        dependencySubscriptions = CompositeDisposable().apply {
            if (conditionsGateScheduling) {
                conditions.forEach { condition ->
                    add(
                        condition.getDependencies().merge().subscribe(invokeOnSubscription = false) {
                            initializedEnvironment?.let(::reactToModelUpdate)
                        },
                    )
                }
            }
        }
    }

    private fun reactToModelUpdate(environment: Environment<T, *>) {
        val currentTime = environment.simulationOrNull?.time ?: lastKnownTime
        refreshReactionState(currentTime, environment)
        if (canExecute.current) {
            updateSchedulingAfterInvalidation(currentTime)
        } else {
            suspendScheduling()
        }
    }
}
