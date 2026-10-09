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
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observation.AbstractObservable
import it.unibo.alchemist.model.observation.Disposable
import it.unibo.alchemist.model.observation.Observable

/**
 * Set of useful extensions on [Observable] and collections of observables.
 */
object Observables {

    /**
     * Collects the latest values emitted by a list of [Observable]s and transforms them
     * into a new [Observable] using the provided collector function.
     *
     * This is the `combineLatest` operator of the
     * [ReactiveX convention](https://reactivex.io/documentation/operators/combinelatest.html), also known as
     * [combine](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html)
     * in Kotlin flows. Since observables always have a current value, the result does not wait for every source to
     * emit before producing a value; an empty list of sources yields [arrow.core.None].
     *
     * @param T the type of the values emitted by the source [Observable]s.
     * @param O the type of the combined and transformed result emitted by the resulting [Observable].
     * @param collector a function that takes a list of the latest values from the source [Observable]s
     * and transforms them into a result of type [O], which will be emitted by the resulting
     * [Observable].
     * @return a [Observable] of type [O], which emits the transformed result whenever the
     * state of the source [Observable]s changes.
     */
    @JvmStatic
    fun <T, O> List<Observable<T>>.combineLatest(collector: (List<T>) -> O): Observable<Option<O>> =
        object : AbstractObservable<Option<O>>() {

            override fun computeFresh(): Option<O> = this@combineLatest.takeIf { it.isNotEmpty() }
                ?.let { _ -> collector(this@combineLatest.map { it.current }).some() }
                ?: none<O>()

            override fun startMonitoring() = startMonitoring(false)

            override fun startMonitoring(lazy: Boolean) {
                this@combineLatest.forEach { observable ->
                    observable.onChange(this, !lazy) { updateAndNotify(computeFresh()) }
                }
            }

            override fun stopMonitoring() {
                this@combineLatest.forEach { it.stopWatching(this) }
            }
        }

    /**
     * Maps the current value of this [Observable] to an inner observable through [transform], and mirrors the inner
     * observable of the most recent value only: whenever this observable emits, the previous inner observable is
     * unsubscribed and the new one is followed.
     *
     * This is the `switchMap` operator of the
     * [ReactiveX convention](https://reactivex.io/documentation/operators/flatmap.html), also known as
     * [flatMapLatest](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flat-map-latest.html)
     * in Kotlin flows. Unlike `flatMap` in the same convention, which merges the emissions of every inner observable
     * created so far, inner observables that are no longer selected stop affecting the result.
     *
     * @param transform a function that returns an [Observable] for each item emitted by the source.
     * @return an [Observable] whose value is the current value of the observable returned by [transform] for the
     * current value of this observable.
     */
    @JvmStatic
    fun <T, R> Observable<T>.switchMap(transform: (T) -> Observable<R>): Observable<R> =
        object : AbstractObservable<R>() {
            private var innerSubscription: Disposable? = null

            override fun computeFresh(): R = transform(this@switchMap.current).current

            override fun startMonitoring() = startMonitoring(false)

            override fun startMonitoring(lazy: Boolean) {
                this@switchMap.onChange(this, !lazy, ::switchInner)
                if (lazy) {
                    // a manual switch to set up inner subscription without emitting is required if lazy
                    switchInner(this@switchMap.current, invokeOnRegistration = cached.isSome())
                }
            }

            override fun stopMonitoring() {
                this@switchMap.stopWatching(this)
                innerSubscription?.dispose()
                innerSubscription = null
            }

            private fun switchInner(value: T) = switchInner(value, true)

            private fun switchInner(value: T, invokeOnRegistration: Boolean) {
                innerSubscription?.dispose()
                with(transform(value)) {
                    innerSubscription =
                        subscribe(invokeOnSubscription = invokeOnRegistration, callback = ::updateAndNotify)
                    if (invokeOnRegistration) {
                        updateAndNotify(this.current)
                    } else {
                        cached = this.current.some()
                    }
                }
            }
        }

