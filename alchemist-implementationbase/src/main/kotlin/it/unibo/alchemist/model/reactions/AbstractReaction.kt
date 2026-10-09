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
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.observables.CompositeDisposable
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable

/**
 * Owner-neutral implementation of the reactive state shared by all [Reaction]s.
 *
 * @param T concentration type
 * @param initialOccurrence the occurrence exposed before engine initialization
 */
abstract class AbstractReaction<T>(initialOccurrence: Time) : Reaction<T> {

    override var actions: List<Action<T>> = emptyList()
        set(value) {
            requireOwnership(value, Action<T>::reaction)
            field = value
        }

    override val canExecute: Observable<Boolean> get() = conditionValidity

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            requireOwnership(value, Condition<T>::reaction)
            validateConditions(value)
            field = value
            conditionValidity.dispose()
            conditionValidity = value.map { it.isValid }
                .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                ?: observe(true)
            if (initializedEnvironment != null) {
                initializeSchedulingSubscriptions()
                schedulingInputChanged()
            }
        }

    /*
     * The backing field is needed because replacing conditions must replace and dispose the old combined observable.
     * Computing it in the canExecute getter would create a new observable on every access.
     * Making canExecute itself mutable would also be undesirable: the API promises a read-only property,
     * and subclasses must override it with a fixed val.
     * For reactions overriding canExecute, the default conditionValidity is unused,
     * although its derived observable remains lazy and does not subscribe to condition sources.
     */
    private var conditionValidity: Observable<Boolean> = observe(true)

    private var disposed = false

    protected var initializedEnvironment: Environment<T, *>? = null
        private set

    protected var lastKnownTime = Time.ZERO

    final override val nextOccurrence: Observable<Time>
        field: MutableObservable<Time> = observe(initialOccurrence, false)

    private var schedulingSubscriptions: CompositeDisposable? = null

    final override fun compareTo(other: Reaction<T>): Int =
        nextOccurrence.current.compareTo(other.nextOccurrence.current)

    /** Prepares valid conditions, applies the model mutation, and advances surviving distributed reactions. */
    override fun execute() {
        val firingTime = nextOccurrence.current
        signalConditionsReady()
        performModelMutation()
        if (initializedEnvironment != null && this is TimeDistributedReaction<*>) {
            updateSchedulingAfterFiring(firingTime)
        }
    }

    /** Performs this reaction's model mutation. */
    protected open fun performModelMutation() = actions.forEach(Action<T>::execute)

    /** Notifies conditions immediately before their valid reaction executes. */
    protected fun signalConditionsReady() = conditions.forEach(Condition<T>::beforeReactionFires)

    /** The scheduling information appended by [toString], or `null` when none exists. */
    protected open val rateAsString: String? get() = null

    /** Initializes reactive inputs after the model has been fully assembled. */
    final override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        check(!disposed) { "A disposed reaction cannot be initialized again: $this" }
        lastKnownTime = atTime
        initializedEnvironment = environment
        initializeSchedulingSubscriptions()
        onInitializationComplete(atTime, environment)
        afterInitializationComplete(atTime, environment)
        if (!canExecute.current) {
            suspendScheduling()
        }
    }

    /** Called once reactive scheduling inputs have been activated. */
    protected open fun onInitializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    /** Called after reaction-specific initialization completes. */
    protected open fun afterInitializationComplete(atTime: Time, environment: Environment<T, *>) = Unit

    private fun <E> requireOwnership(elements: List<E>, owner: (E) -> Any) {
        val foreign = elements.filterNot { owner(it) === this }
        require(foreign.isEmpty()) { "$foreign belong to a reaction other than $this" }
    }

    /** Rejects unsupported conditions before they become observable reaction state. */
    protected open fun validateConditions(conditions: List<Condition<T>>) = Unit

    /** Recomputes reaction-specific state before scheduling policy is applied. */
    protected open fun refreshReactionState(currentTime: Time, environment: Environment<T, *>) = Unit

    /**
     * Adds subscriptions for the model inputs controlling this reaction's scheduling.
     *
     * The default policy observes only combined condition validity. Specialized reaction families override this
     * method and subscribe to the narrow semantic inputs used by their scheduling law.
     */
    protected open fun subscribeToSchedulingInputs(subscriptions: CompositeDisposable) {
        subscriptions.add(
            canExecute.subscribe(invokeOnSubscription = false) {
                schedulingInputChanged()
            },
        )
    }

    /** Refreshes scheduling after one directly observed model input changes. */
    protected fun schedulingInputChanged() {
        initializedEnvironment?.let { environment ->
            environment.simulationOrNull
                ?.reactionInvalidated(this)
                ?: updateSchedulingAfterInvalidation(lastKnownTime)
        }
    }

    final override fun updateSchedulingAfterInvalidation(currentTime: Time) {
        val environment = checkNotNull(initializedEnvironment) {
            "Reaction $this was invalidated before initialization"
        }
        lastKnownTime = currentTime
        refreshReactionState(currentTime, environment)
        if (canExecute.current) {
            scheduleAfterInvalidation(currentTime)
        } else {
            suspendScheduling()
        }
    }

    /** Applies scheduling policy after a reactive invalidation without firing the reaction. */
    protected abstract fun scheduleAfterInvalidation(currentTime: Time)

    /** Suspends this reaction without advancing its scheduling policy or consuming another sample. */
    protected open fun suspendScheduling() {
        if (nextOccurrence.current != Time.INFINITY) {
            setNextOccurrence(Time.INFINITY)
        }
    }

    /** Changes the reaction-owned absolute occurrence time. */
    protected fun setNextOccurrence(nextOccurrence: Time) {
        this.nextOccurrence.current = nextOccurrence
    }

    override fun dispose() {
        if (!disposed) {
            disposed = true
            initializedEnvironment = null
            schedulingSubscriptions?.dispose()
            schedulingSubscriptions = null
            conditions.forEach(Condition<T>::dispose)
            conditions = emptyList()
            actions = emptyList()
            val executionEligibility = canExecute
            executionEligibility.dispose()
            if (executionEligibility !== conditionValidity) {
                conditionValidity.dispose()
            }
            nextOccurrence.dispose()
        }
    }

    override fun toString(): String = buildString {
        append(this@AbstractReaction::class.simpleName ?: "<anonymous ${AbstractReaction::class.simpleName}>")
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

    private fun initializeSchedulingSubscriptions() {
        schedulingSubscriptions?.dispose()
        schedulingSubscriptions = CompositeDisposable().also(::subscribeToSchedulingInputs)
    }
}
