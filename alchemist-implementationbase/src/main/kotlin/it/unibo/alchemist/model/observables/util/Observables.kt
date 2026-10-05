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

    internal fun <T, C : Collection<T>, R, O> Observable<C>.combineLatestCollection(
        map: (T) -> Observable<R>,
        aggregator: (List<R>) -> O,
    ): Observable<O> = object : AbstractObservable<O>() {

        private val sources = mutableMapOf<T, Observable<R>>()

        override fun computeFresh(): O = aggregator(this@combineLatestCollection.current.map { map(it).current })

        override fun startMonitoring() = startMonitoring(false)

        @Suppress("UNCHECKED_CAST")
        override fun startMonitoring(lazy: Boolean) {
            val callback: (C) -> Unit = { current ->
                reconcile(
                    sources = sources,
                    current = current,
                    map = map,
                    doOnChange = { updateAndNotify(computeFresh()) },
                    postCleanup = { updateAndNotify(computeFresh()) },
                )
            }

            this@combineLatestCollection.onChange(this, !lazy, callback)
            if (lazy) {
                reconcile(
                    sources = sources,
                    current = ArrayList(this@combineLatestCollection.current) as C,
                    map = map,
                    doOnChange = { updateAndNotify(computeFresh()) },
                    invokeOnRegistration = cached.getOrNull() != null,
                )
            }
        }

        override fun stopMonitoring() {
            this@combineLatestCollection.stopWatching(this)
            sources.values.forEach { it.stopWatching(this@combineLatestCollection) }
            sources.clear()
        }
    }

    internal fun <T, C : Collection<T>, O> Observable<C>.flatMapCollection(
        map: (T) -> Observable<O>,
    ): Observable<Option<O>> = object : AbstractObservable<Option<O>>() {
        private val sources = mutableMapOf<T, Observable<O>>()

        override fun computeFresh(): Option<O> = this@flatMapCollection.current.firstOrNull()
            ?.let { key -> sources[key]?.current ?: map(key).current }
            ?.some()
            ?: none()

        override fun startMonitoring() = startMonitoring(false)

        @Suppress("UNCHECKED_CAST")
        override fun startMonitoring(lazy: Boolean) {
            val callback: (C) -> Unit = { current ->
                reconcile(
                    sources = sources,
                    current = current,
                    map = map,
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
                    sources = sources,
                    current = ArrayList(this@flatMapCollection.current) as C,
                    map = map,
                    doOnChange = { updateAndNotify(it.some()) },
                    invokeOnRegistration = cached.getOrNull() != null,
                )
            }
        }

        override fun stopMonitoring() {
            this@flatMapCollection.stopWatching(this)
            sources.values.forEach { it.stopWatching(this@flatMapCollection) }
            sources.clear()
        }
    }

    private fun <T, O> Observable<out Collection<T>>.reconcile(
        sources: MutableMap<T, Observable<O>>,
        current: Collection<T>,
        map: (T) -> Observable<O>,
        doOnChange: (O) -> Unit,
        postCleanup: () -> Unit = {},
        invokeOnRegistration: Boolean = true,
    ) {
        val currentSet = current.toSet()
        (sources.keys - currentSet).forEach { sources.remove(it)?.stopWatching(this) }
        (currentSet - sources.keys).forEach { key ->
            with(map(key)) {
                sources[key] = this
                onChange(this@reconcile, invokeOnRegistration, doOnChange)
            }
        }
        postCleanup()
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
     * Transforms the items emitted by this [Observable] into observables, then flatten the emissions from those
     * into a single observable mirroring the most recently emitted observable.
     *
     * @param transform a function that returns an [Observable] for reach item emitted by the source.
     * @return an [Observable] that emits the items emitted by the observable returned by [transform].
     */
    fun <T, R> Observable<T>.switchMap(transform: (T) -> Observable<R>): Observable<R> =
        object : AbstractObservable<R>() {
            private var innerSubscription: Disposable? = null

            override fun computeFresh(): R = transform(this@switchMap.current).current

            override fun startMonitoring() = startMonitoring(false)

            override fun startMonitoring(lazy: Boolean) {
                this@switchMap.onChange(this, !lazy, ::switchInner)
                if (lazy) {
                    // a manual switch to set up inner subscription without emitting is required if lazy
                    switchInner(this@switchMap.current, invokeOnRegistration = cached.getOrNull() != null)
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
}
