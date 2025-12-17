/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.observation

import it.unibo.alchemist.rx.model.observation.ObservableMutableSet.Companion.toObservableSet
import kotlin.collections.forEach

/**
 * Set of useful extensions on [Observable] and collections of observables.
 */
object ObservableExtensions {

    /**
     * Set of useful extensions on [ObservableSet].
     */
    object ObservableSetExtensions {

        /**
         * Combines the content of an `ObservableSet` into a single observable by applying a mapping function
         * to each element and aggregating the results.
         * This function generates a single observable that emits every time the underlying set of contents is changed,
         * or some of the values (which are in turn observables) emit a changes, triggering the re-evaluation
         * of the [aggregation][aggregator] function.
         *
         * @param map A function that maps each element of the `ObservableSet` to an `Observable`.
         * @param aggregator A function that aggregates the mapped results into a single value.
         * @return An `Observable` emitting the aggregated result of the mapped content from the `ObservableSet`.
         */
        fun <T, R, O> ObservableSet<T>.combineLatest(
            map: (T) -> Observable<R>,
            aggregator: (Iterable<R>) -> O,
        ): Observable<O> = object : Observable<O> {

            private val sources = mutableMapOf<T, Observable<R>>()

            override var current: O = aggregator(this@combineLatest.toSet().map { map(it).current })
                private set

            override var observers: List<Any> = emptyList()

            override fun onChange(registrant: Any, callback: (O) -> Unit) {
                observers += registrant
                this@combineLatest.onChange(this to registrant) {
                    sources.reconcile(it, map, this to registrant) { recomputeAndEmit(callback) }
                    recomputeAndEmit(callback)
                }
                callback(current)
            }

            private fun recomputeAndEmit(callback: (O) -> Unit) {
                val newValue = aggregator(sources.values.map { it.current })
                if (newValue != current) {
                    current = newValue
                    callback(newValue)
                }
            }

            override fun stopWatching(registrant: Any) {
                observers -= registrant
                this@combineLatest.stopWatching(this to registrant)
                sources.values.forEach { it.stopWatching(this to registrant) }
                sources.clear()
            }
        }

        /**
         * Transforms an [ObservableSet] of type [T] into a single [Observable] of type [O] by fusing the individual
         * observables obtained from each item in the set. The items in this collection will be mapped
         * through [map], which is a producer of observables. In this way, the resulting observer will emit when
         * 1. Members of this collection emit values
         * 2. The member set of this collection changes, emitting current values for newly added members only.
         *
         * @param T The type of elements in the [ObservableSet].
         * @param O The type of the resulting fused observable.
         * @param map A function that maps each element of the source [ObservableSet] to an [Observable] of type [O].
         * @return An [Observable] of type [O] that emits the combined or updated value whenever changes are emitted.
         */
        fun <T, O> ObservableSet<T>.flatMap(map: (T) -> Observable<O>): Observable<O> = object : Observable<O> {
            private val sources = mutableMapOf<T, Observable<O>>()

            override var current: O = map(toSet().first()).current // TODO: better way to handle initial vale
            override var observers: List<Any> = emptyList()

            override fun onChange(registrant: Any, callback: (O) -> Unit) {
                observers += registrant
                this@flatMap.onChange(this to registrant) {
                    sources.reconcile(it, map, this to registrant) { newValue ->
                        current = newValue
                        callback(newValue)
                    }
                }
            }

            override fun stopWatching(registrant: Any) {
                observers -= registrant
                this@flatMap.stopWatching(this to registrant)
                sources.values.forEach { it.stopWatching(this to registrant) }
                sources.clear()
            }
        }

        /**
         * Converts this [ObservableSet] of [observables][Observable] into a unique observable that emits
         * when either this set would have changed (addition/removal of members) or one of its members
         * emits a value of type [T].
         *
         * @return a unique observable wrapping in one place all the notifications emitted by this [ObservableSet]
         */
        fun <T> ObservableSet<Observable<T>>.merge(): Observable<T> = this.flatMap { it }

        /**
         * Returns a new [ObservableSet] applying the given [predicate] to each element.
         * The resulting collection is this collection with all the items that satisfies the given [predicate].
         * This function is backed by the standard `filter` of [sets][Set].
         *
         * @param predicate the predicate to apply for each element of this collection.
         * @return a new [ObservableSet] with the items that satisfies the input [predicate].
         */
        fun <T> ObservableSet<T>.filter(predicate: (T) -> Boolean): ObservableSet<T> =
            this.toSet().filter(predicate).toObservableSet()

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

            override val observers: List<Any> = backing.observers

            override val observableSize: Observable<Int> = backing.map { it.size }

            override fun onChange(registrant: Any, callback: (Set<T>) -> Unit) = backing.onChange(registrant, callback)

            override fun stopWatching(registrant: Any) = backing.stopWatching(registrant)

