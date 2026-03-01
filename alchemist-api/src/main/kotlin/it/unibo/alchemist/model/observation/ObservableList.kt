/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

/**
 * Represents a list that allows observation of its contents and provides notifications on changes.
 * This interface supports observing the entire list and various utility operations.
 */
interface ObservableList<T> : Observable<List<T>> {

    /**
     * An observable that emits the current size of the list.
     */
    val observableSize: Observable<Int>

    /**
     * Observes changes to the list and triggers the provided callback function whenever the list changes.
     * The callback will be invoked with the current list of items as a parameter.
     *
     * @param registrant The object registering for change notifications. Used to manage the lifecycle of the observer.
     * @param invokeOnRegistration whether the callback should be invoked on registration
     * @param callback The function to be invoked whenever the list changes. It receives the current list of items as
     *                 a parameter.
     */
    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (List<T>) -> Unit)

    /**
     * Returns the element at the specified index in the list.
     */
    operator fun get(index: Int): T

    /**
     * Returns an unmodifiable view of the current list of items in the observable list.
     *
     * @return A [List] containing all the items currently in the observable list.
     */
    fun toList(): List<T>

    /**
     * Creates and returns a copy of the current ObservableList.
     *
     * @return A new ObservableList containing the same elements as the current list.
     */
    fun copy(): ObservableList<T>

    /**
     * Checks if the specified item is present in the observable list.
     *
     * @param item The item to be checked for presence in the list.
     * @return `true` if the item exists in the list, `false` otherwise.
     */
    operator fun contains(item: T): Boolean

    /**
     * A companion object for the [ObservableList] class, providing a simple factory.
     */
    companion object {

        /**
         * Creates a new [ObservableList] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableList`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableList] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableList<T> = ObservableMutableList(*items)
    }
}

/**
 * A mutable observable list implementation, which allows for observing changes to the list and
 * provides notifications when its contents are modified. This class supports addition, removal,
 * updates, and observation of modifications to the list.
 *
 * @param T The type of elements maintained by this list.
 */
class ObservableMutableList<T> : ObservableList<T> {

    private var backing: PersistentList<T> = persistentListOf()
    private val sizeObservable = MutableObservable.observe(0)

    override val observableSize: Observable<Int> = sizeObservable

    override val current: List<T> get() = backing

    override val observers: List<Any> get() = observingCallbacks.keys.toList()

    override val observingCallbacks: MutableMap<Any, List<(List<T>) -> Unit>> = linkedMapOf()

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

    /**
     * Removes all of the elements from this list.
     */
    fun clear() {
        if (backing.isNotEmpty()) {
            backing = persistentListOf()
            notifyObservers()
        }
    }

    override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (List<T>) -> Unit) {
        observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
        if (invokeOnRegistration) callback(toList())
    }

    override fun stopWatching(registrant: Any) {
        observingCallbacks.remove(registrant)
    }

    override fun dispose() {
        observingCallbacks.clear()
        sizeObservable.dispose()
        backing = persistentListOf()
    }

    override operator fun get(index: Int): T = backing[index]

    override fun toList(): List<T> = backing

    override fun copy(): ObservableMutableList<T> = ObservableMutableList<T>().apply {
        backing = this@ObservableMutableList.backing
    }

    override operator fun contains(item: T): Boolean = backing.contains(item)

    /**
     * Adds an item to the observable list.
     * Observers of the list will be notified of the change.
     *
     * @param item The item to be added to the list.
     */
    operator fun plusAssign(item: T) {
        add(item)
    }

    /**
     * Removes the specified item from the observable list.
     *
     * @param item The item to be removed from the list.
     */
    operator fun minusAssign(item: T) {
        remove(item)
    }

    private fun notifyObservers() {
        sizeObservable.update { backing.size }
        val snapshot = backing
        observingCallbacks.values.forEach { callbacks ->
            callbacks.forEach { it(snapshot) }
        }
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
        fun <T> List<T>.toObservableList(): ObservableMutableList<T> = ObservableMutableList<T>().also {
            it.addAll(this)
        }

        /**
         * Creates a new [ObservableMutableList] and populates it with the specified items.
         *
         * @param items The items to be added to the newly created `ObservableMutableList`.
         * The items are provided as a variable number of arguments.
         * @return A new [ObservableMutableList] containing the specified items.
         */
        operator fun <T> invoke(vararg items: T): ObservableMutableList<T> = ObservableMutableList<T>().apply {
            backing = items.toList().toPersistentList()
        }
    }
}
