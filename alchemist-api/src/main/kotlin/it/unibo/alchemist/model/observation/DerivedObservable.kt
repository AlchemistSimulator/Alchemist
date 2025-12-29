/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import java.util.Collections

/**
 * An abstract implementation of the `Observable` interface designed to support observables whose states
 * are derived from other data sources. Manages the lifecycle of observation and update propagation.
 *
 * @param T The type of data being observed.
 */
abstract class DerivedObservable<T> : Observable<T> {
    private val callbacks = LinkedHashMap<Any, List<(T) -> Unit>>()

    private var cached: Option<T> = none()

    private var isListening = false

    override val observingCallbacks: Map<Any, List<(T) -> Unit>>
        get() = Collections.unmodifiableMap(callbacks)

    override val observers: List<Any>
        get() = callbacks.keys.toList()

    @Suppress("UNCHECKED_CAST")
    override val current: T
        get() = if (isListening && cached.isSome()) {
            cached.getOrNull()!!
        } else {
            computeFresh()
        }

    override fun onChange(registrant: Any, callback: (T) -> Unit) {
        val wasEmpty = callbacks.isEmpty()
        callbacks[registrant] = callbacks[registrant].orEmpty() + callback
        if (wasEmpty) {
            val initial = computeFresh()
            cached = initial.some()
            callback(initial)

            isListening = true
            startMonitoring()
        } else {
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

    protected abstract fun startMonitoring()

    protected abstract fun stopMonitoring()

    protected abstract fun computeFresh(): T

    protected fun updateAndNotify(newValue: T) {
        if (newValue != cached) {
            cached = newValue.some()
            callbacks.values.forEach { cs ->
                cs.forEach { it(newValue) }
            }
        }
    }
}
