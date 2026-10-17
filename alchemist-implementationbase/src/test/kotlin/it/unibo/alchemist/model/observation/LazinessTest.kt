/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import kotlin.test.Test
import kotlin.test.assertEquals

class LazinessTest {
    @Test
    fun `an observable computes its value if registered eagerly`() {
        var computations = 0
        val derived = object : AbstractObservable<Int>() {
            override fun computeFresh(): Int {
                computations++
                return 26
            }

            override fun startMonitoring() = Unit

            override fun stopMonitoring() = Unit
        }
        derived.onChange(this, true) { }
        assertEquals(1, computations)
    }

    @Test
    fun `an observable without distinct emissions computes nothing if registered lazily`() {
        var computations = 0
        val derived = object : AbstractObservable<Int>(emitOnDistinct = false) {
            override fun computeFresh(): Int {
                computations++
                return 26
            }

            override fun startMonitoring() = Unit

            override fun stopMonitoring() = Unit
        }
        derived.onChange(this, false) { }
        assertEquals(0, computations)
    }

    @Test
    fun `combineLatest on a list collects once if registered lazily`() {
        val list = listOf(observe(1), observe(2))
        var collections = 0
        val combined = list.combineLatest {
            collections++
            it.sum()
        }
        combined.onChange(this, false) { }
        assertEquals(1, collections)
    }

    @Test
    fun `combineLatest on a set aggregates once if registered lazily`() {
        val set = ObservableMutableSet(1, 2)
        var aggregations = 0
        val combined = set.combineLatest(
            transformer = { observe(it) },
            aggregator = {
                aggregations++
                it.sum()
            },
        )
        combined.onChange(this, false) { }
        assertEquals(1, aggregations)
    }

    @Test
    fun `combineLatest on a set receives updates if registered lazily`() {
        val set = ObservableMutableSet("a")
        val inner = observe(10)
        val combined = set.combineLatest(
            transformer = { inner },
            aggregator = { it.sum() },
        )
        var lastSeen: Int? = null
        combined.onChange(this, false) { lastSeen = it }
        inner.update { 20 }
        assertEquals(20, lastSeen)
        set.add("b")
        assertEquals(40, lastSeen) // Both elements map to the same inner observable.
    }

    @Test
    fun `map applies the transform if registered eagerly`() {
        val source = observe(1)
        var transforms = 0
        val mapped = source.map {
            transforms++
            it * 2
        }
        mapped.onChange(this, true) { }
        assertEquals(1, transforms)
    }

    @Test
    fun `map applies the transform once if registered lazily, to compare the first update against it`() {
        val source = observe(1)
        var transforms = 0
        val mapped = source.map {
            transforms++
            it * 2
        }
        mapped.onChange(this, false) { }
        assertEquals(1, transforms)
    }

    @Test
    fun `map receives updates if registered lazily`() {
        val source = observe(1)
        val mapped = source.map { it * 2 }
        var lastSeen: Int? = null
        mapped.onChange(this, false) { lastSeen = it }
        source.update { 2 }
        assertEquals(4, lastSeen)
    }

    @Test
    fun `mergeWith merges once if registered lazily, to compare the first update against it`() {
        val a = observe(1)
        val b = observe(2)
        var merges = 0
        val merged = a.mergeWith(b) { x, y ->
            merges++
            x + y
        }
        merged.onChange(this, false) { }
        assertEquals(1, merges)
    }

    @Test
    fun `mergeWith receives updates from both sources if registered lazily`() {
        val a = observe(1)
        val b = observe(2)
        val merged = a.mergeWith(b) { x, y -> x + y }
        var lastSeen: Int? = null
        merged.onChange(this, false) { lastSeen = it }
        a.update { 3 }
        assertEquals(5, lastSeen)
        b.update { 4 }
        assertEquals(7, lastSeen)
    }

    @Test
    fun `switchMap disposes the replaced and the active inner subscriptions`() {
        val source = observe(1)
        val first = observe("a")
        val second = observe("b")
        val switched = source.switchMap { if (it == 1) first else second }
        switched.onChange(this, false) { }
        assertEquals(1, first.observers.size)
        source.update { 2 }
        assertEquals(0, first.observers.size)
        assertEquals(1, second.observers.size)
        switched.stopWatching(this)
        assertEquals(0, second.observers.size)
    }

    @Test
    fun `switchMap follows the source after a lazy registration`() {
        val source = observe(1)
        val switched = source.switchMap { observe(it * 10) }
        var lastSeen: Int? = null
        switched.onChange(this, false) { lastSeen = it }
        source.update { 2 }
        assertEquals(20, lastSeen)
    }

    @Test
    fun `switchMap suppresses an equal first update after a lazy registration`() {
        val source = observe(10)
        val switched = source.switchMap { observe(it / 10) }
        var emissions = 0
        switched.onChange(this, false) { emissions++ }
        source.update { 11 }
        assertEquals(0, emissions)
        source.update { 20 }
        assertEquals(1, emissions)
    }

    @Test
    fun `switchMap transforms once if registered lazily`() {
        val source = observe(1)
        var transforms = 0
        val switched = source.switchMap {
            transforms++
            observe(it)
        }
        switched.onChange(this, false) { }
        assertEquals(1, transforms)
    }
}
