/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option

/**
 * An observable map whose read-only snapshot is available through [current]. Values can be observed by key through
 * [get].
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
}
