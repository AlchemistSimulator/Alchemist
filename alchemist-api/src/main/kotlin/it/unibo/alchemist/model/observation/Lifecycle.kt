/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observation.LifecycleState.DESTROYED
import it.unibo.alchemist.model.observation.LifecycleState.STARTED

/**
 * Represents the lifecycle state of a component.
 */
enum class LifecycleState {

    /**
     * Destroyed state for a LifecycleOwner. After this state is reached, this Lifecycle will not emit any more events.
     */
    DESTROYED,

    /**
     * Initialized state for a LifecycleOwner.
     */
    INITIALIZED,

    /**
     * Started state for a LifecycleOwner.
     */
    STARTED,

    ;

    /**
     * Checks if the current state is at least the given [state].
     */
    fun isAtLeast(state: LifecycleState): Boolean = this >= state
}

/**
 * An object which has a [Lifecycle].
 */
interface LifecycleOwner {

    /**
     * The lifecycle of the provider.
     */
    val lifecycle: Lifecycle
}

/**
 * Manages the state and listeners.
 */
interface Lifecycle {

    /**
     * Returns the current state of the Lifecycle.
     */
    val currentState: LifecycleState

    /**
     * Adds a LifecycleObserver that will be notified when the LifecycleOwner changes state.
     */
    fun addObserver(observer: (LifecycleState) -> Unit)

    /**
     * Removes the given observer from the observers list.
     */
    fun removeObserver(observer: (LifecycleState) -> Unit)
}

/**
 * A concrete implementation of [Lifecycle] that handles state transitions and observer notification.
 */
class LifecycleRegistry : Lifecycle {

    private val observers = mutableListOf<(LifecycleState) -> Unit>()

    override var currentState: LifecycleState = LifecycleState.INITIALIZED
        private set

    /**
     * Transitions the lifecycle to a new [state] and notifies all observers.
     */
    fun markState(state: LifecycleState) {
        currentState = state
        observers.toList().forEach { it(state) }
    }

    override fun addObserver(observer: (LifecycleState) -> Unit) {
        observers.add(observer)
    }

    override fun removeObserver(observer: (LifecycleState) -> Unit) {
        observers.remove(observer)
    }
}

/**
 * Observes this [Observable] within the context of a [LifecycleOwner]. This
 * method is a memory-safe alternative to [Observable.onChange] in scenarios
 * where it is crucial to avoid that the owner subscription leaks. When the
 * registrant's state reaches [LifecycleState.DESTROYED], the subscription
 * is automatically removed and cleared up. Moreover, the [callback] is only
 * invoked if the lifecycle is in an active state ([STARTED]). Finally,
 * when the lifecycle moves from an inactive state back to active, the
 * [callback] is triggered immediately with the [Observable.current] value.
 *
 * @param lifecycleOwner The object controlling the lifecycle of this subscription.
 * @param callback The action to perform when the observable emits a value.
 * @return a [Disposable] to manually dispose the subscription outside owner's lifecycle.
 */
fun <T> Observable<T>.bindTo(lifecycleOwner: LifecycleOwner, callback: (T) -> Unit): Disposable? =
    lifecycleOwner.takeIf { it.lifecycle.currentState != DESTROYED }?.let {
        val dataListener: (T) -> Unit = { data ->
            // avoid zombie callbacks
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(STARTED)) {
                callback(data)
            }
        }

        var lifecycleObserver: ((LifecycleState) -> Unit)? = null
        lifecycleObserver = { state ->
            when (state) {
                DESTROYED -> {
                    this.stopWatching(lifecycleOwner)
                    lifecycleObserver?.let { lifecycleOwner.lifecycle.removeObserver(it) }
                }
                STARTED -> callback(this.current)
                else -> { /* No action needed for other states */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        // set `invokeOnRegistration = false` because we handle the initial data emission manually below
        // to strictly respect the `isAtLeast(STARTED)` check.
        this.onChange(lifecycleOwner, invokeOnRegistration = false, callback = dataListener)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(STARTED)) {
            callback(this.current)
        }

        return Disposable {
            this@bindTo.stopWatching(dataListener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }
