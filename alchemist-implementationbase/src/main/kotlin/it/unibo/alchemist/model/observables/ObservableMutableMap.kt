/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables

import arrow.core.Option
import arrow.core.getOrNone
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observables.util.Observables.currentOrNull
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.Observable.Companion.notifyCurrentObservers
import it.unibo.alchemist.model.observation.ObservableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

/**
 * A class that represents an observable, mutable map. Allows observation of map changes,
 * including addition, removal, modifications of key-value pairs, and observation of
 * particular keys for their value changes.
 *
 * @param K The type of keys maintained by the map.
 * @param V The type of mapped values.
 * @property backingMap The internal mutable map that stores the key-value pairs.
 */
open class ObservableMutableMap<K, V>(initial: Map<K, V> = emptyMap()) : ObservableMap<K, V> {

    override val current: Map<K, V> get() = backingMap

    override val observers: List<Any> get() = observingCallbacks.keys.toList()

    override val observingCallbacks: MutableMap<Any, List<(Map<K, V>) -> Unit>> = linkedMapOf()

    /*
     * Subscribers of the per-key observables, by key.
     * Per-key observables are stateless façades over this map,
     * so any handle for a key reads the live value and shares these subscribers;
     * only keys being observed have an entry.
     */
    private val keyCallbacks: MutableMap<K, MutableMap<Any, List<(Option<V>) -> Unit>>> = linkedMapOf()

    private var backingMap: PersistentMap<K, V> = initial.toPersistentMap()

    /**
     * Replaces the whole content of this map with [from], like doing a `clear` followed
     * by a `putAll`, * but without notifying per-key observers for keys that remain
     * present (unless their value changes).
     *
     * @param from A map containing the new key-value pairs to populate the map with.
     */
    fun clearAndPutAll(from: Map<K, V>) {
        var changed = false
        val keysToRemove = backingMap.keys - from.keys
        if (keysToRemove.isNotEmpty()) {
            backingMap = backingMap.mutate { it -= keysToRemove }
            keysToRemove.forEach { key -> notifyKeyObservers(key, none()) }
            changed = true
        }
        if (from.isNotEmpty()) {
            from.forEach { (key, value) ->
                if (backingMap.valueWouldBeNewOrChanged(key, value)) {
                    backingMap = backingMap.put(key, value)
                    notifyKeyObservers(key, value.some())
                    changed = true
                }
            }
        }
        if (changed) {
            notifyMapObservers()
        }
    }

    override fun dispose() {
        keyCallbacks.clear()
        observingCallbacks.clear()
        backingMap = persistentMapOf()
    }

    override operator fun get(key: K): Observable<Option<V>> = KeyObservable(key)

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Map<K, V>) -> Unit) {
        observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
        if (invokeOnRegistration) callback(backingMap)
    }

    /**
     * Adds the specified key-value pair to the map. If the key already exists, the value is updated.
     * Observers are notified if the value changes.
     *
     * @param key The key to be added or updated in the map.
     * @param value The value associated with the specified key.
     */
    fun put(key: K, value: V) {
        if (backingMap.valueWouldBeNewOrChanged(key, value)) {
            backingMap = backingMap.put(key, value)
            notifyKeyObservers(key, value.some())
            notifyMapObservers()
        }
    }

    /**
     * Removes the mapping for the specified key from the map, if it exists.
     * Notifies observers if the key had an associated value before removal.
     *
     * @param key The key whose mapping is to be removed from the map.
     * @return the previous value associated with the key, or null if the key was not present in the map.
     */
    fun remove(key: K): V? {
        val previous = backingMap[key]
        if (previous != null || key in backingMap) {
            backingMap = backingMap.remove(key)
            notifyKeyObservers(key, none())
            notifyMapObservers()
        }
        return previous
    }

    /**
     * @see [put]
     */
    operator fun set(key: K, value: V) = put(key, value)

    // Per-key subscriptions belong to the per-key observables, which release them independently.
    override fun stopWatching(registrant: Any) {
        observingCallbacks.remove(registrant)
    }

    private fun notifyKeyObservers(key: K, value: Option<V>) {
        KeyObservable(key).notifyCurrentObservers(value) { backingMap.getOrNone(key) == value }
    }

    private fun notifyMapObservers() {
        val snapshot = backingMap
        notifyCurrentObservers(snapshot) { backingMap === snapshot }
    }

    private fun stopWatchingKey(key: K, registrant: Any) {
        keyCallbacks[key]?.let { callbacks ->
            callbacks.remove(registrant)
            if (callbacks.isEmpty()) {
                keyCallbacks.remove(key)
            }
        }
    }

    /**
     * The observable value of [key] in this map: a stateless façade, equal to any other façade of the same key.
     */
    private inner class KeyObservable(private val key: K) : Observable<Option<V>> {
        override val current: Option<V> get() = backingMap.getOrNone(key)

        override val observers: List<Any> get() = observingCallbacks.keys.toList()

        override val observingCallbacks: Map<Any, List<(Option<V>) -> Unit>>
            get() = keyCallbacks[key].orEmpty()

        private val map get() = this@ObservableMutableMap

        override fun equals(other: Any?): Boolean =
            other is ObservableMutableMap<*, *>.KeyObservable && other.map === map && other.key == key

        override fun hashCode(): Int = 31 * map.hashCode() + key.hashCode()

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Option<V>) -> Unit) {
            val callbacks = keyCallbacks.getOrPut(key) { linkedMapOf() }
            callbacks[registrant] = callbacks[registrant].orEmpty() + callback
            if (invokeOnRegistration) callback(current)
        }

        override fun stopWatching(registrant: Any) = stopWatchingKey(key, registrant)

        override fun toString(): String = "KeyObservable($key=$current)"
    }

    /**
     * Simple utility function for the ObservableMaps.
     */
    companion object ObservableMapExtensions {

        /*
         * Keys with nullable values must check the case in which null is added as an element,
         * so the key is present, but the value is undistinguishable from the absence.
         * With this method, the double check is executed only if a null value is explicitly passed
         * AND the key is associated with null.
         */
        private fun <K, V> Map<K, V>.valueWouldBeNewOrChanged(key: K, newValue: V) =
            get(key) != newValue || newValue == null && key !in keys

        /**
         * Inserts or updates a key-value pair in the ObservableMutableMap. If the key already exists,
         * its value is updated using the provided transformation function. If the key does not exist,
         * a new key-value pair is added with the value derived from the transformation function.
         *
         * @param key The key to be added or updated in the map.
         * @param valueUpdate A function that computes the new value based on the current value
         * (or `null` if the key does not exist).
         */
        fun <K, V> ObservableMutableMap<K, V>.upsertValue(key: K, valueUpdate: (V?) -> V) =
            put(key, valueUpdate(this[key].current.getOrNull()))

        /**
         * Updates the value associated with the given key in the map by applying the provided transformation function.
         * If the key does not exist or its current value is `null`, no operation is performed, and `null` is returned.
         *
         * @param key The key whose associated value is to be updated.
         * @param valueUpdate A function that defines how the current value should be updated.
         * @return The updated value if the key exists and its value is not `null`; otherwise, `null`.
         */
        fun <K, V> ObservableMutableMap<K, V>.updateOrNull(key: K, valueUpdate: V.() -> Unit): V? =
            this[key].currentOrNull()?.let { it.apply(valueUpdate).also { new -> this[key] = new } }
    }
}
