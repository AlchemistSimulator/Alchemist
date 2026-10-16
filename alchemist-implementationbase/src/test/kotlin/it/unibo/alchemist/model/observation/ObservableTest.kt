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
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObservableTest {
    @Test
    fun `a disposed EventObservable has no observers and notifies nobody`() {
        val event = EventObservable()
        var calls = -2
        val registrant1 = "R1"
        val registrant2 = "R2"
        event.onChange(registrant1) { calls++ }
        event.onChange(registrant2) { calls++ }
        event.emit()
        event.dispose()
        event.emit()
        assertEquals(2, calls)
        assertTrue(event.observers.isEmpty())
    }

    @Test
    fun `a disposed observable does not notify its observers`() {
        val observable = observe(10)
        var changesCount = -1
        observable.onChange(this) { changesCount++ }
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
    fun `a mapped observable registered without invokeOnRegistration emits only when the mapped value changes`() {
        val counter = observe(0)
        val parities = mutableListOf<Int>()
        counter.map { it % 2 }.onChange(this, false) { parities += it }
        counter.update { 2 }
        assertEquals(emptyList(), parities)
        counter.update { 3 }
        assertEquals(listOf(1), parities)
    }

    @Test
    fun `a mapped observable emits only when the mapped value changes`() {
        val observable = observe(0)
        val parityObservable = observable.map { it % 2 == 0 }
        var emissions = -1
        parityObservable.onChange(this) { emissions++ }
        // Sequence that keeps parity constant: 0 -> 2 -> 4 -> 6
        repeat(3) {
            observable.update { curr -> curr + 2 }
        }
        assertEquals(0, emissions)
    }

    @Test
    fun `a merged observable emits only when the merged value changes`() {
        val a = observe(0)
        val b = observe(0)
        val c = a.mergeWith(b) { first, second -> (first + second) % 2 }
        var emissions = -1
        c.onChange(this) { emissions++ }
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
        merged.onChange(this, invokeOnRegistration = false) { counter++ }
        obsA.update { it + 11 }
        elements.add(observe(29))
        assertEquals(2, counter)
    }

    @Test
    fun `an observable publishing while it starts monitoring delivers only the newer value to its first observer`() {
        val observable = object : AbstractObservable<Int>() {
            var value = 1

            override fun computeFresh(): Int = value

            override fun startMonitoring() {
                value = 2
                updateAndNotify(value)
            }

            override fun stopMonitoring() = Unit
        }
        val seen = mutableListOf<Int>()
        observable.subscribe { seen += it }
        // an observable that publishes 2 while starting to monitor must deliver only [2] to its first observer.
        // (it used to deliver 2, then 1)
        assertEquals(listOf(2), seen)
    }

    @Test
    fun `an EventObservable notifies all observers on every emission`() {
        val event = EventObservable()
        var calls = -1
        event.onChange(this) { calls++ }
        repeat(5) { event.emit() }
        assertEquals(5, calls)
    }

    @Test
    fun `an EventObservable stops notifying a registrant that stopped watching`() {
        val event = EventObservable()
        val registrant = "R"
        var calls = -1
        event.onChange(registrant) { calls++ }
        event.emit()
        event.stopWatching(registrant)
        event.emit()
        assertEquals(1, calls)
        assertFalse(registrant in event.observers)
    }

    @Test
    fun `an EventObservable supports multiple registrants`() {
        val event = EventObservable()
        var aCalls = -1
        var bCalls = -1
        val registrantA = "A"
        val registrantB = "B"
        event.onChange(registrantA) { aCalls++ }
        event.onChange(registrantB) { bCalls++ }
        repeat(3) { event.emit() }
        assertEquals(3, aCalls)
        assertEquals(3, bCalls)
        assertEquals(2, event.observers.size)
    }

    @Test
    fun `an observable can be mapped into another`() {
        val observable = observe(10)
        val strObservable = observable.map { it.toString() }
        var changesCounts = -1
        strObservable.onChange(this) { changesCounts++ }
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
        c.onChange(this) { current = it }
        a.update { it + 10 }
        assertEquals(10, current)
        b.update { it + 10 }
        assertEquals(20, current)
    }

    @Test
    fun `an observable can be stopped watching`() {
        val observable = observe(10)
        var changesCount = -1
        observable.onChange(this) { changesCount++ }
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        observable.stopWatching(this)
        repeat(10) {
            observable.update { curr -> curr + 10 }
        }
        assertEquals(10, changesCount)
    }

    @Test
    fun `an observable may emit an event when assigned an equal value`() {
        val observable = observe(initial = 0, emitOnDistinct = false)
        var emissions = 0
        observable.subscribe(invokeOnSubscription = false) { emissions++ }
        observable.current = 0
        assertEquals(1, emissions)
    }

    @Test
    fun `changes of a value can be observed`() {
        val observable = observe(10)
        var changesCount = -1 // The first subscription always calls the observing callbacks.
        observable.onChange(this) { changesCount++ }
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
        collected.onChange(this) { emissions++ }
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
        collected.onChange(this) { emissions.add(it.getOrElse { "" }) }
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
        combined.onChange(this) { seen.add(it) }
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
    fun `combineLatest on a set unsubscribes from the inner observables on stopWatching`() {
        val set = ObservableMutableSet<String>()
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        set.add("x")
        set.add("y")
        val combined = set.combineLatest(transformer = ::observableFor, aggregator = { values -> values.sum() })
        val registrant = "test-registrant"
        combined.onChange(registrant) { }
        assertEquals(1, inner.getValue("x").observers.size)
        assertEquals(1, inner.getValue("y").observers.size)
        combined.stopWatching(registrant)
        assertEquals(0, inner.getValue("x").observers.size)
        assertEquals(0, inner.getValue("y").observers.size)
    }

    @Test
    fun `combineLatest unsubscribes from its sources when the last observer stops watching`() {
        val a = observe(1)
        val b = observe(2)
        val collected: Observable<Option<Int>> =
            listOf<Observable<Int>>(a, b).combineLatest { values ->
                values.sum()
            }
        val registrant = "collector-observer"
        assertEquals(0, a.observers.size)
        assertEquals(0, b.observers.size)
        collected.onChange(registrant) { }
        assertTrue(a.observers.isNotEmpty())
        assertTrue(b.observers.isNotEmpty())
        collected.stopWatching(registrant)
        assertEquals(0, a.observers.size)
        assertEquals(0, b.observers.size)
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
        fused.onChange(this) { seen.add(it) }
        assertEquals(listOf(none()), seen)
    }

    @Test
    fun `flatMap emits None when the source set becomes empty`() {
        val set = ObservableMutableSet("a")
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        val fused = set.flatMap(::observableFor)
        val seen = mutableListOf<Option<Int>>()
        fused.onChange(this) { seen.add(it) }
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
        fused.onChange(this) { seen.add(it.getOrElse { -1 }) }
        assertEquals(0, seen[0])
        inner["b"] = observe(10)
        set.add("b")
        inner.getValue("b").update { 20 }
        set.add("c")
        assertEquals(listOf(0, 10, 20, 0), seen)
    }

    @Test
    fun `flatMap unsubscribes from the inner observables on stopWatching`() {
        val set = ObservableMutableSet("a")
        val inner = mutableMapOf<String, MutableObservable<Int>>()
        fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }
        val fused = set.flatMap(::observableFor)
        fused.onChange(this) { }
        set.add("b")
        assertEquals(1, inner.getValue("a").observers.size)
        assertEquals(1, inner.getValue("b").observers.size)
        fused.stopWatching(this)
        assertEquals(0, inner.getValue("a").observers.size)
        assertEquals(0, inner.getValue("b").observers.size)
    }

    @Test
    fun `flattenMerge emits None immediately if the set is empty`() {
        val set = ObservableMutableSet<Observable<Int>>()
        val merged = set.flattenMerge()
        val seen = mutableListOf<Any?>()
        merged.onChange(this) { seen.add(it.getOrNull()) }
        assertEquals(listOf<Any?>(null), seen)
    }

    @Test
    fun `flattenMerge emits None when the set becomes empty, then Some when it is filled again`() {
        val a = observe(1)
        val set = ObservableMutableSet<Observable<Int>>(a)
        val merged = set.flattenMerge()
        val seen = mutableListOf<Any?>()
        merged.onChange(this) { seen.add(it.getOrNull()) }
        set.remove(a)
        val b = observe(9)
        set.add(b)
        assertEquals(listOf<Any?>(1, null, 9), seen)
    }

    @Test
    fun `multiple registrants are all notified`() {
        val observable = observe(0)
        var aCount = -1
        var bCount = -1
        val registrantA = "A"
        val registrantB = "B"
        observable.onChange(registrantA) { aCount++ }
        observable.onChange(registrantB) { bCount++ }
        repeat(3) { observable.update { curr -> curr + 1 } }
        assertEquals(3, aCount)
        assertEquals(3, bCount)
        assertContains(observable.observers, registrantA)
        assertContains(observable.observers, registrantB)
    }

    @Test
    fun `onChange immediately emits the current value to the observer`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange(this) { lastSeen = it }
        assertEquals(42, lastSeen)
    }

    @Test
    fun `onChange with invokeOnRegistration emits the current value`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange(this, true) { lastSeen = it }
        assertEquals(42, lastSeen)
    }

    @Test
    fun `onChange without invokeOnRegistration does not emit the current value`() {
        val observable = observe(42)
        var lastSeen: Int? = null
        observable.onChange(this, false) { lastSeen = it }
        assertNull(lastSeen)
    }

    @Test
    fun `subscription changes and re-entrant updates do not disrupt an emission`() {
        val observable = observe(0)
        val emissions = mutableListOf<String>()
        lateinit var first: Disposable
        first = observable.subscribe(invokeOnSubscription = false) { value ->
            emissions += "first:$value"
            if (value == 1) {
                first.dispose()
                observable.subscribe(invokeOnSubscription = false) { emissions += "late:$it" }
                observable.current = 2
            }
        }
        observable.subscribe(invokeOnSubscription = false) { emissions += "second:$it" }
        observable.current = 1
        // The outdated value is not delivered after the newer one.
        assertEquals(listOf("first:1", "second:2", "late:2"), emissions)
    }

    @Test
    fun `subscriptions are disposed independently`() {
        val observable = observe(0)
        var firstCount = 0
        var secondCount = 0
        val first = observable.subscribe(invokeOnSubscription = false) { firstCount++ }
        val second = observable.subscribe(invokeOnSubscription = false) { secondCount++ }
        assertEquals(2, observable.observers.size)
        first.dispose()
        first.dispose()
        observable.update { it + 1 }
        assertEquals(0, firstCount)
        assertEquals(1, secondCount)
        assertEquals(1, observable.observers.size)
        second.dispose()
        assertEquals(0, observable.observers.size)
    }

    @Test
    fun `the same registrant can have multiple callbacks`() {
        val observable = observe(0)
        var sum = 0
        var count = -1
        val registrant = "same-registrant"
        observable.onChange(registrant) { sum += it }
        observable.onChange(registrant) { count++ }
        repeat(3) { observable.update { curr -> curr + 1 } }
        assertEquals(1 + 2 + 3, sum)
        assertEquals(3, count)
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
        observable.onChange(this) { changesCount++ }
        repeat(10) { _ ->
            observable.update { it }
        }
        assertEquals(0, changesCount)
    }
}
