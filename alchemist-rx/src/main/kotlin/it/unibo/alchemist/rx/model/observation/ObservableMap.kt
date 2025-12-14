/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.observation

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import java.util.Collections

/**
 * Represents an observable map that allows observation of changes to its contents,
 * as well as observation of keys (i.e. addition, removal and content changes)
 *
 * @param K The type of keys maintained by the map.
 * @param V The type of mapped values.
 */
interface ObservableMap<K, V> : Observable<Map<K, V>> {

    /**
     * Retrieves the observable representation of the value associated with the specified key
     * in the observable map. The resulting observable emits updates whenever the value for
     * the given key changes, is added, or is removed.
     *
     * @param key The key whose associated value is to be retrieved.
     * @return An observable emitting an optional value. If the key is present and associated
     *         with a value, the value is wrapped in `Option.Some`. If the key is absent, it
     *         emits `Option.None`.
     */
    operator fun get(key: K): Observable<Option<V>>

    /**
     * Converts the observable map into a standard, non-observable map.
     * The returned map reflects the contents of the observable map at the time of invocation,
     * and it's unmodifiable.
     *
     * @return An unmodifiable map containing the key-value pairs of the observable map.
     */
    fun asMap(): Map<K, V>
}

/**
 * A class that represents an observable, mutable map. Allows observation of map changes,
 * including addition, removal, modifications of key-value pairs, and observation of
 * particular keys for their value changes.
 *
 * @param K The type of keys maintained by the map.
 * @param V The type of mapped values.
 * @property backingMap The internal mutable map that stores the key-value pairs.
 */
open class ObservableMutableMap<K, V>(private val backingMap: MutableMap<K, V> = linkedMapOf()) : ObservableMap<K, V> {

    private val keyObservables: MutableMap<K, MutableObservable<Option<V>>> = linkedMapOf()
    private val mapObservers: MutableMap<Any, List<(Map<K, V>) -> Unit>> = linkedMapOf()

    override val current: Map<K, V> = Collections.unmodifiableMap(backingMap)

    override val observers: List<Any> = mapObservers.keys.toList()

    init {
        if (backingMap.isNotEmpty()) {
            backingMap.forEach { (key, value) ->
                keyObservables[key] = observe(value.some())
            }
        }
    }

    /**
     * Adds the specified key-value pair to the map. If the key already exists, the value is updated.
     * Observers are notified if the value changes.
     *
     * @param key The key to be added or updated in the map.
     * @param value The value associated with the specified key.
     */
    fun put(key: K, value: V) {
        val previous: V? = backingMap.put(key, value)
        if (previous != value) {
            getAsMutable(key).update { value.some() }
            notifyMapObservers()
        }
    }

    /**
     * Removes the mapping for the specified key from the map, if it exists.
     * Notifies observers if the key had an associated value before removal.
     *
     * @param key The key whose mapping is to be removed from the map.
     */
    fun remove(key: K) {
        backingMap.remove(key)
        val previousObservedValue = keyObservables[key]?.update { none() } ?: none()
        if (previousObservedValue.isSome()) notifyMapObservers()
    }

    override fun onChange(registrant: Any, callback: (Map<K, V>) -> Unit) {
        mapObservers[registrant] = mapObservers[registrant].orEmpty() + callback
        callback(current.toMap())
    }

    override fun stopWatching(registrant: Any) {
        mapObservers.remove(registrant)
        with(keyObservables.iterator()) {
            while (hasNext()) {
                val (_, obs) = next()
                obs.stopWatching(registrant)
                if (obs.observers.isEmpty()) {
                    remove()
                }
            }
        }
    }

    override fun dispose() {
        keyObservables.values.forEach { it.dispose() }
        mapObservers.keys.forEach(::stopWatching)
    }

    /**
     * Creates and returns a copy of the current ObservableMutableMap.
     *
     * @return A new instance of ObservableMutableMap containing the same key-value pairs as the original.
     */
    fun copy(): ObservableMutableMap<K, V> = ObservableMutableMap(backingMap.toMutableMap())

    override fun asMap(): Map<K, V> = Collections.unmodifiableMap(backingMap)

    override operator fun get(key: K): Observable<Option<V>> = getAsMutable(key)

    /**
     * @see [put]
     */
    operator fun set(key: K, value: V) = put(key, value)

    /**
     * @see [put]
     */
    operator fun plus(entry: Pair<K, V>) = put(entry.first, entry.second)

    /**
     * @see [remove]
     */
    operator fun minus(key: K) = remove(key)

    private fun notifyMapObservers() = mapObservers.values.forEach { callbacks ->
        callbacks.forEach { it(current.toMap()) }
    }

    private fun getAsMutable(key: K): MutableObservable<Option<V>> = keyObservables.getOrPut(key) { observe(none()) }

    /**
     * Simple utility function for the ObservableMaps.
     */
    companion object ObservableMapExtensions {

        /**
         * Inserts or updates a key-value pair in the ObservableMutableMap. If the key already exists,
         * its value is updated using the provided transformation function. If the key does not exist,
         * a new key-value pair is added with the value derived from the transformation function.
         *
         * @param key The key to be added or updated in the map.
         * @param valueUpdate A function that computes the new value based on the current value
         * (or `null` if the key does not exist).
         */
        fun <K, V> ObservableMutableMap<K, V>.upsertValue(key: K, valueUpdate: (V?) -> V) {
            getAsMutable(key).update {
                valueUpdate(it.getOrNull()).apply { put(key, this) }.some()
            }
        }
    }
}
