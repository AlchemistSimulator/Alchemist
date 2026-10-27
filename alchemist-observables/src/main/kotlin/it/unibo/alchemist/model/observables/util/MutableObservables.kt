/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables.util

import it.unibo.alchemist.model.observables.AbstractObservable
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
    fun <T> observe(initial: T, emitOnDistinct: Boolean = true): MutableObservable<T> =
        SimpleMutableObservable(initial, emitOnDistinct)

    /**
     * Creates a [MutableObservable] initialized with [initial], like [observe],
     * whose [current][MutableObservable.current] value can be read from any thread,
     * always returning the latest value set.
     * Only reading the current value is shared: setting it, registering observers,
     * and deriving further observables must still happen on a single thread.
     *
     * @param T The type of the value being observed.
     * @param initial The initial value of the observable.
     * @param emitOnDistinct whether to notify observers only when the value set differs from the current one.
     * @return A new instance of [MutableObservable] initialized with the provided value.
     */
    @JvmOverloads
    @JvmStatic
    fun <T> observeShareable(initial: T, emitOnDistinct: Boolean = true): MutableObservable<T> =
        object : SimpleMutableObservable<T>(initial, emitOnDistinct) {
            @Volatile
            override var value: T = initial
        }

    private open class SimpleMutableObservable<T>(initial: T, emitOnDistinct: Boolean = true) :
        AbstractObservable<T>(emitOnDistinct),
        MutableObservable<T> {
        override var current: T
            get() = value
            set(newValue) {
                if (!emitOnDistinct || newValue != value) {
                    value = newValue
                    updateAndNotify(newValue)
                }
            }

        protected open var value: T = initial

        override fun computeFresh(): T = value
    }
}
