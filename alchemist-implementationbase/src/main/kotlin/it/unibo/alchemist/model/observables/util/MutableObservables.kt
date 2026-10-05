/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables.util

import arrow.core.Option
import it.unibo.alchemist.model.observation.MutableObservable

/** Factory and utilities for mutable observables. */
object MutableObservables {
    /**
     * Creates and returns a new instance of a [MutableObservable] initialized with the given value.
     * The resulting observable allows its state to be modified and notifies registered observers of any changes.
     *
     * @param T The type of the value being observed.
     * @param initial The initial value of the observable.
     * @return A new instance of [MutableObservable] initialized with the provided value.
     */
    @JvmOverloads
    @JvmStatic
    fun <T> observe(initial: T, emitOnDistinct: Boolean = true): MutableObservable<T> = object : MutableObservable<T> {
        override val observingCallbacks: MutableMap<Any, List<(T) -> Unit>> = linkedMapOf()

        override var current: T = initial
            set(value) {
                if (!emitOnDistinct || value != field) {
                    field = value
                    observingCallbacks.notifyCurrentObservers(value)
                }
            }

        override val observers: List<Any> get() = observingCallbacks.keys.toList()

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (T) -> Unit) {
            if (invokeOnRegistration) {
                callback(current)
            }
            observingCallbacks[registrant] = observingCallbacks[registrant]?.let {
                it + callback
            } ?: listOf(callback)
        }

        override fun stopWatching(registrant: Any) {
            observingCallbacks.remove(registrant)
        }
    }

    /**
     * Handy method to update the optional[Option] contents of this [MutableObservable].
     * Applies the given function to the value contained by the underlying [Option],
     * if it is empty nothing is computed.
     *
     * @param updateFunc the update function to perform on the value wrapped by the underlying [Option]
     */
    fun <T> MutableObservable<Option<T>>.updateValue(updateFunc: (T) -> T) {
        update { it.map(updateFunc) }
    }
}
