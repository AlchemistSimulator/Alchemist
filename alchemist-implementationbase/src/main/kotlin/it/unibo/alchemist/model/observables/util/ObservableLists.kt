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
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.util.Observables.combineLatestCollection
import it.unibo.alchemist.model.observables.util.Observables.flatMapCollection
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.observation.ObservableList

/**
 * Set of useful extensions on [it.unibo.alchemist.model.observation.ObservableList].
 */
object ObservableLists {

    /**
     * Combines the content of an `ObservableList` into a single observable by applying a mapping function
     * to each element and aggregating the results.
     *
     * @param map A function that maps each element of the `ObservableList` to an `Observable`.
     * @param aggregator A function that aggregates the mapped results into a single value.
     * @return An `Observable` emitting the aggregated result of the mapped content from the `ObservableList`.
     */
    fun <T, R, O> ObservableList<T>.combineLatest(
        map: (T) -> Observable<R>,
        aggregator: (List<R>) -> O,
    ): Observable<O> = combineLatestCollection(map, aggregator)

    /**
     * Transforms an [ObservableList] of type [T] into a single [Observable] of type
     * [arrow.core.Option]<[O]> by fusing the individual observables obtained from each item in the list.
     *
     * The emissions are merged, as in the `flatMap` operator of the
     * [ReactiveX convention](https://reactivex.io/documentation/operators/flatmap.html): the result emits the value of
     * whichever mapped observable emitted, and the current value of the observables of elements joining the list.
     * Observables of elements leaving the list are unsubscribed. Merging has no single current value:
     * [Observable.current] is the current value of the observable of the first element of the list.
     * To follow only the observable selected by the latest value, use [Observables.switchMap] instead.
     *
     * @param T The type of elements in the [ObservableList].
     * @param O The type of the resulting fused observable.
     * @param map A function that maps each element of the source [ObservableList] to an [Observable] of type [O].
     * @return An [Observable] of type [arrow.core.Option]<[O]> that is safe to use on empty lists.
     */
    fun <T, O> ObservableList<T>.flatMap(map: (T) -> Observable<O>): Observable<Option<O>> = flatMapCollection(map)

    /**
     * Converts this [ObservableList] of [observables][Observable] into a unique observable.
     *
     * @return a unique observable wrapping in one place all the notifications emitted by this [ObservableList]
     */
    @Suppress("UNCHECKED_CAST")
    fun ObservableList<out Observable<*>>.merge(): Observable<Option<Any?>> = flatMap { it as Observable<Any?> }

    /**
     * Converts the given [ObservableList] of [observables][Observable] into a unique observable.
     *
     * @return a unique observable wrapping in one place all the notifications emitted by this [ObservableList]
     */
    @JvmStatic
    @JvmName("mergeObservables")
    @Suppress("UNCHECKED_CAST")
    fun merge(observables: ObservableList<out Observable<*>>): Observable<Option<Any?>> =
        observables.flatMap { it as Observable<Any?> }

    /**
     * Creates a new [ObservableList] and populates it with the specified items.
     *
     * @param items The items to be added to the newly created `ObservableMutableList`.
     * The items are provided as a variable number of arguments.
     * @return A new [ObservableList] containing the specified items.
     */
    @JvmStatic
    fun <T> observableListOf(vararg items: T): ObservableList<T> = ObservableMutableList(*items)
}
