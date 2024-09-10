/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

/**
 * Represents an observable value.
 *
 * @param T the type of the value
 */
interface Observable<out T> {

    /**
     * Returns the current value of this observable
     * without subscribing to changes.
     */
    val current: T

    /**
     * Subscribes to changes of this observable.
     */
    fun onChange(registrant: Any, callback: (T) -> Unit)

    /**
     * Stops watching the observable.
     */
    fun stopWatching(registrant: Any)

    /**
     * Maps this observable to another one.
     *
     * @param R the type of the new observable
     * @param transform the transformation function
     * @return a new observable
     */
    fun <R> map(transform: (T) -> R): Observable<R> = object : Observable<R> {

        override var current: R = transform(this@Observable.current)

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            callback(current)
            this@Observable.onChange(registrant) { newValue ->
                val transformed = transform(newValue)
                if (transformed != current) {
                    current = transformed
                    callback(transformed)
                }
            }
        }

        override fun stopWatching(registrant: Any) {
            this@Observable.stopWatching(registrant)
        }

        override fun toString(): String =
            "MapObservable($current)[from: ${this@Observable}]"
    }

    /**
     * Merges this observable with another one.
     *
     * @param O the type of the other observable
     * @param R the type of the new observable
     * @param other the other observable
     * @param merge the merge function
     * @return a new observable
     */
    fun <O, R> mergeWith(other: Observable<O>, merge: (T, O) -> R): Observable<R> = object : Observable<R> {

        override var current: R = merge(this@Observable.current, other.current)

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            callback(current)
            listOf(this@Observable, other).forEach {
                it.onChange(registrant) {
                    val newValue = merge(this@Observable.current, other.current)
                    if (newValue != current) {
                        current = newValue
                        callback(newValue)
                    }
                }
            }
        }

        override fun stopWatching(registrant: Any) {
            this@Observable.stopWatching(registrant)
        }

        override fun toString() = "MergeObservable($current)[from: ${this@Observable}, other: $other]"
    }
}

/**
 * Represents a mutable observable value.
 *
 * @param T the type of the value
 */
interface MutableObservable<T> : Observable<T> {

    override var current: T

    /**
     * Sets the value of this observable, notifying all subscribers,
     * and returns the old value.
     */
    fun update(computeNewValue: (T) -> T): T = current.also {
        current = computeNewValue(current)
    }

    companion object {

        /**
         * Creates a new observable with the given initial value.
         *
         * @param initial the initial value
         * @return a new observable
         */
        fun <T> observableOf(initial: T): MutableObservable<T> = object : MutableObservable<T> {

            private val observingCallbacks: MutableMap<Any, List<(T) -> Unit>> = linkedMapOf()
            override var current: T = initial
                set(value) {
                    if (value != field) {
                        field = value
                        observingCallbacks.values.forEach { callbacks -> callbacks.forEach { it(value) } }
                    }
                }

            override fun onChange(registrant: Any, callback: (T) -> Unit) {
                callback(current)
                observingCallbacks.compute(registrant) { _, callbacks -> callbacks.orEmpty() + callback }
            }

            override fun stopWatching(registrant: Any) {
                observingCallbacks.remove(registrant)
            }
        }
    }
}

private val Any?.typeName get() = this?.let { it::class.simpleName } ?: "???"
