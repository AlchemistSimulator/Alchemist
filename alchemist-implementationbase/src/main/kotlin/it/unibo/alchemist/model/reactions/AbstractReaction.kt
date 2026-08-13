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
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.observation.CompositeDisposable
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.model.timedistributions.AbstractDistribution
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.SimpleNetworkArrivals
import it.unibo.alchemist.model.timedistributions.Trigger
import it.unibo.alchemist.model.timedistributions.WeibullTime
import javax.annotation.Nonnull

/**
 * Partial implementation of a [Reaction] whose scheduling is driven by observable model state.
 *
 * @param T concentration type
 */
abstract class AbstractReaction<T>(
    final override val node: Node<T>,
    final override val timeDistribution: TimeDistribution<T>,
) : Reaction<T> {

    override var actions: List<Action<T>> = emptyList()
        set(value) {
            field = value
            outputContext = value.fold(Context.LOCAL) { context, action ->
                Context.getWider(context, action.getContext())
            }
        }

    override var conditions: List<Condition<T>> = emptyList()
        set(value) {
            field = value
            inputContext =
                value.fold(Context.LOCAL) { context, condition ->
                    Context.getWider(context, condition.getContext())
                }
            canExecute.dispose()
            canExecute = value
                .map(Condition<T>::isValid)
                .reduceOrNull { left, right -> left.mergeWith(right) { a, b -> a && b } }
                ?: MutableObservable.observe(true)
            initializedEnvironment?.let {
                initializeDependencySubscriptions()
                reactToModelUpdate(it)
            }
        }

    final override var inputContext: Context = Context.LOCAL
        private set

    final override var outputContext: Context = Context.LOCAL
        private set

    private var canExecute: Observable<Boolean> = MutableObservable.observe(true)

    @Transient
    private var dependencySubscriptions: CompositeDisposable? = null

    @Transient
    private var initializedEnvironment: Environment<T, *>? = null

    private var lastKnownTime = Time.ZERO

    private var disposed = false

    private var newlyInstantiatedAt: Time? = null

    private val mutableNextOccurrence = MutableObservable.observe(timeDistribution.startTime, false)

    private val observableNextOccurrence = mutableNextOccurrence.map { it }

    private var previousRate: Double? = null

    override val tau: Observable<Time> get() = observableNextOccurrence

    override val rate: Double
        get() = timeDistribution.defaultReactionRate

    override fun canExecute(): Observable<Boolean> = canExecute

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    /**
     * The default execution iterates all actions in order.
     */
    override fun execute() = actions.forEach(Action<T>::execute)

    /**
     * @return a [String] representation of the rate
     */
    protected open val rateAsString: String get() = rate.toString()

    /**
     * @return the name used by [toString]
     */
    protected val reactionName: String get() = javaClass.simpleName

    final override fun initializationComplete(atTime: Time, environment: Environment<T, *>) {
        check(!disposed) { "A disposed reaction cannot be initialized again: $this" }
        lastKnownTime = atTime
        initializedEnvironment = environment
        initializeDependencySubscriptions()
        onInitializationComplete(atTime, environment)
        newlyInstantiatedAt?.let { cloneTime ->
            val schedulingTime = maxOf(cloneTime, atTime)
            updateInternalStatus(schedulingTime, false, environment)
            initializeNewProgramScheduling(schedulingTime)
            newlyInstantiatedAt = null
        }
    }

    /** Whether this reaction is awaiting initialization as a newly instantiated program. */
    protected val isNewlyInstantiatedProgram: Boolean get() = newlyInstantiatedAt != null

    /**
     * Called once reactive dependencies have been activated.
     */
    protected open fun onInitializationComplete(@Nonnull atTime: Time, @Nonnull environment: Environment<T, *>) = Unit

    /**
     * Creates a clone and populates it with cloned actions and conditions.
     */
    protected fun <R : AbstractReaction<T>> makeClone(
        node: Node<T>,
        currentTime: Time,
        builder: (TimeDistribution<T>) -> R,
    ): R {
        val freshGenerator = timeDistribution.newInstanceOn(node)
        check(freshGenerator !== timeDistribution) {
            "The time distribution of $this returned itself from newInstanceOn($node)"
        }
        return prepareClone(builder(freshGenerator), currentTime)
    }

    /**
     * Populates a freshly constructed specialized reaction with cloned actions and conditions.
     */
    protected fun <R : AbstractReaction<T>> prepareClone(result: R, currentTime: Time): R = result.also { clone ->
        val destination = clone.node
        clone.conditions = conditions.map { condition -> condition.cloneCondition(destination, clone) }
        clone.actions = actions.map { action -> action.cloneAction(destination, clone) }
        clone.newlyInstantiatedAt = currentTime
    }

    final override fun update(currentTime: Time) {
        if (disposed) {
            return
        }
        val environment = checkNotNull(initializedEnvironment) {
            "Reaction $this was advanced before initialization"
        }
        lastKnownTime = currentTime
        updateInternalStatus(currentTime, true, environment)
        updateScheduling(currentTime, true)
    }

    /**
     * Recomputes reaction-specific state before its time distribution is updated.
     *
     * @param currentTime current simulation time
     * @param hasBeenExecuted whether this reaction's scheduled event has fired
     * @param environment current environment
     */
    protected abstract fun updateInternalStatus(
        currentTime: Time,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>,
    )

    /**
     * Overrides the automatically inferred input context.
     */
    protected fun setInputContext(context: Context) {
        inputContext = context
    }

    /**
     * Overrides the automatically inferred output context.
     */
    protected fun setOutputContext(context: Context) {
        outputContext = context
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
        append(tau.current)
        append(':')
        append(conditions)
        append('-')
        append(rateAsString)
        append("->")
        append(actions)
    }

    private fun initializeDependencySubscriptions() {
        dependencySubscriptions?.dispose()
        dependencySubscriptions = CompositeDisposable().apply {
            conditions.forEach { condition ->
                add(
                    condition.getDependencies().merge().subscribe(invokeOnSubscription = false) {
                        initializedEnvironment?.let(::reactToModelUpdate)
                    },
                )
            }
        }
    }

    private fun reactToModelUpdate(environment: Environment<T, *>) {
        val currentTime = environment.simulationOrNull?.time ?: lastKnownTime
        updateInternalStatus(currentTime, false, environment)
        updateScheduling(currentTime, false)
    }

    /**
     * Applies this reaction's scheduling policy after execution or reactive invalidation.
     *
     * The default policy draws a new delay after execution. Exponential generators additionally preserve their
     * sampled exponential variate when the reaction rate changes, rescaling the remaining delay to the new rate.
     *
     * @param currentTime current simulation time
     * @param hasBeenExecuted whether this reaction's scheduled event fired
     */
    protected open fun updateScheduling(currentTime: Time, hasBeenExecuted: Boolean) {
        val schedulingTime = maxOf(currentTime, timeDistribution.startTime)
        when (val distribution = timeDistribution) {
            is Trigger<*> -> if (hasBeenExecuted) setNextOccurrence(Time.INFINITY)
            is ExponentialTime<*> -> updateExponentialScheduling(distribution, schedulingTime, hasBeenExecuted)
            else -> if (hasBeenExecuted) scheduleSampleAfter(schedulingTime)
        }
    }

    /** Starts a newly instantiated program without interpreting the initialization as a previous occurrence. */
    protected open fun initializeNewProgramScheduling(currentTime: Time) {
        if (timeDistribution is Trigger<*>) {
            setNextOccurrence(maxOf(currentTime, timeDistribution.startTime))
        } else {
            updateScheduling(currentTime, true)
        }
    }

    /** Schedules a newly sampled delay after [currentTime]. */
    protected fun scheduleSampleAfter(currentTime: Time) {
        setNextOccurrence(currentTime.plus(validatedSample()))
    }

    /** Changes the reaction-owned absolute occurrence time. */
    protected fun setNextOccurrence(nextOccurrence: Time) {
        mutableNextOccurrence.current = nextOccurrence
    }

    private fun updateExponentialScheduling(
        distribution: ExponentialTime<*>,
        currentTime: Time,
        hasBeenExecuted: Boolean,
    ) {
        val newRate = rate
        val oldRate = previousRate
        check(!newRate.isNaN() && oldRate?.isNaN() != true) { "Reaction propensity cannot be NaN" }
        when {
            newRate == 0.0 -> setNextOccurrence(Time.INFINITY)
            hasBeenExecuted || oldRate == null || oldRate == 0.0 -> {
                val baseRate = distribution.lambda
                val sampledDelay = validatedSample().let { sample ->
                    if (baseRate == newRate) sample else sample.times(baseRate / newRate)
                }
                setNextOccurrence(currentTime.plus(sampledDelay))
            }
            oldRate != newRate -> {
                val remaining = tau.current.minus(currentTime)
                setNextOccurrence(currentTime.plus(remaining.times(oldRate / newRate)))
            }
        }
        previousRate = newRate
    }

    private fun validatedSample(): Time = timeDistribution.sample().also { sample ->
        check(sample.isFinite && sample >= Time.ZERO) {
            "$timeDistribution generated an invalid delay: $sample"
        }
    }

    private val TimeDistribution<T>.startTime: Time
        get() = (this as? AbstractDistribution<*>)?.startTime ?: Time.ZERO

    private val TimeDistribution<T>.defaultReactionRate: Double
        get() = when (this) {
            is DiracComb<*> -> frequency
            is ExponentialTime<*> -> lambda
            is AnyRealDistribution<*> -> mean
            is WeibullTime<*> -> mean
            is SimpleNetworkArrivals<*> -> expectedRate
            else -> Double.NaN
        }
}
