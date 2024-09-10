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
        onChange(registrant) { map, _, _, _ -> callback(map) }

    fun onChange(
        registrant: Any,
        callback: (map: Map<K, V>, key: Option<K>, previousValue: Option<V>, newValue: Option<V>) -> Unit,
    )

    /**
     * Returns an [Observable] of all changes to values associated to the provided [key].
     */
    operator fun get(key: K): Observable<Option<V>>
}

/**
 * An observable map that allows to modify its content.
 */
class ObservableMutableMap<K, V> : ObservableMap<K, V> {

    override val current: MutableMap<K, V> = linkedMapOf()
    private val keyObservables: MutableMap<K, MutableObservable<Option<V>>> = linkedMapOf()
    private val observers: WeakHashMap<Any, List<(Map<K, V>, Option<K>, Option<V>, Option<V>) -> Unit>> = WeakHashMap()

    /**
     * Puts the given [value] in the map at the given [key],
     * notifying all subscribers to the map and all subscribers to the key.
     */
    fun put(key: K, value: V) {
        /*
         * Distinguish between null values in the map and missing keys
         * key is absent -> none()
         * key is present with null value -> some(null)
         * key is present with non-null value -> some(value)
         */
        val containedKey = current.containsKey(key)
        val previous = current.put(key, value).let {
            @Suppress("UNCHECKED_CAST") // It can be null only if V is nullable
            if (containedKey) (it as V).some() else none()
        }
        if (previous != value) {
            getAsMutable(key).current = value.some()
            observers.values.forEach { callbacks ->
                callbacks.forEach { it(current, key.some(), previous, value.some()) }
            }
        }
    }

    operator fun plus(entry: Pair<K, V>) = put(entry.first, entry.second)

    operator fun minus(key: K) = remove(key)

    /**
     * Puts the given [value] in the map at the given [key].
     */
    operator fun set(key: K, value: V) = put(key, value)

    /**
     * Removes the value associated to the given [key],
     * notifying all subscribers to the map and all subscribers to the key.
     */
    fun remove(key: K) {
        current.remove(key)
        val previousObservedValue = keyObservables[key]?.update { none() } ?: none()
        if (previousObservedValue.isSome()) {
            observers.values.forEach { callbacks ->
                callbacks.forEach { it(current, key.some(), previousObservedValue, none()) }
            }
        }
    }

    override fun onChange(
        registrant: Any,
        callback: (map: Map<K, V>, key: Option<K>, previousValue: Option<V>, newValue: Option<V>) -> Unit,
    ) {
        callback(current, none(), none(), none())
        observers.compute(registrant) { _, callbacks -> callbacks.orEmpty() + callback }
    }

    override fun stopWatching(registrant: Any) {
        TODO("Not yet implemented")
    }

    override operator fun get(key: K): Observable<Option<V>> = getAsMutable(key)

    /**
     * Returns a copy of this map with no observers.
     */
    fun copy() = ObservableMutableMap<K, V>().apply {
        current.putAll(this@ObservableMutableMap.current)
    }

    private fun getAsMutable(key: K): MutableObservable<Option<V>> =
        keyObservables.getOrPut(key) { MutableObservable.observableOf(none()) }
}
