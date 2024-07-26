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

interface ObservableMap<K, out V> {

    fun subscribe(subscriber: Any, onChange: (Map<K, V>) -> Unit)

    fun subscribe(subscriber: Any, key: K, onChange: (V) -> Unit)
}

class ObservableMutableMap<K, V> {

    private val backingMap: MutableMap<K, V> = linkedMapOf()
    private val globalObservers: WeakHashMap<Any, List<(Map<K, V>) -> Unit>> = WeakHashMap()
    private val observers: WeakHashMap<Any, Map<K, List<(V) -> Unit>>> = WeakHashMap()

    fun put(key: K, value: V) {
        val previous = backingMap.put(key, value)
        if (previous != value) {
            globalObservers.values.forEach {
                    callbacks ->
                callbacks.forEach { it(backingMap) }
            }
            observers.values.forEach { callbacks ->
                callbacks[key].orEmpty().forEach { it(value) }
            }
        }
    }

    fun onChange(subscriber: Any, callback: (Map<K, V>) -> Unit) {
        globalObservers.compute(subscriber) { _, callbacks -> (callbacks ?: emptyList()) + callback }
        callback(backingMap)
    }

    operator fun get(key: K): Observable<V> {
        return object : Observable<V> {
            override fun onChange(registrant: Any, callback: (V) -> Unit) {
                observers.compute(registrant) { _, callbacks ->
                    val newCallbacks = callbacks.orEmpty() +
                        newCallbacks[key] = (newCallbacks[key] ?: emptyList()) + callback
                    newCallbacks
                }
                callback(backingMap[key])
            }

            override fun <R> map(transform: (V) -> R): Observable<R> {
                TODO("Not yet implemented")
            }
        }
    }

    fun onChange(subscriber: Any, key: K, onChange: (V) -> Unit) {
        observers.compute(subscriber) { _, callbacks ->
            val newCallbacks = callbacks.orEmpty() +
                newCallbacks[key] = (newCallbacks[key] ?: emptyList()) + onChange
            newCallbacks
        }
        onChange(backingMap[key])
    }
}
