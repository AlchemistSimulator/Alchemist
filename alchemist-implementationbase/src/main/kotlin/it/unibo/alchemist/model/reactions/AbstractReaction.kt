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
import java.util.function.Supplier
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

    override val tau: Observable<Time> get() = timeDistribution.nextOccurence

    override fun canExecute(): Observable<Boolean> = canExecute

    override fun compareTo(other: Actionable<T>): Int = tau.current.compareTo(other.tau.current)

    /**
     * The default execution iterates all actions in order.
     */
    override fun execute() = actions.forEach(Action<T>::execute)

    /**
     * @return a [String] representation of the rate
     */
    protected open val rateAsString: String get() = timeDistribution.rate.toString()

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
    }

    /**
     * Called once reactive dependencies have been activated.
     */
    protected open fun onInitializationComplete(@Nonnull atTime: Time, @Nonnull environment: Environment<T, *>) = Unit

    /**
     * Creates a clone and populates it with cloned actions and conditions.
     */
    protected fun <R : Reaction<T>> makeClone(builder: Supplier<R>): R = builder.get().also { result ->
        val destination = result.node
        result.conditions = conditions.map { it.cloneCondition(destination, result) }
        result.actions = actions.map { it.cloneAction(destination, result) }
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
        timeDistribution.update(currentTime, this)
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
            tau.dispose()
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
        timeDistribution.reactToUpdate(currentTime, this)
    }
}
