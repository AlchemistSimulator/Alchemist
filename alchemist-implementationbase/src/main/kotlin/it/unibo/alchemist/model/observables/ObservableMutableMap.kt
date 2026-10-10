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
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.notifyCurrentObservers
import it.unibo.alchemist.model.observation.MutableObservable
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.Observable.ObservableExtensions.currentOrNull
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

    private var backingMap: PersistentMap<K, V> = initial.toPersistentMap()
    private val keyObservables: MutableMap<K, MutableObservable<Option<V>>> = linkedMapOf()
    override val observingCallbacks: MutableMap<Any, List<(Map<K, V>) -> Unit>> = linkedMapOf()

    override val current: Map<K, V> get() = backingMap

    override val observers: List<Any> get() = observingCallbacks.keys.toList()

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
        if (backingMap.valueWouldBeNewOrChanged(key, value)) {
            backingMap = backingMap.put(key, value)
            getAsMutable(key).update { value.some() }
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
            keyObservables[key]?.update { none() }
            notifyMapObservers()
        }
        return previous
    }

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
            keysToRemove.forEach { key ->
                keyObservables[key]?.update { none() }
            }
            changed = true
        }
        if (from.isNotEmpty()) {
            from.forEach { (key, value) ->
                if (backingMap.valueWouldBeNewOrChanged(key, value)) {
                    backingMap = backingMap.put(key, value)
                    getAsMutable(key).update { value.some() }
                    changed = true
                }
            }
        }
        if (changed) {
            notifyMapObservers()
        }
    }

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Map<K, V>) -> Unit) {
        observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
        if (invokeOnRegistration) callback(backingMap)
    }

    override fun stopWatching(registrant: Any) {
        observingCallbacks.remove(registrant)
        with(keyObservables.iterator()) {
            while (hasNext()) {
                val (key, obs) = next()
                obs.stopWatching(registrant)
                // Observables of present keys hold their values: only unobserved, absent keys can be dropped.
                if (obs.observers.isEmpty() && key !in backingMap) {
                    remove()
                }
            }
        }
    }

    override fun dispose() {
        keyObservables.values.forEach { it.dispose() }
        keyObservables.clear()
        observingCallbacks.keys.toList().forEach(::stopWatching)
        observingCallbacks.clear()
        backingMap = persistentMapOf()
    }

    override operator fun get(key: K): Observable<Option<V>> = getAsMutable(key)

    /**
     * @see [put]
     */
    operator fun set(key: K, value: V) = put(key, value)

    private fun notifyMapObservers() {
        val snapshot = backingMap
        observingCallbacks.notifyCurrentObservers(snapshot)
    }

    private fun getAsMutable(key: K): MutableObservable<Option<V>> = keyObservables.getOrPut(key) {
        observe(none())
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
        fun <K, V> ObservableMutableMap<K, V>.upsertValue(key: K, valueUpdate: (V?) -> V) {
            getAsMutable(key).update {
                valueUpdate(it.getOrNull()).apply { put(key, this) }.some()
            }
        }

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
