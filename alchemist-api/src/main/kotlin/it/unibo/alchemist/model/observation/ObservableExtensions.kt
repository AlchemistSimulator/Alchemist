/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observation.ObservableMutableSet.Companion.toObservableSet
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
        ): Observable<O> = object : DerivedObservable<O>() {

            private val sources = mutableMapOf<T, Observable<R>>()

            override fun computeFresh(): O = aggregator(this@combineLatest.toSet().map { map(it).current })

            override fun startMonitoring() {
                val callback: (Set<T>) -> Unit = { current ->
                    reconcile(
                        sources = sources,
                        current = current,
                        map = map,
                        doOnChange = { updateAndNotify(computeFresh()) },
                        postCleanup = { updateAndNotify(computeFresh()) },
                    )
                }

                this@combineLatest.onChange(this, callback)
                callback(this@combineLatest.toSet())
            }

            override fun stopMonitoring() {
                this@combineLatest.stopWatching(this)
                sources.values.forEach { it.stopWatching(this@combineLatest) }
                sources.clear()
            }
        }

        /**
         * Transforms an [ObservableSet] of type [T] into a single [Observable] of type [Option]<[O]> by fusing the
         * individual observables obtained from each item in the set.
         *
         * The resulting observable is **total**:
         * - If the set is empty, it emits (and its [Observable.current] is) [arrow.core.None]
         * - If the set is non-empty, it emits [arrow.core.Some] values coming from any mapped observable
         *
         * @param T The type of elements in the [ObservableSet].
         * @param O The type of the resulting fused observable.
         * @param map A function that maps each element of the source [ObservableSet] to an [Observable] of type [O].
         * @return An [Observable] of type [Option]<[O]> that is safe to use on empty sets.
         */
        fun <T, O> ObservableSet<T>.flatMap(map: (T) -> Observable<O>): Observable<Option<O>> =
            object : DerivedObservable<Option<O>>() {
                private val sources = mutableMapOf<T, Observable<O>>()

                override fun computeFresh(): Option<O> = this@flatMap.toSet().firstOrNull()
                    ?.let { key -> sources[key]?.current ?: map(key).current }
                    ?.some()
                    ?: none()

                override fun startMonitoring() {
                    val callback: (Set<T>) -> Unit = { current ->
                        reconcile(
                            sources = sources,
                            current = current,
                            map = map,
                            doOnChange = { updateAndNotify(it.some()) },
                            postCleanup = {
                                if (this@flatMap.current.isEmpty()) {
                                    updateAndNotify(none())
                                }
                            },
                        )
                    }

                    this@flatMap.onChange(this, callback)
                    callback(this@flatMap.toSet())
                }

                override fun stopMonitoring() {
                    this@flatMap.stopWatching(this)
                    sources.values.forEach { it.stopWatching(this@flatMap) }
                    sources.clear()
                }
            }

        private fun <T, O> ObservableSet<T>.reconcile(
            sources: MutableMap<T, Observable<O>>,
            current: Set<T>,
            map: (T) -> Observable<O>,
            doOnChange: (O) -> Unit,
            postCleanup: () -> Unit,
        ) {
            (sources.keys - current).forEach { sources.remove(it)?.stopWatching(this) }
            (current - sources.keys).forEach { key ->
                with(map(key)) {
                    sources[key] = this
                    onChange(this@reconcile, doOnChange)
                }
            }
            postCleanup()
        }

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

            override var observers: List<Any> = emptyList()

            override val observingCallbacks: MutableMap<Any, List<(Set<T>) -> Unit>> = mutableMapOf()

            override val observableSize: Observable<Int> = backing.map { it.size }

            override fun onChange(registrant: Any, callback: (Set<T>) -> Unit) {
                observers += registrant
                observingCallbacks[registrant] = observingCallbacks[registrant].orEmpty() + callback
                backing.onChange(this to registrant, callback)
            }

            override fun stopWatching(registrant: Any) {
                observers -= registrant
                observingCallbacks.remove(registrant)
                backing.stopWatching(registrant)
            }

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
    fun <T, O> List<Observable<T>>.combineLatest(collector: (List<T>) -> O): Observable<Option<O>> =
        object : DerivedObservable<Option<O>>() {

            override fun computeFresh(): Option<O> = this@combineLatest.takeIf { it.isNotEmpty() }
                ?.let { _ -> collector(this@combineLatest.map { it.current }).some() }
                ?: arrow.core.none()

            override fun startMonitoring() {
                this@combineLatest.forEach { observable ->
                    observable.onChange(this) { updateAndNotify(computeFresh()) }
                }
            }

            override fun stopMonitoring() {
                this@combineLatest.forEach { it.stopWatching(this) }
            }
        }

    /**
     * Transforms the items emitted by this [Observable] into observables, then flatten the emissions from those
     * into a single observable mirroring the most recently emitted observable.
     *
     * @param transform a function that returns an [Observable] for reach item emitted by the source.
     * @return an [Observable] that emist the items emitted by the observable returned by [transform].
     */
    fun <T, R> Observable<T>.switchMap(transform: (T) -> Observable<R>): Observable<R> =
        object : DerivedObservable<R>() {
            private var innerSubscription: Observable<R>? = null

            override fun computeFresh(): R = transform(this@switchMap.current).current

            override fun startMonitoring() {
                this@switchMap.onChange(this, ::switchInner)
                switchInner(this@switchMap.current)
            }

            override fun stopMonitoring() {
                this@switchMap.stopWatching(this)
                innerSubscription?.stopWatching(this)
                innerSubscription = null
            }

            private fun switchInner(value: T) {
                innerSubscription?.stopWatching(this)
                with(transform(value)) {
                    innerSubscription = this
                    onChange(this@switchMap, ::updateAndNotify)
                    updateAndNotify(this.current)
                }
            }
        }
}
