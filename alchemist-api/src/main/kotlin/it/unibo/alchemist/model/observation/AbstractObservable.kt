/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import java.util.Collections

/**
 * An abstract implementation of the [Observable] interface designed to support observables whose states
 * are derived from other data sources. Manages the lifecycle of observation and update propagation while
 * keeping a minimal set of active subscriptions with the underlying observables.
 * Moreover, observation of the sources is enabled if only there are observers registered with this derived observable.
 *
 * @param emitOnDistinct whether to emit when the new derived value is different from the current one.
 * @param T The type of data being observed.
 */
abstract class AbstractObservable<T>(private val emitOnDistinct: Boolean = true) : Observable<T> {
    protected var cached: Option<T> = none()
    private val callbacks = LinkedHashMap<Any, List<(T) -> Unit>>()

    override val observingCallbacks: Map<Any, List<(T) -> Unit>>
        get() = Collections.unmodifiableMap(callbacks)

    override val observers: List<Any>
        get() = callbacks.keys.toList()
    private var isListening = false

    override val current: T
        get() = if (isListening) cached.getOrElse { computeFresh() } else computeFresh()

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (T) -> Unit) {
        val wasEmpty = callbacks.isEmpty()
        callbacks[registrant] = callbacks[registrant].orEmpty() + callback
        if (wasEmpty) {
            isListening = true
            if (invokeOnRegistration) {
                val initial = computeFresh()
                cached = initial.some()
                callback(initial)
                startMonitoring(true)
            } else {
                startMonitoring(true)
            }
        } else if (invokeOnRegistration) {
            callback(current)
        }
    }

    override fun stopWatching(registrant: Any) {
        callbacks.remove(registrant)
        if (callbacks.isEmpty() && isListening) {
            stopMonitoring()
            isListening = false
            cached = none()
        }
    }

    override fun dispose() {
        if (isListening) {
            stopMonitoring()
        }
        callbacks.clear()
        isListening = false
        cached = none()
    }

    /**
     * Initiates monitoring for changes or updates in the implementing class.
     * This method should enable necessary mechanisms to observe and react to changes
     * based on the specific implementation of the derived class.
     */
    protected abstract fun startMonitoring()

    /**
     * Initiates monitoring for changes or updates in the implementing class.
     * This method should enable necessary mechanisms to observe and react to changes
     * based on the specific implementation of the derived class.
     *
     * @param lazy whether the monitoring should be started lazily (avoiding immediate callbacks)
     */
    protected open fun startMonitoring(lazy: Boolean) {
        startMonitoring()
    }

    /**
     * Stops monitoring for changes or updates in the implementing class.
     * This method should disable any active observation mechanisms and ensure
     * that resources or listeners associated with monitoring are appropriately released.
     * It is intended to complement the `startMonitoring` function.
     */
    protected abstract fun stopMonitoring()

    /**
     * Computes and returns a fresh value of type [T]. This method is expected to be implemented
     * in derived classes to provide a new value that represents the updated state or computation
     * result of the observable entity.
     *
     * @return a fresh, computed value of type [T]
     */
    protected abstract fun computeFresh(): T

    protected fun updateAndNotify(newValue: T) {
        val hasChanged = cached.fold(
            ifEmpty = { true },
            ifSome = { it != newValue }
        )
        if (!emitOnDistinct || hasChanged) {
            cached = newValue.some()
            callbacks.values.flatten().forEach { it(newValue) }
        }
    }
}
