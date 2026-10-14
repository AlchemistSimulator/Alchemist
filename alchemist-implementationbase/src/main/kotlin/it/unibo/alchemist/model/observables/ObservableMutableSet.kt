/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables

import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableSet

/**
 * A mutable observable set implementation, which allows for observing changes to the set and
 * provides notifications when its contents are modified. This class supports addition, removal,
 * and observation of modifications to the set.
 *
 * @param T The type of elements maintained by this set.
 */
class ObservableMutableSet<T>(initial: Iterable<T> = emptyList()) : ObservableSet<T> {
    override val current: Set<T> get() = backing.current.keys

    override val observers: List<Any> get() = observingCallbacks.keys.toList()

    override val observingCallbacks: MutableMap<Any, List<(Set<T>) -> Unit>> = mutableMapOf()

    // Out of order: size is initialized from it.
    private val backing = ObservableMutableMap<T, Boolean>(initial.associateWith { true })

    override val size: Observable<Int> = backing.map { it.keys.size }

    /**
     * Adds an item to the observable set.
     * If the item does not already exist in the set, it will be added, and observers
     * of the set will be notified of the change.
     *
     * @param item The item to be added to the set.
     */
    fun add(item: T) {
        if (item !in backing.current) backing.put(item, true)
    }

    /**
     * Replaces the contents of this set with [items].
     * The observers of the set are notified once, if the contents change, and the observers of an item only if the
     * presence of that item changes.
     *
     * @param items the new contents of this set
     */
    fun clearAndAddAll(items: Set<T>) = backing.clearAndPutAll(items.associateWith { true })

    override fun containsItem(item: T): Observable<Boolean> = backing[item].map { opt -> opt.isSome() }

    override fun dispose() {
        backing.dispose()
        size.dispose()
        observingCallbacks.clear()
    }

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Set<T>) -> Unit) {
        observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
        backing.onChange(this to registrant, invokeOnRegistration) { callback(it.keys) }
    }

    /**
     * Removes the specified item from the observable set.
     * If the item exists in the set, it will be removed, and observers of the set
     * will be notified of the change.
     *
     * @param item The item to be removed from the set.
     * @return true if the set contained the specified [item]
     */
    fun remove(item: T): Boolean = backing.remove(item) != null

    override fun stopWatching(registrant: Any) {
        backing.stopWatching(this to registrant)
        observingCallbacks.remove(registrant)
    }

    /**
     * A companion object for the `ObservableMutableSet` class, providing handy factories.
     */
    companion object {
        /**
         * Creates a new [ObservableMutableSet] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableSet`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableMutableSet] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableMutableSet<T> = ObservableMutableSet<T>(items.toList())

        /**
         * Converts the current list into an observable mutable set.
         * @see ObservableMutableSet
         *
         * @return An instance of `ObservableMutableSet` containing all unique elements from the original list.
         */
        fun <T> List<T>.toObservableSet(): ObservableMutableSet<T> = ObservableMutableSet<T>(this)

        /**
         * Converts the current set into an observable mutable set.
         * @see ObservableMutableSet
         *
         * @return An instance of `ObservableMutableSet` containing all the elements from the original set.
         */
        fun <T> Set<T>.toObservableSet(): ObservableMutableSet<T> = ObservableMutableSet<T>(this)
    }
}
