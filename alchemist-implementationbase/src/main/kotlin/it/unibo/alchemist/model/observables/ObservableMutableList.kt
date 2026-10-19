/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables

import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * A mutable observable list implementation, which allows for observing changes to the list and
 * provides notifications when its contents are modified. This class supports addition, removal,
 * updates, and observation of modifications to the list.
 *
 * @param T The type of elements maintained by this list.
 * @param initial The elements this list initially contains.
 */
class ObservableMutableList<T> @JvmOverloads constructor(initial: Iterable<T> = emptyList()) :
    AbstractObservable<List<T>>(emitOnDistinct = false),
    ObservableList<T> {

    // Out of order: sizeObservable is initialized from it.
    private var backing: PersistentList<T> = initial.toPersistentList()

    override val current: List<T> get() = backing

    override val size: Observable<Int>
        field = observe(backing.size)

    /**
     * Adds an item to the observable list.
     * Observers of the list will be notified of the change.
     *
     * @param item The item to be added to the list.
     * @return `true` (as specified by [MutableList.add])
     */
    fun add(item: T): Boolean {
        backing = backing.add(item)
        notifyObservers()
        return true
    }

    /**
     * Adds an item at the specified index.
     * Observers of the list will be notified of the change.
     *
     * @param index The index at which the specified element is to be inserted
     * @param item The item to be added to the list.
     */
    fun add(index: Int, item: T) {
        backing = backing.add(index, item)
        notifyObservers()
    }

    /**
     * Appends all of the elements in the specified collection to the end of this list.
     *
     * @param items collection containing elements to be added to this list.
     * @return `true` if this list changed as a result of the call
     */
    fun addAll(items: Collection<T>): Boolean = if (items.isNotEmpty()) {
        backing = backing.addAll(items)
        notifyObservers()
        true
    } else {
        false
    }

    /**
     * Removes all of the elements from this list.
     */
    fun clear() {
        if (backing.isNotEmpty()) {
            backing = persistentListOf()
            notifyObservers()
        }
    }

    override fun computeFresh(): List<T> = backing

    override fun dispose() {
        super.dispose()
        size.dispose()
        backing = persistentListOf()
    }

    /**
     * Removes the first occurrence of the specified item from the observable list, if it is present.
     * If the list changes, observers will be notified.
     *
     * @param item The item to be removed from the list.
     * @return `true` if the list contained the specified element
     */
    fun remove(item: T): Boolean {
        val newBacking = backing.remove(item)
        val result = newBacking !== backing
        if (result) {
            backing = newBacking
            notifyObservers()
        }
        return result
    }

    /**
     * Removes all the elements satisfying [predicate] from the observable list.
     * If the list changes, observers will be notified once.
     *
     * @param predicate the condition selecting the elements to remove
     * @return `true` if any element was removed
     */
    fun removeAll(predicate: (T) -> Boolean): Boolean {
        val newBacking = backing.removeAll(predicate)
        val result = newBacking !== backing
        if (result) {
            backing = newBacking
            notifyObservers()
        }
        return result
    }

    /**
     * Removes the element at the specified position in this list.
     *
     * @param index the index of the element to be removed
     * @return the element that was removed from the list
     */
    fun removeAt(index: Int): T {
        val old = backing[index]
        backing = backing.removeAt(index)
        notifyObservers()
        return old
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     *
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     */
    operator fun set(index: Int, element: T): T {
        val old = backing[index]
        if (old != element) {
            backing = backing.set(index, element)
            notifyObservers()
        }
        return old
    }

    private fun notifyObservers() {
        size.update { backing.size }
        updateAndNotify(backing)
    }

    /**
     * A companion object for the `ObservableMutableList` class, providing handy factories.
     */
    companion object {

        /**
         * Converts the current list into an observable mutable list.
         * @see ObservableMutableList
         *
         * @return An instance of `ObservableMutableList` containing all elements from the original list.
         */
        fun <T> List<T>.toObservableList(): ObservableMutableList<T> = ObservableMutableList(this)

        /**
         * Creates a new [ObservableMutableList] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableList`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableMutableList] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableMutableList<T> = ObservableMutableList(items.toList())
    }
}
