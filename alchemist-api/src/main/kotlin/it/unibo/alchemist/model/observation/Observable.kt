/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import java.util.WeakHashMap

interface Observable<out T> {

    fun <R> map(transform: (T) -> R): Observable<R> = object : Observable<R> {

        private var transformed: R? = null
        private var original: T? = null

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            this@Observable.onChange(registrant) { newValue ->
                val transformed = transform(newValue)
                callback(transformed).also {
                    this.transformed = transformed
                    this.original = newValue
                }
            }
        }

        override fun toString(): String =
            "MappedObservable<${original.typeName}->${transformed.typeName}>($original -> $transformed)"
    }

    fun onChange(registrant: Any, callback: (T) -> Unit)
}

interface MutableObservable<T> : Observable<T> {

    fun set(value: T)

    companion object {

        fun <T> observableOf(initial: T): MutableObservable<T> = object : MutableObservable<T> {

            private var currentValue: T = initial
            private val observingCallbacks: WeakHashMap<Any, List<(T) -> Unit>> = WeakHashMap()

            override fun onChange(registrant: Any, callback: (T) -> Unit) {
                observingCallbacks.compute(registrant) { _, callbacks -> callbacks.orEmpty() + callback }
            }

            override fun set(value: T) {
                currentValue = value
                observingCallbacks.values.forEach { callbacks -> callbacks.forEach { it(value) } }
            }
        }
    }
}

private val Any?.typeName get() = this?.let { it::class.simpleName } ?: "???"
