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
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

interface MutableObservable0<T> : Observable<T>, ReadWriteProperty<Any, T> {

    val readOnly: Observable<T> get() = this

    fun markChanged()

    private class MappedObservable<B, T>(
        val backend: MutableObservable0<B>,
        val transform: (B) -> T,
    ) : MutableObservable0<T> {

        private var baseValue: B by backend

        private val value: T get() = transform(baseValue)

        override fun onChange(registrant: Any, callback: (T) -> Unit) {
            backend.onChange(registrant) { callback(transform(it)) }
        }

        override fun <R> map(transform: (T) -> R): Observable<R> =
            MappedObservable(this, transform)

        override fun getValue(thisRef: Any, property: KProperty<*>): T = value

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            error("${property.name} in $thisRef is a transformed observable and can not be assigned")
        }

        override fun markChanged() = backend.markChanged()

        override fun toString(): String = "MappedObservable<${baseValue.typeName}->${value.typeName}>($backend)"
    }

    private class MutableObservableImpl<T>(initial: T) : MutableObservable0<T> {
        private val observingProperties: WeakHashMap<Any, List<KMutableProperty<*>>> = WeakHashMap()
        private val observingCallbacks: WeakHashMap<Any, List<(T) -> Unit>> = WeakHashMap()

        private var value: T = initial

        override fun <R> map(transform: (T) -> R): Observable<R> = MappedObservable(this, transform)

        override fun onChange(registrant: Any, callback: (T) -> Unit) {
            observingCallbacks.compute(registrant) { _, callbacks -> callbacks.orEmpty() + callback }
        }

        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            check(property is KMutableProperty<*>) {
                "can not assign property ${property.name} of $thisRef to an observable $this: " +
                    "observables can only be assigned to mutable properties, as their value can change"
            }
            observingProperties.compute(thisRef) { _, properties -> properties.orEmpty() + property }
            return value
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            if (this.value != value) {
                this.value = value
                markChanged()
            }
        }

        override fun markChanged() {
            observingProperties.forEach {
                    (observer, properties) ->
                properties.forEach { it.call(observer, value) }
            }
            observingCallbacks.forEach {
                    (_, callbacks) ->
                callbacks.forEach { it(value) }
            }
        }

        override fun toString(): String = "Observable<${value?.let { it::class.simpleName } ?: "???"}>($value)"
    }

    companion object {

        private val Any?.typeName get() = this?.let { it::class.simpleName } ?: "???"

        @JvmStatic
        fun <T> observing(initial: T): MutableObservable0<T> = MutableObservableImpl(initial)
    }
}
