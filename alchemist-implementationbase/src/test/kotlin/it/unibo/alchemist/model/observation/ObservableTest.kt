/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observables.AbstractObservable
import it.unibo.alchemist.model.observables.EventObservable
import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.MutableObservables.updateValue
import it.unibo.alchemist.model.observables.util.ObservableSets.combineLatest
import it.unibo.alchemist.model.observables.util.ObservableSets.flatMap
import it.unibo.alchemist.model.observables.util.ObservableSets.flattenMerge
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.currentOrNull
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObservableTest {
    @Test
    fun `a combined observable released while registering with a joining element keeps no registration`() {
        var elementObservers = 0
        // An element publishing a newer value while it starts observing its sources, as network models do.
        val element = object : AbstractObservable<Int>() {
            var value = 1

            override fun computeFresh(): Int = value

            override fun startObservingSources() {
                elementObservers++
                value = 2
                updateAndNotify(value)
            }

            override fun stopObservingSources() {
                elementObservers--
            }
        }
        val set = ObservableMutableSet<String>()
        val combined = set.combineLatest(transformer = { element }, aggregator = { values -> values.sum() })
        lateinit var subscription: Disposable
        subscription = combined.onChange(notifyOnRegistration = false) { subscription.dispose() }
        // The element publishes while the combination registers with it, releasing the only observer.
        set.add("a")
        assertFalse(combined.isObserved)
        assertEquals(0, elementObservers)
    }

    @Test
    fun `a disposed EventObservable has no observers and notifies nobody`() {
        val event = EventObservable()
        var calls = -2
        event.onChange { calls++ }
        event.onChange { calls++ }
        event.emit()
        event.dispose()
        event.emit()
        assertEquals(2, calls)
        assertFalse(event.isObserved)
    }

    @Test
    fun `a disposed observable does not notify its observers`() {
        val observable = observe(10)
        var changesCount = -1
        observable.onChange { changesCount++ }
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        observable.dispose()
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        assertEquals(10, changesCount)
    }

    @Test
    fun `a mapped observable emits only when the mapped value changes`() {
        val observable = observe(0)
        val parityObservable = observable.map { it % 2 == 0 }
        var emissions = -1
        parityObservable.onChange { emissions++ }
        // Sequence that keeps parity constant: 0 -> 2 -> 4 -> 6
        repeat(3) {
            observable.update { curr -> curr + 2 }
        }
        assertEquals(0, emissions)
    }

    @Test
    fun `a mapped observable registered without notifyOnRegistration emits only when the mapped value changes`() {
        val counter = observe(0)
        val parities = mutableListOf<Int>()
        counter.map { it % 2 }.onChange(false) { parities += it }
        counter.update { 2 }
        assertEquals(emptyList(), parities)
        counter.update { 3 }
        assertEquals(listOf(1), parities)
    }

    @Test
    fun `a merged observable emits only when the merged value changes`() {
        val a = observe(0)
        val b = observe(0)
        val c = a.mergeWith(b) { first, second -> (first + second) % 2 }
        var emissions = -1
        c.onChange { emissions++ }
        repeat(5) { _ ->
            a.update { it + 2 }
            b.update { it + 2 }
        }
        assertEquals(0, emissions)
    }

    @Test
    fun `a merged observable set emits on membership and member emissions`() {
        val obsA = observe(10)
        val obsB = observe(-10)
        val elements = ObservableMutableSet(obsA, obsB)
        val merged = elements.flattenMerge()
        var counter = 0
        merged.onChange(notifyOnRegistration = false) { counter++ }
        obsA.update { it + 11 }
        elements.add(observe(29))
        assertEquals(2, counter)
    }

    @Test
    fun `an EventObservable notifies all observers on every emission`() {
        val event = EventObservable()
        var calls = -1
        event.onChange { calls++ }
        repeat(5) { event.emit() }
        assertEquals(5, calls)
    }

    @Test
    fun `an EventObservable stops notifying a released observer`() {
        val event = EventObservable()
        var calls = -1
        val subscription = event.onChange { calls++ }
        event.emit()
        subscription.dispose()
        event.emit()
        assertEquals(1, calls)
        assertFalse(event.isObserved)
    }

    @Test
    fun `an EventObservable supports multiple observers`() {
        val event = EventObservable()
        var aCalls = -1
        var bCalls = -1
        event.onChange { aCalls++ }
        event.onChange { bCalls++ }
        repeat(3) { event.emit() }
        assertEquals(3, aCalls)
        assertEquals(3, bCalls)
        assertEquals(2, event.observerCount)
    }

    @Test
    fun `an observable can be mapped into another`() {
        val observable = observe(10)
        val strObservable = observable.map { it.toString() }
        var changesCounts = -1
        strObservable.onChange { changesCounts++ }
        repeat(10) { observable.update { curr -> curr + 10 } }
        assertEquals(10, changesCounts)
        assertEquals("${10 * 11}", strObservable.current)
    }

    @Test
    fun `an observable can be merged with another`() {
        val a = observe(0)
        val b = observe(0)
        val c = a.mergeWith(b) { first, second -> first + second }
        var current = 0
        c.onChange { current = it }
        a.update { it + 10 }
        assertEquals(10, current)
        b.update { it + 10 }
        assertEquals(20, current)
    }

    @Test
    fun `an observable may emit an event when assigned an equal value`() {
        val observable = observe(initial = 0, emitOnDistinct = false)
        var emissions = 0
        observable.onChange(notifyOnRegistration = false) { emissions++ }
        observable.current = 0
        assertEquals(1, emissions)
    }

    @Test
    fun `an observable publishing while observing its sources delivers only the newer value to its first observer`() {
        val observable = object : AbstractObservable<Int>() {
            var value = 1

            override fun computeFresh(): Int = value

            override fun startObservingSources() {
                value = 2
                updateAndNotify(value)
            }

            override fun stopObservingSources() = Unit
        }
        val seen = mutableListOf<Int>()
        observable.onChange { seen += it }
        // An observable publishing 2 while starting to observe its sources delivers only [2] to its first observer.
        // (it used to deliver 2, then 1)
        assertEquals(listOf(2), seen)
    }

    @Test
    fun `an observer can be released`() {
        val observable = observe(10)
        var changesCount = -1
        val subscription = observable.onChange { changesCount++ }
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        subscription.dispose()
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        assertEquals(10, changesCount)
    }

    @Test
    fun `changes of a value can be observed`() {
        val observable = observe(10)
        var changesCount = -1 // The first subscription always calls the observing callbacks.
        observable.onChange { changesCount++ }
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        assertEquals(10, changesCount)
    }

    @Test
    fun `combineLatest emits only when the combined value changes`() {
        val a = observe(0)
        val b = observe(0)
        val collected: Observable<Option<Int>> =
            listOf<Observable<Int>>(a, b).combineLatest { values ->
                (values[0] + values[1]) % 2
            }
        var emissions = -1
        collected.onChange { emissions++ }
        repeat(5) { _ ->
            a.update { it + 2 }
            b.update { it + 4 }
        }
        assertEquals(0, emissions)
    }

    @Test
    fun `combineLatest emits the combined value when any source changes`() {
        val a = observe(1)
        val b = observe(10)
        val collected: Observable<Option<String>> =
            listOf<Observable<Int>>(a, b).combineLatest { values ->
                "${values[0]}-${values[1]}"
            }
        val emissions = mutableListOf<String>()
        collected.onChange { emissions.add(it.getOrElse { "" }) }
        a.update { it + 1 }
        b.update { it + 5 }
        assertEquals("2-10", emissions[1])
        assertEquals("2-15", emissions[2])
    }

    @Test
    fun `combineLatest on a set aggregates the inner values and reacts to set and inner changes`() {
        val set = ObservableMutableSet<String>()
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        val combined = set.combineLatest(transformer = ::observableFor, aggregator = { values -> values.sum() })
        val seen = mutableListOf<Int>()
        combined.onChange { seen.add(it) }
        assertEquals(0, seen[0])
        set.add("a")
        set.add("b")
        assertEquals(0, seen.last())
        inner.getValue("a").update { 10 }
        inner.getValue("b").update { 5 }
        assertEquals(listOf(0, 10, 15), seen)
        set.remove("a")
        assertEquals(5, seen.last())
    }

    @Test
    fun `combineLatest on a set unsubscribes from the inner observables when released`() {
        val set = ObservableMutableSet<String>()
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        set.add("x")
        set.add("y")
        val combined = set.combineLatest(transformer = ::observableFor, aggregator = { values -> values.sum() })
        val subscription = combined.onChange { }
        assertEquals(1, inner.getValue("x").observerCount)
        assertEquals(1, inner.getValue("y").observerCount)
        subscription.dispose()
        assertEquals(0, inner.getValue("x").observerCount)
        assertEquals(0, inner.getValue("y").observerCount)
    }

    @Test
    fun `combineLatest unsubscribes from its sources when the last observer is released`() {
        val a = observe(1)
        val b = observe(2)
        val collected: Observable<Option<Int>> =
            listOf<Observable<Int>>(a, b).combineLatest { values ->
                values.sum()
            }
        assertEquals(0, a.observerCount)
        assertEquals(0, b.observerCount)
        val subscription = collected.onChange { }
        assertTrue(a.isObserved)
        assertTrue(b.isObserved)
        subscription.dispose()
        assertEquals(0, a.observerCount)
        assertEquals(0, b.observerCount)
    }

    @Test
    fun `currentOrNull returns the content of an optional observable, or null`() {
        val someObs = observe(10.some())
        val noneObs = observe(none<Int>())
        assertEquals(10, someObs.currentOrNull())
        assertNull(noneObs.currentOrNull())
    }

    @Test
    fun `flatMap emits None immediately if the source set is empty`() {
        val set = ObservableMutableSet<String>()
        val fused = set.flatMap { observe(10) }
        val seen = mutableListOf<Option<Int>>()
        fused.onChange { seen.add(it) }
        assertEquals(listOf(none()), seen)
    }

    @Test
    fun `flatMap emits None when the source set becomes empty`() {
        val set = ObservableMutableSet("a")
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        val fused = set.flatMap(::observableFor)
        val seen = mutableListOf<Option<Int>>()
        fused.onChange { seen.add(it) }
        inner.getValue("a").update { 7 }
        set.remove("a")
        assertEquals(listOf(0.some(), 7.some(), none()), seen)
    }

    @Test
    fun `flatMap emits the current values of new elements and reacts to their emissions`() {
        val set = ObservableMutableSet<String>()
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        set.add("a")
        val fused = set.flatMap(::observableFor)
        val seen = mutableListOf<Int>()
        fused.onChange { seen.add(it.getOrElse { -1 }) }
        assertEquals(0, seen[0])
        inner["b"] = observe(10)
        set.add("b")
        inner.getValue("b").update { 20 }
        set.add("c")
        assertEquals(listOf(0, 10, 20, 0), seen)
    }

    @Test
    fun `flatMap unsubscribes from the inner observables when released`() {
        val set = ObservableMutableSet("a")
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        val fused = set.flatMap(::observableFor)
        val subscription = fused.onChange { }
        set.add("b")
        assertEquals(1, inner.getValue("a").observerCount)
        assertEquals(1, inner.getValue("b").observerCount)
        subscription.dispose()
        assertEquals(0, inner.getValue("a").observerCount)
        assertEquals(0, inner.getValue("b").observerCount)
    }

    @Test
    fun `flattenMerge emits None immediately if the set is empty`() {
        val set = ObservableMutableSet<Observable<Int>>()
        val merged = set.flattenMerge()
        val seen = mutableListOf<Any?>()
        merged.onChange { seen.add(it.getOrNull()) }
        assertEquals(listOf<Any?>(null), seen)
    }

    @Test
    fun `flattenMerge emits None when the set becomes empty, then Some when it is filled again`() {
        val a = observe(1)
        val set = ObservableMutableSet<Observable<Int>>(a)
        val merged = set.flattenMerge()
        val seen = mutableListOf<Any?>()
        merged.onChange { seen.add(it.getOrNull()) }
        set.remove(a)
        val b = observe(9)
        set.add(b)
        assertEquals(listOf<Any?>(1, null, 9), seen)
    }

    @Test
    fun `multiple observers are all notified`() {
        val observable = observe(0)
        var aCount = -1
        var bCount = -1
        observable.onChange { aCount++ }
        observable.onChange { bCount++ }
        repeat(3) { observable.update { curr -> curr + 1 } }
        assertEquals(3, aCount)
        assertEquals(3, bCount)
        assertEquals(2, observable.observerCount)
    }

    @Test
    fun `onChange immediately emits the current value to the observer`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange { lastSeen = it }
        assertEquals(42, lastSeen)
    }

    @Test
    fun `onChange with notifyOnRegistration emits the current value`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange(true) { lastSeen = it }
        assertEquals(42, lastSeen)
    }

    @Test
    fun `onChange without notifyOnRegistration does not emit the current value`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange(false) { lastSeen = it }
        assertNull(lastSeen)
    }

    @Test
    fun `subscription changes and re-entrant updates do not disrupt an emission`() {
        val observable = observe(0)
        val emissions = mutableListOf<String>()
        lateinit var first: Disposable
        first = observable.onChange(notifyOnRegistration = false) { value ->
            emissions += "first:$value"
            if (value == 1) {
                first.dispose()
                observable.onChange(notifyOnRegistration = false) { emissions += "late:$it" }
                observable.current = 2
            }
        }
        observable.onChange(notifyOnRegistration = false) { emissions += "second:$it" }
        observable.current = 1
        // The outdated value is not delivered after the newer one.
        assertEquals(listOf("first:1", "second:2", "late:2"), emissions)
    }

    @Test
    fun `subscriptions are disposed independently`() {
        val observable = observe(0)
        var firstCount = 0
        var secondCount = 0
        val first = observable.onChange(notifyOnRegistration = false) { firstCount++ }
        val second = observable.onChange(notifyOnRegistration = false) { secondCount++ }
        assertEquals(2, observable.observerCount)
        first.dispose()
        first.dispose()
        observable.update { it + 1 }
        assertEquals(0, firstCount)
        assertEquals(1, secondCount)
        assertEquals(1, observable.observerCount)
        second.dispose()
        assertEquals(0, observable.observerCount)
    }

    @Test
    fun `update returns the previous value and updates the current one`() {
        val observable = observe(0)
        val old = observable.update { it + 5 }
        assertEquals(0, old)
        assertEquals(5, observable.current)
    }

    @Test
    fun `updateValue updates the content of an optional observable`() {
        val someObs = observe(1.some())
        val noneObs = observe(none<Int>())
        listOf(someObs, noneObs).forEach { obs -> obs.updateValue { it + 1 } }
        assertEquals(2.some(), someObs.current)
        assertEquals(none(), noneObs.current)
    }

    @Test
    fun `updating an observable with its own value emits nothing`() {
        val observable = observe(10)
        var changesCount = -1
        observable.onChange { changesCount++ }
        repeat(10) { _ ->
            observable.update { it }
        }
        assertEquals(0, changesCount)
    }
}
