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
import it.unibo.alchemist.model.observation.Observable.Companion.notifyCurrentObservers
import java.util.Collections

/**
 * An abstract implementation of the [Observable] interface designed to support observables whose states
 * are derived from other data sources. Manages the lifecycle of observation and update propagation while
 * keeping a minimal set of active subscriptions with the underlying observables.
 * Moreover, observation of the sources is enabled if only there are observers registered with this derived observable.
 *
 * @param emitOnDistinct whether to emit only when the new derived value is different from the current one.
 * If so, the first registration computes the current value even when not delivering it, so that the first emission
 * can be compared against it; otherwise, a registration that does not deliver the current value computes nothing.
 * @param T The type of data being observed.
 */
abstract class AbstractObservable<T>(private val emitOnDistinct: Boolean = true) : Observable<T> {
    final override val current: T
        get() = if (isListening) cached.getOrElse { computeFresh() } else computeFresh()

    final override val observers: List<Any>
        get() = callbacks.keys.toList()

    final override val observingCallbacks: Map<Any, List<(T) -> Unit>>
        get() = Collections.unmodifiableMap(callbacks)

    protected var cached: Option<T> = none()

    private val callbacks = LinkedHashMap<Any, List<(T) -> Unit>>()

    // Counts the emissions, so that a notification can tell whether a callback emitted again.
    private var emissions = 0L

    private var isListening = false

    override fun dispose() {
        if (isListening) {
            stopMonitoring()
        }
        callbacks.clear()
        isListening = false
        cached = none()
    }

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (T) -> Unit) {
        val wasEmpty = callbacks.isEmpty()
        callbacks[registrant] = callbacks[registrant].orEmpty() + callback
        if (wasEmpty) {
            isListening = true
            if (invokeOnRegistration || emitOnDistinct) {
                /*
                 * Distinct observables cache the initial value even when not delivering it,
                 * so that the first emission is compared against it.
                 */
                val initial = computeFresh()
                cached = initial.some()
                /*
                 * Starting to monitor may itself publish a value:
                 * subscribing to the sources can make them, or this observable, emit.
                 * Such an emission is notified to every registered callback, including the one just added,
                 * and is newer than initial, which was computed before monitoring started.
                 * Delivering initial afterwards would overwrite it with an outdated value.
                 * The emission count is therefore recorded before starting to monitor and compared right after:
                 * if it changed, the callback already received a newer value, and initial is not delivered.
                 */
                val emissionsBeforeMonitoring = emissions
                // Monitor before notifying, so that source changes made by the callback itself are not missed.
                startMonitoring()
                if (invokeOnRegistration && emissions == emissionsBeforeMonitoring) {
                    callback(initial)
                }
            } else {
                startMonitoring()
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

    /**
     * Computes and returns a fresh value of type [T]. This method is expected to be implemented
     * in derived classes to provide a new value that represents the updated state or computation
     * result of the observable entity.
     *
     * @return a fresh, computed value of type [T]
     */
    protected abstract fun computeFresh(): T

    /**
     * Starts monitoring the sources of this observable, publishing their changes through [updateAndNotify].
     * It is called when the first observer registers, after the current value has been computed if needed:
     * implementations must subscribe to their sources without being notified of their current values.
     */
    protected abstract fun startMonitoring()

    /**
     * Stops monitoring for changes or updates in the implementing class.
     * This method should disable any active observation mechanisms and ensure
     * that resources or listeners associated with monitoring are appropriately released.
     * It is intended to complement the `startMonitoring` function.
     */
    protected abstract fun stopMonitoring()

    protected fun updateAndNotify(newValue: T) {
        val hasChanged = cached.fold(
            ifEmpty = { true },
            ifSome = { it != newValue },
        )
        if (!emitOnDistinct || hasChanged) {
            cached = newValue.some()
            val emission = ++emissions
            // A callback emitting again makes this emission outdated for the remaining callbacks.
            notifyCurrentObservers(newValue) { emissions == emission }
        }
    }
}
