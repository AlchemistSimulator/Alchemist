/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
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
import java.util.WeakHashMap

/**
 * Represents an observable map with keys of type [K] and values of type [V].
 */
interface ObservableMap<K, out V> : Observable<Map<K, V>> {

    override fun onChange(registrant: Any, callback: (Map<K, V>) -> Unit) =
        onChange(registrant) { map, _, _ -> callback(map) }

    /**
     * Registers the [subscriber] to be notified for any map change.
     */
    fun onChange(subscriber: Any, callback: (Map<K, V>, Option<K>, Option<V>) -> Unit)

    /**
     * Returns an [Observable] of all changes to values associated to the provided [key].
     */
    operator fun get(key: K): Observable<Option<V>>
}

/**
 * An observable map that allows to modify its content.
 */
class ObservableMutableMap<K, V> : ObservableMap<K, V> {

    private val backingMap: MutableMap<K, V> = linkedMapOf()
    private val backingMapOfObservable: MutableMap<K, MutableObservable<Option<V>>> = linkedMapOf()
    private val observers: WeakHashMap<Any, List<(Map<K, V>, Option<K>, Option<V>) -> Unit>> = WeakHashMap()

    /**
     * Puts the given [value] in the map at the given [key],
     * notifying all subscribers to the map and all subscribers to the key.
     */
    fun put(key: K, value: V) {
        val previous = backingMap.put(key, value)
        if (previous != value) {
            getAsMutable(key).replaceWith(value.some())
            observers.values.forEach { callbacks ->
                callbacks.forEach { it(backingMap, key.some(), value.some()) }
            }
        }
    }

    /**
     * Puts the given [value] in the map at the given [key].
     */
    operator fun set(key: K, value: V) = put(key, value)

    /**
     * Removes the value associated to the given [key],
     * notifying all subscribers to the map and all subscribers to the key.
     */
    fun remove(key: K) {
        backingMap.remove(key)
        val previousObservedValue = backingMapOfObservable[key]?.replaceWith(none()) ?: none()
        if (previousObservedValue.isSome()) {
            observers.values.forEach { callbacks ->
                callbacks.forEach { it(backingMap, key.some(), none()) }
            }
        }
    }

    override fun onChange(subscriber: Any, callback: (Map<K, V>, Option<K>, Option<V>) -> Unit) {
        observers.compute(subscriber) { _, callbacks -> callbacks.orEmpty() + callback }
        callback(backingMap, none(), none())
    }

    override operator fun get(key: K): Observable<Option<V>> = getAsMutable(key)

    /**
     * Returns a copy of this map with no observers.
     */
    fun copy() = ObservableMutableMap<K, V>().apply {
        backingMap.putAll(this@ObservableMutableMap.backingMap)
    }

    private fun getAsMutable(key: K): MutableObservable<Option<V>> =
        backingMapOfObservable.getOrPut(key) { MutableObservable.observableOf(none()) }
}
