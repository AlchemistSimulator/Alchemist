/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.getOrElse
import java.util.Collections

/**
 * Represents a set that allows observation of its contents and provides notifications on changes.
 * This interface supports observing the entire set, individual item membership, and various utility operations.
 */
interface ObservableSet<T> : Observable<Set<T>> {

    /**
     * An observable that emits the current size of the set.
     */
    val observableSize: Observable<Int>

    /**
     * Observes changes to the set and triggers the provided callback function whenever the set changes.
     * The callback will be invoked with the current set of items as a parameter.
     *
     * @param registrant The object registering for change notifications. Used to manage the lifecycle of the observer.
     * @param callback The function to be invoked whenever the set changes. It receives the current set of items as
     *                 a parameter.
     */
    override fun onChange(registrant: Any, callback: (Set<T>) -> Unit)

    /**
     * Observes the membership status of a specific item in the set.
     * The returned observable will emit `true` if the item is a member of the set,
     * and `false` otherwise. Emits updates whenever the membership status changes.
     *
     * @param item The item whose membership status is to be observed.
     * @return An observable emitting `true` if the item is in the set, `false` otherwise.
     */
    fun observeMembership(item: T): Observable<Boolean>

    /**
     * Returns an unmodifiable view of the current set of items in the observable set.
     *
     * @return A `Set` containing all the items currently in the observable set.
     */
    fun toSet(): Set<T>

    /**
     * Returns an unmodifiable view of the current list of items in the observable set.
     *
     * @return A [List] containing all the items currently in the observable set.
     */
    fun toList(): List<T>

    /**
     * Creates and returns a copy of the current ObservableSet.
     *
     * @return A new ObservableSet containing the same elements as the current set.
     */
    fun copy(): ObservableSet<T>

    /**
     * Checks if the specified item is present in the observable set.
     *
     * @param item The item to be checked for presence in the set.
     * @return `true` if the item exists in the set, `false` otherwise.
     */
    operator fun contains(item: T): Boolean

    /**
     * A companion object for the [ObservableSet] class, providing a simple factory.
     */
    companion object {

        /**
         * Creates a new [ObservableSet] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableSet`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableSet] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableSet<T> = ObservableMutableSet(*items)
    }
}

/**
 * A mutable observable set implementation, which allows for observing changes to the set and
 * provides notifications when its contents are modified. This class supports addition, removal,
 * and observation of modifications to the set.
 *
 * @param T The type of elements maintained by this set.
 */
class ObservableMutableSet<T> : ObservableSet<T> {

    private val backing = ObservableMutableMap<T, Boolean>()

    override val observableSize: Observable<Int> = backing.map { it.keys.size }

    override val current: Set<T> get() = backing.current.keys

    override val observers: List<Any> get() = backing.observers

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
     * Removes the specified item from the observable set.
     * If the item exists in the set, it will be removed, and observers of the set
     * will be notified of the change.
     *
     * @param item The item to be removed from the set.
     */
    fun remove(item: T) = backing.remove(item)

    /**
     * Clears the current set and inserts the given [items]. This is the equivalent
     * of calling [remove] for each `this - [items]` element, and [add] for each
     * `[items] - this` notifying every subscriber.
     *
     * > WARNING: calling so many times add and remove for basically every new element
     * added in this collection will trigger `|N ∪ M|` times the callbacks associated with
     * this set resulting in a non-negligible time spent updating observers. Please be
     * careful when using this method.
     *
     * @param items
     */
    fun clearAndAddAll(items: Set<T>) {
        val (toRemove, toAdd) = with(this.toSet()) { (this - items) to (items - this) }
        toRemove.forEach(::remove)
        toAdd.forEach(::add)
    }

    override fun onChange(registrant: Any, callback: (Set<T>) -> Unit) {
        backing.onChange(registrant) { callback(it.keys.toSet()) }
    }

    override fun stopWatching(registrant: Any) {
        backing.stopWatching(registrant)
    }

    override fun dispose() {
        backing.dispose()
        observableSize.dispose()
    }

    override fun observeMembership(item: T): Observable<Boolean> = backing[item].map { opt -> opt.getOrElse { false } }

    override fun toSet(): Set<T> = Collections.unmodifiableSet(backing.current.keys)

    override fun toList(): List<T> = backing.current.keys.toList()

    override fun copy(): ObservableMutableSet<T> = ObservableMutableSet<T>().apply {
        this@ObservableMutableSet.backing.asMap().keys.forEach(this::add)
    }

    override operator fun contains(item: T): Boolean = item in backing.current

    /**
     * Adds an item to the observable set.
     * If the item does not already exist in the set, it will be added, and observers
     * of the set will be notified of the change.
     *
     * @param item The item to be added to the set.
     */
    operator fun plus(item: T) = add(item)

    /**
     * Removes the specified item from the observable set.
     * This method serves as an operator overload for the `-` operator, allowing
     * items to be removed from the set using concise syntax. If the item exists
     * in the set, it will be removed and observers of the set will be notified.
     *
     * @param item The item to be removed from the set.
     */
    operator fun minus(item: T) = remove(item)

    /**
     * A companion object for the `ObservableMutableSet` class, providing handy factories.
     */
    companion object {

        /**
         * Converts the current list into an observable mutable set.
         * @see ObservableMutableSet
         *
         * @return An instance of `ObservableMutableSet` containing all unique elements from the original list.
         */
        fun <T> List<T>.toObservableSet(): ObservableMutableSet<T> = ObservableMutableSet<T>().also {
            this.forEach(it::add)
        }

        /**
         * Converts the current set into an observable mutable set.
         * @see ObservableMutableSet
         *
         * @return An instance of `ObservableMutableSet` containing all the elements from the original set.
         */
        fun <T> Set<T>.toObservableSet(): ObservableMutableSet<T> = ObservableMutableSet<T>().also {
            this.forEach(it::add)
        }

        /**
         * Creates a new [ObservableMutableSet] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableSet`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableMutableSet] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableMutableSet<T> = ObservableMutableSet<T>().apply {
            items.forEach(this::add)
        }
    }
}
