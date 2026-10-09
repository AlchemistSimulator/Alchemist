/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observables.util

import arrow.core.Option
import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.model.observables.util.Observables.combineLatestCollection
import it.unibo.alchemist.model.observables.util.Observables.flatMapCollection
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableSet

/**
 * Set of useful extensions on [it.unibo.alchemist.model.observation.ObservableSet].
 */
object ObservableSets {

    /**
     * Combines the content of an `ObservableSet` into a single observable by applying a mapping function
     * to each element and aggregating the results.
     * This function generates a single observable that emits every time the underlying set of contents is changed,
     * or some of the values (which are in turn observables) emit a changes, triggering the re-evaluation
     * of the [aggregation][aggregator] function.
     *
     * @param transformer A function that maps each element of the `ObservableSet` to an `Observable`.
     * @param aggregator A function that aggregates the mapped results into a single value.
     * @return An `Observable` emitting the aggregated result of the mapped content from the `ObservableSet`.
     */
    fun <T, R, O> ObservableSet<T>.combineLatest(
        transformer: (T) -> Observable<R>,
        aggregator: (Iterable<R>) -> O,
    ): Observable<O> = combineLatestCollection(transformer, aggregator)

    /**
     * Transforms an [ObservableSet] of type [T] into a single [Observable] of type
     * [arrow.core.Option]<[O]> by fusing the individual observables obtained from each item in the set.
     *
     * The emissions are merged, as in the `flatMap` operator of the
     * [ReactiveX convention](https://reactivex.io/documentation/operators/flatmap.html): the result emits the value of
     * whichever mapped observable emitted, and the current value of the observables of elements joining the set.
     * Observables of elements leaving the set are unsubscribed. Merging has no single current value:
     * [Observable.current] is the current value of the observable of the first element of the set.
     * To follow only the observable selected by the latest value, use [Observables.switchMap] instead.
     *
     * The resulting observable is **total**:
     * - If the set is empty, it emits (and its [Observable.current] is) [arrow.core.None]
     * - If the set is non-empty, it emits [arrow.core.Some] values coming from any mapped observable
     *
     * @param T The type of elements in the [ObservableSet].
     * @param O The type of the resulting fused observable.
     * @param transformer A function that maps each element of the source [ObservableSet] to an [Observable]
     * of type [O].
     * @return An [Observable] of type [arrow.core.Option]<[O]> that is safe to use on empty sets.
     */
    fun <T, O> ObservableSet<T>.flatMap(transformer: (T) -> Observable<O>): Observable<Option<O>> =
        flatMapCollection(transformer)

    /**
     * Converts this [ObservableSet] of [observables][Observable] into a unique observable that emits
     * when either this set would have changed (addition/removal of members) or one of its members
     * emits a value.
     *
     * The resulting observable is safe on empty sets: it emits [arrow.core.None] when the set is empty.
     *
     * @return a unique observable wrapping in one place all the notifications emitted by this [ObservableSet]
     */
    @Suppress("UNCHECKED_CAST")
    fun ObservableSet<out Observable<*>>.merge(): Observable<Option<Any?>> = flatMap { it as Observable<Any?> }

    /**
     * Converts the given [ObservableSet] of [observables][Observable] into a unique observable that emits
     * when either this set would have changed (addition/removal of members) or one of its members
     * emits a value.
     *
     * The resulting observable is safe on empty sets: it emits [arrow.core.None] when the set is empty.
     *
     * @return a unique observable wrapping in one place all the notifications emitted by this [ObservableSet]
     */
    @JvmStatic
    @JvmName("mergeObservables")
    @Suppress("UNCHECKED_CAST")
    fun merge(observables: ObservableSet<out Observable<*>>): Observable<Option<Any?>> =
        observables.flatMap { it as Observable<Any?> }

    /**
     * Returns a new [ObservableSet] applying the given [predicate] to each element.
     * The resulting collection is this collection with all the items that satisfy the given [predicate].
     * This function is backed by the standard `filter` of [sets][Set].
     *
     * @param predicate the predicate to apply for each element of this collection.
     * @return a new [ObservableSet] with the items that satisfy the input [predicate].
     */
    fun <T> ObservableSet<T>.filter(predicate: (T) -> Boolean): ObservableSet<T> =
        current.filter(predicate).toObservableSet()

    /**
     * Combines this observable set with another, producing a new observable set that represents
     * the union of both sets.
     *
     * @param other The observable set to be merged with the current one.
     * @return A new observable set that emits the union of the values.
     */
    infix fun <T> ObservableSet<T>.union(other: ObservableSet<T>): ObservableSet<T> = object : ObservableSet<T> {
        private val backing = this@union.mergeWith(other) { s1, s2 -> s1 + s2 }

        override val current: Set<T> get() = backing.current

        override var observers: List<Any> = emptyList()

        override val observingCallbacks: MutableMap<Any, List<(Set<T>) -> Unit>> = mutableMapOf()

        override val size: Observable<Int> = backing.map { it.size }

        override fun onChange(registrant: Any, invokeOnRegistration: Boolean, callback: (Set<T>) -> Unit) {
            observers += registrant
            observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
            backing.onChange(this to registrant, invokeOnRegistration, callback)
        }

        override fun stopWatching(registrant: Any) {
            observers -= registrant
            observingCallbacks.remove(registrant)
            backing.stopWatching(this to registrant)
        }

        override fun containsItem(item: T): Observable<Boolean> =
            this@union.containsItem(item).mergeWith(other.containsItem(item)) { a, b -> a || b }

        override fun toString(): String = "UnionObservableSet($current)[from: ${this@union}, other: $other]"
    }

    /**
     * Creates a new [ObservableSet] and populates it with the specified items.
     *
     * @param items The items to be added to the newly created `ObservableMutableSet`.
     * The items are provided as a variable number of arguments.
     * @return A new [ObservableSet] containing the specified items.
     */
    @JvmStatic
    fun <T> observableSetOf(vararg items: T): ObservableSet<T> = ObservableMutableSet(*items)
}
