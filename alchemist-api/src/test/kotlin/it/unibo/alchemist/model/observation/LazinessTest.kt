/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.model.observation.ObservableExtensions.ObservableSetExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.combineLatest
import it.unibo.alchemist.model.observation.ObservableExtensions.switchMap

class LazinessTest : FunSpec({

    context("DerivedObservable laziness") {

        test("should not compute fresh value if registered lazily") {
            var computations = 0
            val derived = object : DerivedObservable<Int>() {
                override fun computeFresh(): Int {
                    computations++
                    return 26
                }

                override fun startMonitoring() = Unit
                override fun stopMonitoring() = Unit
            }
            derived.onChange(this, false) { }
            computations shouldBe 0
        }

        test("should compute fresh value if registered eagerly") {
            var computations = 0
            val derived = object : DerivedObservable<Int>() {
                override fun computeFresh(): Int {
                    computations++
                    return 26
                }

                override fun startMonitoring() = Unit
                override fun stopMonitoring() = Unit
            }

            derived.onChange(this, true) { }
            computations shouldBe 1
        }
    }

    context("Map laziness") {
        test("map should not apply transform if registered lazily") {
            val source = observe(1)
            var transforms = 0
            val mapped = source.map {
                transforms++
                it * 2
            }

            mapped.onChange(this, false) { }
            transforms shouldBe 0
        }

        test("map should apply transform if registered eagerly") {
            val source = observe(1)
            var transforms = 0
            val mapped = source.map {
                transforms++
                it * 2
            }

            mapped.onChange(this, true) { }
            transforms shouldBe 1
        }

        test("map should receive updates even if registered lazily") {
            val source = observe(1)
            val mapped = source.map { it * 2 }
            var lastSeen: Int? = null

            mapped.onChange(this, false) { lastSeen = it }
            source.update { 2 }
            lastSeen shouldBe 4
        }
    }

    context("Merge laziness") {
        test("mergeWith should not merge if registered lazily") {
            val a = observe(1)
            val b = observe(2)
            var merges = 0
            val merged = a.mergeWith(b) { x, y ->
                merges++
                x + y
            }
            merged.onChange(this, false) { }
            merges shouldBe 0
        }

        test("mergeWith should receive updates from both sources if registered lazily") {
            val a = observe(1)
            val b = observe(2)
            val merged = a.mergeWith(b) { x, y -> x + y }
            var lastSeen: Int? = null

            merged.onChange(this, false) { lastSeen = it }

            a.update { 3 }
            lastSeen shouldBe 5

            b.update { 4 }
            lastSeen shouldBe 7
        }
    }

    context("Extension methods laziness") {

        test("combineLatest (collection) should not aggregate if registered lazily") {
            val set = ObservableMutableSet(1, 2)
            var aggregations = 0
            val combined = set.combineLatest(
                map = { observe(it) },
                aggregator = {
                    aggregations++
                    it.sum()
                },
            )

            combined.onChange(this, false) { }
            aggregations shouldBe 0
        }

        test("combineLatest (collection) should receive updates if registered lazily") {
            val set = ObservableMutableSet("a")
            val inner = observe(10)
            val combined = set.combineLatest(
                map = { inner },
                aggregator = { it.sum() },
            )
            var lastSeen: Int? = null

            combined.onChange(this, false) { lastSeen = it }

            inner.update { 20 }
            lastSeen shouldBe 20

            set.add("b")
            lastSeen shouldBe 40 // 20 + 20 (both map to same inner)
        }

        test("combineLatest (list) should not collect if registered lazily") {
            val list = listOf(observe(1), observe(2))
            var collections = 0
            val combined = list.combineLatest {
                collections++
                it.sum()
            }

            combined.onChange(this, false) { }
            collections shouldBe 0
        }

        test("switchMap should not transform if registered lazily") {
            val source = observe(1)
            var transforms = 0
            val switched = source.switchMap {
                transforms++
                observe(it)
            }
            switched.onChange(this, false) { }
            transforms shouldBe 1
        }

        test("switchMap should work correctly after lazy registration") {
            val source = observe(1)
            val switched = source.switchMap { observe(it * 10) }
            var lastSeen: Int? = null

            switched.onChange(this, false) { lastSeen = it }

            source.update { 2 }
            lastSeen shouldBe 20
        }
    }
})