            override fun observeMembership(item: T): Observable<Boolean> =
                this@union.observeMembership(item).mergeWith(other.observeMembership(item)) { a, b -> a || b }

            override fun toSet(): Set<T> = current

            override fun toList(): List<T> = current.toList()

            override fun copy(): ObservableSet<T> = ObservableMutableSet<T>().apply {
                current.forEach(this::add)
            }

            override fun contains(item: T): Boolean = item in current

            override fun toString(): String = "UnionObservableSet($current)[from: ${this@union}, other: $other]"
        }
    }

    /**
     * Combines a collection of [Observable] instances into a single observable instance that emits updates
     * based on changes coming from all observables. Every time an observer of this collection emits,
     * the resulting observable emits.
     *
     * @return A new [Observable] that aggregates updates from the collection of source [Observable] instances.
     */
    fun <T> Iterable<Observable<T>>.merge(): Observable<T> = object : Observable<T> {

        override var current: T = this@merge.first().current // bho

        override var observers: List<Any> = emptyList()

        override fun onChange(registrant: Any, callback: (T) -> Unit) {
            observers += registrant
            this@merge.forEach { observable ->
                observable.onChange(this to registrant) {
                    if (it != current) { // do we really need this
                        current = it
                        callback(it)
                    }
                }
            }
            callback(current)
        }

        override fun stopWatching(registrant: Any) {
            observers -= registrant
            this@merge.forEach { observable ->
                observable.stopWatching(this to registrant)
            }
        }
    }

    /**
     * Collects the latest values emitted by a list of [Observable]s and transforms them
     * into a new [Observable] using the provided collector function.
     *
     * @param T the type of the values emitted by the source [Observable]s.
     * @param O the type of the combined and transformed result emitted by the resulting [Observable].
     * @param collector a function that takes a list of the latest values from the source [Observable]s
     *                  and transforms them into a result of type [O], which will be emitted by the resulting
     *                  [Observable].
     * @return a [Observable] of type [O], which emits the transformed result whenever the
     *         state of the source [Observable]s changes.
     */
    fun <T, O> List<Observable<T>>.combineLatest(collector: (List<T>) -> O): Observable<O> = object : Observable<O> {
        val sources = this@combineLatest

        override var current = collector(sources.map { it.current })
        override var observers: List<Any> = listOf()

        override fun onChange(registrant: Any, callback: (O) -> Unit) {
            observers += registrant
            sources.forEach { observable ->
                observable.onChange(this to registrant) { _ ->
                    val newValue = collector(sources.map { it.current })
                    if (newValue != current) {
                        current = newValue
                        callback(current)
                    }
                }
            }
            callback(current)
        }

        override fun stopWatching(registrant: Any) {
            observers -= registrant
            sources.forEach { it.stopWatching(this to registrant) }
        }
    }

    /**
     * Transforms the items emitted by this [Observable] into observables, then flatten the emissions from those
     * into a single observable mirroring the most recently emitted observable.
     *
     * @param transform a function that returns an [Observable] for reach item emitted by the source.
     * @return an [Observable] that emist the items emitted by the observable returned by [transform].
     */
    fun <T, R> Observable<T>.switchMap(transform: (T) -> Observable<R>): Observable<R> = object : Observable<R> {

        private var innerSubscription: Observable<R>? = null
        override var current: R = transform(this@switchMap.current).also { innerSubscription = it }.current
        override var observers: List<Any> = emptyList()

        override fun onChange(registrant: Any, callback: (R) -> Unit) {
            observers += registrant
            this@switchMap.onChange(this to registrant) { outerValue ->
                innerSubscription?.stopWatching(this to registrant)
                val newInner = transform(outerValue)
                innerSubscription = newInner
                newInner.onChange(this to registrant) { innerValue ->
                    if (innerValue != current) {
                        current = innerValue
                        callback(innerValue)
                    }
                }
                if (newInner.current != current) {
                    current = newInner.current
                    callback(current)
                }
            }
            innerSubscription?.onChange(this to registrant) {
                if (it != current) {
                    current = it
                    callback(it)
                }
            }
        }

        override fun stopWatching(registrant: Any) {
            observers -= registrant
            this@switchMap.stopWatching(this to registrant)
            innerSubscription?.stopWatching(this to registrant)
        }
    }

    private fun <T, R> MutableMap<T, Observable<R>>.reconcile(
        newKeys: Set<T>,
        map: (T) -> Observable<R>,
        subscriptionKey: Any,
        onValueChange: (R) -> Unit,
    ) {
        val keysToRemove = keys - newKeys
        val keysToAdd = newKeys - keys
        keysToRemove.forEach { remove(it)?.stopWatching(subscriptionKey) }
        keysToAdd.forEach { key ->
            val observable = map(key)
            put(key, observable)
            observable.onChange(subscriptionKey, onValueChange)
        }
    }
}
