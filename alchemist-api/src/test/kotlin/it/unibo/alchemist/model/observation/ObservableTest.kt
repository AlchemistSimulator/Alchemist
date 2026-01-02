/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import arrow.core.none
import arrow.core.some
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.MutableObservable.Companion.updateValue
import it.unibo.alchemist.model.observation.Observable.ObservableExtensions.asMutable
import it.unibo.alchemist.model.observation.Observable.ObservableExtensions.currentOrNull
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.flatMap
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest

class ObservableTest : FunSpec({
    context("base `Observable` tests") {
        test("`onChange` should immediately emit current value to the observer") {
            val observable = observe(42)
            var lastSeen: Int? = null

            observable.onChange(this) { lastSeen = it }

            lastSeen shouldBe 42
        }

        test("`update` should return previous value and update current") {
            val observable = observe(0)

            val old = observable.update { it + 5 }

            old shouldBe 0
            observable.current shouldBe 5
        }

        test("it should be possibile to observe changes of a value") {
            val observable = observe(10)

            var changesCount = -1 // first sub always calls observing callbacks
            observable.onChange(this) { changesCount++ }

            repeat(10) {
                observable.update { curr -> curr + 10 }
            }

            changesCount shouldBe 10
        }

        test("it should be possible to stop watching an observable") {
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

            changesCount shouldBe 10
        }

        test("an observable should not notify observers if `dispose` has been called") {
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

            changesCount shouldBe 10
        }

        test("observers emission should be idempotent") {
            val observable = observe(10)
            var changesCount = -1
            observable.onChange(this) { changesCount++ }

            repeat(10) { _ ->
                observable.update { it } // update the value with itself
            }

            changesCount shouldBe 0
        }

        test("multiple registrants should all be notified") {
            val observable = observe(0)

            var aCount = -1
            var bCount = -1

            val registrantA = "A"
            val registrantB = "B"

            observable.onChange(registrantA) { aCount++ }
            observable.onChange(registrantB) { bCount++ }

            repeat(3) { observable.update { curr -> curr + 1 } }

            aCount shouldBe 3
            bCount shouldBe 3

            observable.observers shouldContain registrantA
            observable.observers shouldContain registrantB
        }

        test("same registrant can have multiple callbacks") {
            val observable = observe(0)

            var sum = 0
            var count = -1

            val registrant = "same-registrant"

            observable.onChange(registrant) { sum += it }
            observable.onChange(registrant) { count++ }

            repeat(3) { observable.update { curr -> curr + 1 } }

            sum shouldBe (1 + 2 + 3)
            count shouldBe 3
        }

        test("it should be possibile to map an observable into another") {
            val observable = observe(10)
            val strObservable = observable.map { it.toString() }

            var changesCounts = -1

            strObservable.onChange(this) { changesCounts++ }

            repeat(10) { observable.update { curr -> curr + 10 } }

            changesCounts shouldBe 10
            strObservable.current shouldBe "${10 * 11}"
        }

        test("a mapped observable should be idempotent wrt mapped values") {
            val observable = observe(0)
            val parityObservable = observable.map { it % 2 == 0 }

            var emissions = -1
            parityObservable.onChange(this) { emissions++ }

            // Sequence that keeps parity constant: 0 -> 2 -> 4 -> 6
            repeat(3) {
                observable.update { curr -> curr + 2 }
            }

            emissions shouldBe 0
        }

        test("it should be possibile to merge an observable into another") {
            val a = observe(0)
            val b = observe(0)

            val c = a.mergeWith(b) { first, second -> first + second }

            var current = 0
            c.onChange(this) { current = it }

            a.update { it + 10 }
            current shouldBe 10
            b.update { it + 10 }
            current shouldBe 20
        }

        test("a merged observable should be idempotent wrt merged values") {
            val a = observe(0)
            val b = observe(0)

            val c = a.mergeWith(b) { first, second -> (first + second) % 2 }

            var emissions = -1
            c.onChange(this) { emissions++ }

            repeat(5) { _ ->
                a.update { it + 2 }
                b.update { it + 2 }
            }

            emissions shouldBe 0
        }
    }

    context("`Observable` extensions tests") {

        test("`combineLatest` should emit combined value when any source changes") {
            val a = observe(1)
            val b = observe(10)

            val collected: MutableObservable<String> =
                listOf<Observable<Int>>(a, b).combineLatest { values ->
                    "${values[0]}-${values[1]}"
                }.asMutable()

            val emissions = mutableListOf<String>()

            collected.onChange(this) { emissions.add(it) }

            a.update { it + 1 }
            b.update { it + 5 }

            emissions[1] shouldBe "2-10"
            emissions[2] shouldBe "2-15"
        }

        test("`combineLatest` should be idempotent wrt collected result") {
            val a = observe(0)
            val b = observe(0)

            val collected: MutableObservable<Int> =
                listOf<Observable<Int>>(a, b).combineLatest { values ->
                    (values[0] + values[1]) % 2
                }.asMutable()

            var emissions = -1
            collected.onChange(this) { emissions++ }

            repeat(5) { _ ->
                a.update { it + 2 }
                b.update { it + 4 }
            }

            emissions shouldBe 0
        }

        test("`combineLatest` should unsubscribe sources when last observer stops watching") {
            val a = observe(1)
            val b = observe(2)

            val collected: MutableObservable<Int> =
                listOf<Observable<Int>>(a, b).combineLatest { values ->
                    values.sum()
                }.asMutable()

            val registrant = "collector-observer"

            a.observers.size shouldBe 0
            b.observers.size shouldBe 0

            collected.onChange(registrant) { }

            a.observers.size shouldBeGreaterThan 0
            b.observers.size shouldBeGreaterThan 0

            collected.stopWatching(registrant)

            a.observers.size shouldBe 0
            b.observers.size shouldBe 0
        }

        context("`Obsevable<Option<T>>` extensions tests") {

            test("`currentOrNull` should work as expected") {
                val someObs = observe(10.some())
                val noneObs = observe(none<Int>())

                someObs.currentOrNull() shouldBe 10
                noneObs.currentOrNull() shouldBe null
            }

            test("`updateValue` should update the inner Option") {
                val someObs = observe(1.some())
                val noneObs = observe(none<Int>())

                listOf(someObs, noneObs).forEach { obs -> obs.updateValue { it + 1 } }

                someObs.current shouldBe 2.some()
                noneObs.current shouldBe none()
            }
        }

        context("`combineLatest`") {

            test("should aggregate inner observable values and react to both set and inner changes") {
                val set = ObservableMutableSet<String>()

                val inner = mutableMapOf<String, MutableObservable<Int>>()

                fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }

                val combined = set.combineLatest(map = ::observableFor, aggregator = { values -> values.sum() })

                val seen = mutableListOf<Int>()
                combined.onChange(this) { seen.add(it) }

                seen[0] shouldBe 0

                set.add("a")
                set.add("b")

                seen.last() shouldBe 0

                inner["a"]!!.update { 10 }
                inner["b"]!!.update { 5 }

                seen shouldContainExactly listOf(0, 10, 15)

                set.remove("a")

                seen.last() shouldBe 5
            }

            test("should unsubscribe from removed elements' observables on stopWatching") {
                val set = ObservableMutableSet<String>()
                val inner = mutableMapOf<String, MutableObservable<Int>>()

                fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }

                set.add("x")
                set.add("y")

                val combined = set.combineLatest(map = ::observableFor, aggregator = { values -> values.sum() })

                val registrant = "test-registrant"
                combined.onChange(registrant) { }

                inner["x"]!!.observers.shouldHaveSize(1)
                inner["y"]!!.observers.shouldHaveSize(1)

                combined.stopWatching(registrant)

                inner["x"]!!.observers.shouldHaveSize(0)
                inner["y"]!!.observers.shouldHaveSize(0)
            }
        }

        context("flatMap") {

            test("should emit current values of newly added elements and react to their emissions") {
                val set = ObservableMutableSet<String>()

                val inner = mutableMapOf<String, MutableObservable<Int>>()

                fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }

                set.add("a")
                val fused = set.flatMap(::observableFor)

                val seen = mutableListOf<Int>()
                fused.onChange(this) { seen.add(it) }

                seen[0] shouldBe 0

                inner["b"] = observe(10)
                set.add("b")
                inner["b"]!!.update { 20 }
                set.add("c")

                //                                         a,  b,  b, c
                seen.shouldContainExactly(listOf(0, 10, 20, 0))
            }

            test("should unsubscribe from inner observables on stopWatching") {
                val set = ObservableMutableSet("a")
                val inner = mutableMapOf<String, MutableObservable<Int>>()

                fun observableFor(key: String): Observable<Int> = inner.getOrPut(key) { observe(0) }

                val fused = set.flatMap(::observableFor)
                fused.onChange(this) { }
                set.add("b")

                inner["a"]!!.observers.shouldHaveSize(1)
                inner["b"]!!.observers.shouldHaveSize(1)

                fused.stopWatching(this)

                inner["a"]!!.observers.shouldHaveSize(0)
                inner["b"]!!.observers.shouldHaveSize(0)
            }
        }
    }

    context("`EventObservers` tests") {
        test("EventObservable emit should notify all observers every time") {
            val event = EventObservable()

            var calls = -1
            event.onChange(this) { calls++ }

            repeat(5) { event.emit() }

            calls shouldBe 5
        }

        test("EventObservable should support multiple registrants") {
            val event = EventObservable()

            var aCalls = -1
            var bCalls = -1

            val registrantA = "A"
            val registrantB = "B"

            event.onChange(registrantA) { aCalls++ }
            event.onChange(registrantB) { bCalls++ }

            repeat(3) { event.emit() }

            aCalls shouldBe 3
            bCalls shouldBe 3

            event.observers.size shouldBe 2
        }

        test("EventObservable stopWatching should remove the registrant") {
            val event = EventObservable()
            val registrant = "R"

            var calls = -1
            event.onChange(registrant) { calls++ }

            event.emit()
            event.stopWatching(registrant)
            event.emit()

            calls shouldBe 1
            event.observers.contains(registrant) shouldBe false
        }

        test("EventObservable dispose should remove all observers and prevent further notifications") {
            val event = EventObservable()

            var calls = -2
            val registrant1 = "R1"
            val registrant2 = "R2"

            event.onChange(registrant1) { calls++ }
            event.onChange(registrant2) { calls++ }

            event.emit()

            event.dispose()

            event.emit()

            calls shouldBe 2
            event.observers.isEmpty() shouldBe true
        }
    }

})
