/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

/** An observable set whose read-only snapshot is available through [current], with observable [size] and presence. */
interface ObservableSet<T> : Observable<Set<T>> {

    /** Emits the set size whenever it changes. */
    val size: Observable<Int>

    /**
     * Observes the membership status of a specific item in the set.
     * The returned observable will emit `true` if the item is a member of the set,
     * and `false` otherwise. Emits updates whenever the membership status changes.
     *
     * @param item The item whose membership status is to be observed.
     * @return An observable emitting `true` if the item is in the set, `false` otherwise.
     */
    fun containsItem(item: T): Observable<Boolean>
}
