/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.AbstractObservable
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

            override fun startObservingSources() = Unit

            override fun stopObservingSources() = Unit
        }
        derived.onChange(true) { }
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

            override fun startObservingSources() = Unit

            override fun stopObservingSources() = Unit
        }
        derived.onChange(false) { }
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
        combined.onChange(false) { }
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
        combined.onChange(false) { }
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
        combined.onChange(false) { lastSeen = it }
        inner.current = 20
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
        mapped.onChange(true) { }
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
        mapped.onChange(false) { }
        assertEquals(1, transforms)
    }

    @Test
    fun `map receives updates if registered lazily`() {
        val source = observe(1)
        val mapped = source.map { it * 2 }
        var lastSeen: Int? = null
        mapped.onChange(false) { lastSeen = it }
        source.current = 2
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
        merged.onChange(false) { }
        assertEquals(1, merges)
    }

    @Test
    fun `mergeWith receives updates from both sources if registered lazily`() {
        val a = observe(1)
        val b = observe(2)
        val merged = a.mergeWith(b) { x, y -> x + y }
        var lastSeen: Int? = null
        merged.onChange(false) { lastSeen = it }
        a.current = 3
        assertEquals(5, lastSeen)
        b.current = 4
        assertEquals(7, lastSeen)
    }

    @Test
    fun `switchMap disposes the replaced and the active inner subscriptions`() {
        val source = observe(1)
        val first = observe("a")
        val second = observe("b")
        val switched = source.switchMap { if (it == 1) first else second }
        val subscription = switched.onChange(false) { }
        assertEquals(1, first.observerCount)
        source.current = 2
        assertEquals(0, first.observerCount)
        assertEquals(1, second.observerCount)
        subscription.dispose()
        assertEquals(0, second.observerCount)
    }

    @Test
    fun `switchMap follows the source after a lazy registration`() {
        val source = observe(1)
        val switched = source.switchMap { observe(it * 10) }
        var lastSeen: Int? = null
        switched.onChange(false) { lastSeen = it }
        source.current = 2
        assertEquals(20, lastSeen)
    }

    @Test
    fun `switchMap suppresses an equal first update after a lazy registration`() {
        val source = observe(10)
        val switched = source.switchMap { observe(it / 10) }
        var emissions = 0
        switched.onChange(false) { emissions++ }
        source.current = 11
        assertEquals(0, emissions)
        source.current = 20
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
        switched.onChange(false) { }
        assertEquals(1, transforms)
    }
}
