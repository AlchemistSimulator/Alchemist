/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.none
import arrow.core.some
import java.util.WeakHashMap

/**
 * Represents an observable value.
 *
 * @param T the type of the value
 */
interface Observable<out T> {

    /**
     * Maps this observable to another one.
     *
     * @param R the type of the new observable
     * @param transform the transformation function
     * @return a new observable
     */
    fun <R> map(transform: (T) -> R): Observable<R> = object : Observable<R> {

        private var transformed: R? = null
        private var original: T? = null

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            this@Observable.onChange(registrant) { newValue ->
                val transformed = transform(newValue)
                if (transformed != this.transformed) {
                    callback(transformed).also {
                        this.transformed = transformed
                        this.original = newValue
                    }
                }
            }
        }

        override fun toString(): String =
            "MapObservable<${original.typeName}->${transformed.typeName}>($original -> $transformed)"
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

        private var merged: R? = null
        private var original: Option<T> = none()
        private var otherValue: Option<O> = none()

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            fun update() {
                val original = original
                val otherValue = otherValue
                original.onSome { v1 ->
                    otherValue.onSome { v2 ->
                        val result = merge(v1, v2)
                        merged = result
                        callback(result)
                    }
                }
            }
            this@Observable.onChange(registrant) { newValue ->
                original = newValue.some()
                update()
            }
            other.onChange(registrant) { newValue ->
                otherValue = newValue.some()
                update()
            }
        }

        override fun toString(): String {
            fun Option<*>.extract(): Any? = when (this) {
                None -> null
                is Some -> value
            }
            val original = original.extract()
            val otherValue = otherValue.extract()
            return "MergeObservable<${
                original.typeName
            }+${
                otherValue.typeName
            }->${
                merged.typeName
            }>($original + $other -> $merged)"
        }
    }

    /**
     * Subscribes to changes of this observable.
     */
    fun onChange(registrant: Any, callback: (T) -> Unit)
}

/**
 * Represents a mutable observable value.
 *
 * @param T the type of the value
 */
interface MutableObservable<T> : Observable<T> {

    /**
     * Sets the value of this observable, notifying all subscribers,
     * and returns the old value.
     *
     * @param value the new value
     */
    fun replaceWith(value: T): T

    companion object {

        /**
         * Creates a new observable with the given initial value.
         *
         * @param initial the initial value
         * @return a new observable
         */
        fun <T> observableOf(initial: T): MutableObservable<T> = object : MutableObservable<T> {

            private var currentValue: T = initial
            private val observingCallbacks: WeakHashMap<Any, List<(T) -> Unit>> = WeakHashMap()

            override fun onChange(registrant: Any, callback: (T) -> Unit) {
                observingCallbacks.compute(registrant) { _, callbacks -> callbacks.orEmpty() + callback }
                callback(currentValue)
            }

            override fun replaceWith(value: T): T = when {
                value == currentValue -> currentValue
                else -> {
                    val previous = currentValue
                    currentValue = value
                    observingCallbacks.values.forEach { callbacks -> callbacks.forEach { it(value) } }
                    previous
                }
            }
        }
    }
}

private val Any?.typeName get() = this?.let { it::class.simpleName } ?: "???"