    /**
     * Maps every element of the current collection to an observable through `transformer`, and aggregates their current
     * values through [aggregator]. The result is recomputed whenever the collection changes or any mapped
     * observable emits; observables of elements leaving the collection are unsubscribed.
     */
    @JvmStatic
    internal fun <T, C : Collection<T>, R, O> Observable<C>.combineLatestCollection(
        transformer: (T) -> Observable<R>,
        aggregator: (List<R>) -> O,
    ): Observable<O> = object : AbstractObservable<O>() {

        private val sources = mutableMapOf<T, Observable<R>>()

        override fun computeFresh(): O =
            aggregator(this@combineLatestCollection.current.map { key -> (sources[key] ?: transformer(key)).current })

        override fun startMonitoring() = startMonitoring(false)

        @Suppress("UNCHECKED_CAST")
        override fun startMonitoring(lazy: Boolean) {
            val callback: (C) -> Unit = { current ->
                reconcile(
                    owner = this,
                    sources = sources,
                    current = current,
                    transformer = transformer,
                    doOnChange = { updateAndNotify(computeFresh()) },
                    postCleanup = { updateAndNotify(computeFresh()) },
                )
            }

            this@combineLatestCollection.onChange(this, !lazy, callback)
            if (lazy) {
                reconcile(
                    owner = this,
                    sources = sources,
                    current = ArrayList(this@combineLatestCollection.current) as C,
                    transformer = transformer,
                    doOnChange = { updateAndNotify(computeFresh()) },
                    invokeOnRegistration = cached.isSome(),
                )
            }
        }

        override fun stopMonitoring() {
            this@combineLatestCollection.stopWatching(this)
            sources.forEach { (key, source) -> source.stopWatching(this to key) }
            sources.clear()
        }
    }

    /**
     * Maps every element of the current collection to an observable through `transformer`, and merges their
     * emissions, in the sense of the `flatMap` operator of the
     * [ReactiveX convention](https://reactivex.io/documentation/operators/flatmap.html): the result emits the value
     * of whichever mapped observable emitted, as well as the current value of observables joining the collection.
     * Observables of elements leaving the collection are unsubscribed, and an empty collection emits
     * [arrow.core.None]. Merging has no single current value: [Observable.current] is the current value of the
     * observable of the first element of the collection.
     */
    @JvmStatic
    internal fun <T, C : Collection<T>, O> Observable<C>.flatMapCollection(
        transformer: (T) -> Observable<O>,
    ): Observable<Option<O>> = object : AbstractObservable<Option<O>>() {
        private val sources = mutableMapOf<T, Observable<O>>()

        override fun computeFresh(): Option<O> = this@flatMapCollection.current.firstOrNull()
            ?.let { key -> sources[key]?.current ?: transformer(key).current }
            ?.some()
            ?: none()

        override fun startMonitoring() = startMonitoring(false)

        @Suppress("UNCHECKED_CAST")
        override fun startMonitoring(lazy: Boolean) {
            val callback: (C) -> Unit = { current ->
                reconcile(
                    owner = this,
                    sources = sources,
                    current = current,
                    transformer = transformer,
                    doOnChange = { updateAndNotify(it.some()) },
                    postCleanup = {
                        if (this@flatMapCollection.current.isEmpty()) {
                            updateAndNotify(none())
                        }
                    },
                )
            }

            this@flatMapCollection.onChange(this, !lazy, callback)
            if (lazy) {
                reconcile(
                    owner = this,
                    sources = sources,
                    current = ArrayList(this@flatMapCollection.current) as C,
                    transformer = transformer,
                    doOnChange = { updateAndNotify(it.some()) },
                    invokeOnRegistration = cached.isSome(),
                )
            }
        }

        override fun stopMonitoring() {
            this@flatMapCollection.stopWatching(this)
            sources.forEach { (key, source) -> source.stopWatching(this to key) }
            sources.clear()
        }
    }

    /*
     * Each element subscribes to its observable under the pair (owner, element), so that neither other observables
     * derived from the same collection nor other elements mapped to the same observable share the registration.
     */
    @JvmStatic
    private fun <T, O> reconcile(
        owner: Observable<*>,
        sources: MutableMap<T, Observable<O>>,
        current: Collection<T>,
        transformer: (T) -> Observable<O>,
        doOnChange: (O) -> Unit,
        postCleanup: () -> Unit = {},
        invokeOnRegistration: Boolean = true,
    ) {
        val currentSet = current.toSet()
        (sources.keys - currentSet).forEach { key -> sources.remove(key)?.stopWatching(owner to key) }
        (currentSet - sources.keys).forEach { key ->
            with(transformer(key)) {
                sources[key] = this
                onChange(owner to key, invokeOnRegistration, doOnChange)
            }
        }
        postCleanup()
    }
}
