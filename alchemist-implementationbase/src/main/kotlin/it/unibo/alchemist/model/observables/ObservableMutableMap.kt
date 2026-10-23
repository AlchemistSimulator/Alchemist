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
import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observables.util.Observables.currentOrNull
import it.unibo.alchemist.model.observation.Observable
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
open class ObservableMutableMap<K, V>(initial: Map<K, V> = emptyMap()) :
    AbstractObservable<Map<K, V>>(emitOnDistinct = false),
    ObservableMap<K, V> {

    override val current: Map<K, V> get() = backingMap

    /*
     * The observed per-key observables, by key: only keys being observed have an entry.
     * Several per-key observables of the same key may be observed at once, and they are all notified:
     * they are equal, so they are kept in a list and told apart by identity.
     */
    private val observedKeys: MutableMap<K, MutableList<KeyObservable>> = linkedMapOf()

    private var backingMap: PersistentMap<K, V> = initial.toPersistentMap()

    /**
     * Replaces the whole content of this map with [from], like doing a `clear` followed
     * by a `putAll`, * but without notifying per-key observers for keys that remain
     * present (unless their value changes).
     *
     * @param from A map containing the new key-value pairs to populate the map with.
     */
    fun clearAndPutAll(from: Map<K, V>) {
        val keysToRemove = backingMap.keys - from.keys
        val changedEntries = from.filter { (key, value) -> backingMap.valueWouldBeNewOrChanged(key, value) }
        if (keysToRemove.isNotEmpty() || changedEntries.isNotEmpty()) {
            // The whole replacement is applied first, so that per-key observers never see an intermediate map.
            val updated = backingMap.mutate {
                it -= keysToRemove
                it += changedEntries
            }
            backingMap = updated
            keysToRemove.forEach { key -> notifyKeyObservers(key, none()) }
            changedEntries.forEach { (key, value) -> notifyKeyObservers(key, value.some()) }
            notifyMapObserversUnlessChangedSince(updated)
        }
    }

    override fun computeFresh(): Map<K, V> = backingMap

    override fun dispose() {
        super.dispose()
        observedKeys.values.flatten().forEach { it.dispose() }
        observedKeys.clear()
        backingMap = persistentMapOf()
    }

    override operator fun get(key: K): Observable<Option<V>> = KeyObservable(key)

    /**
     * Adds the specified key-value pair to the map. If the key already exists, the value is updated.
     * Observers are notified if the value changes.
     *
     * @param key The key to be added or updated in the map.
     * @param value The value associated with the specified key.
     */
    fun put(key: K, value: V) {
        if (backingMap.valueWouldBeNewOrChanged(key, value)) {
            val updated = backingMap.putting(key, value)
            backingMap = updated
            notifyKeyObservers(key, value.some())
            notifyMapObserversUnlessChangedSince(updated)
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
            val updated = backingMap.removing(key)
            backingMap = updated
            notifyKeyObservers(key, none())
            notifyMapObserversUnlessChangedSince(updated)
        }
        return previous
    }

    /**
     * @see [put]
     */
    operator fun set(key: K, value: V) = put(key, value)

    // A key observer that changed the key again has already notified the newer value: the outdated one stops there.
    private fun notifyKeyObservers(key: K, value: Option<V>) {
        observedKeys[key]?.toList()?.asSequence()?.takeWhile { backingMap.getOrNone(key) == value }?.forEach {
            it.publish(value)
        }
    }

    /*
     * A key observer that changed the map again has already notified the map observers of the newer map,
     * so they are notified only if the map is still the one produced by the change.
     */
    private fun notifyMapObserversUnlessChangedSince(updated: PersistentMap<K, V>) {
        if (backingMap === updated) {
            updateAndNotify(updated)
        }
    }

    /**
     * The observable value of [key] in this map, equal to any other of the same key.
     * While observed, it is among the observed keys of the map, which notifies it of the changes of [key].
     */
    private inner class KeyObservable(private val key: K) : AbstractObservable<Option<V>>(emitOnDistinct = false) {
        override val current: Option<V> get() = backingMap.getOrNone(key)

        private val map get() = this@ObservableMutableMap

        override fun computeFresh(): Option<V> = backingMap.getOrNone(key)

        override fun equals(other: Any?): Boolean =
            other is ObservableMutableMap<*, *>.KeyObservable && other.map === map && other.key == key

        override fun hashCode(): Int = 31 * map.hashCode() + key.hashCode()

        fun publish(value: Option<V>) = updateAndNotify(value)

        override fun startObservingSources() {
            observedKeys.getOrPut(key) { mutableListOf() } += this
        }

        override fun stopObservingSources() {
            observedKeys[key]?.let { observed ->
                observed.removeIf { it === this }
                if (observed.isEmpty()) {
                    observedKeys -= key
                }
            }
        }

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
