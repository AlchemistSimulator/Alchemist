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
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.ObservableMutableList.Companion.toObservableList
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.ObservableLists.combineLatest
import it.unibo.alchemist.model.observables.util.ObservableLists.flatMap
import it.unibo.alchemist.model.observables.util.ObservableLists.merge

class ObservableListTest : FunSpec({
    context("ObservableList factories tests") {

        test("an observable list can be created with varargs") {
            val list = ObservableMutableList(1, 2, 3)
            with(list.current) {
                this shouldContainExactly listOf(1, 2, 3)
                this.size shouldBe 3
            }
            (1 in list.current) shouldBe true
            (4 in list.current) shouldBe false
        }

        test("List.toObservableList should create observable list with same elements and order") {
            val src = listOf(1, 2, 2, 3, 3, 3)
            val list = src.toObservableList()
            with(list.current) {
                this shouldContainExactly listOf(1, 2, 2, 3, 3, 3)
                this.size shouldBe 6
            }
        }
    }

    context("basic list operations tests") {
        test("add should notify observers") {
            val list = ObservableMutableList<Int>()
            val seen = mutableListOf<List<Int>>()
            list.onChange(this) { seen.add(it) }
            list.add(1)
            list.add(2)
            list.add(1) // duplicates allowed
            seen.size shouldBe 4
            seen[0] shouldContainExactly emptyList()
            seen[1] shouldContainExactly listOf(1)
            seen[2] shouldContainExactly listOf(1, 2)
            seen[3] shouldContainExactly listOf(1, 2, 1)
        }

        test("add at index should insert and notify") {
            val list = ObservableMutableList(1, 3)
            val seen = mutableListOf<List<Int>>()
            list.onChange(this) { seen.add(it) }
            list.add(1, 2)
            seen.size shouldBe 2
            list.current shouldContainExactly listOf(1, 2, 3)
        }

        test("addAll should append collection and notify") {
            val list = ObservableMutableList(1)
            val seen = mutableListOf<List<Int>>()
            list.onChange(this) { seen.add(it) }
            list.addAll(listOf(2, 3))
            seen.size shouldBe 2
            list.current shouldContainExactly listOf(1, 2, 3)
        }

        test("list size should be observable") {
            val list = ObservableMutableList(1, 2, 3)
            var nextExpected = 3
            list.size.onChange(this, false) { it shouldBe nextExpected }
            repeat(3) {
                nextExpected++
                list.add(it + 100)
            }
        }

        test("remove should notify observers only when element is present") {
            val list = ObservableMutableList("a", "b", "c")
            val seen = mutableListOf<List<String>>()
            list.onChange(this) { seen.add(it) }
            seen.size shouldBe 1
            seen[0] shouldContainExactly listOf("a", "b", "c")
            list.remove("b")
            list.remove("x")
            seen.size shouldBe 2
            seen[1] shouldContainExactly listOf("a", "c")
        }

        test("removeAt should remove by index and notify") {
            val list = ObservableMutableList("a", "b", "c")
            val seen = mutableListOf<List<String>>()
            list.onChange(this) { seen.add(it) }
            list.removeAt(1)
            seen.size shouldBe 2
            seen[1] shouldContainExactly listOf("a", "c")
        }

        test("set should replace element and notify") {
            val list = ObservableMutableList("a", "b", "c")
            val seen = mutableListOf<List<String>>()
            list.onChange(this) { seen.add(it) }
            list[1] = "z"
            list[1] = "z"
            seen.size shouldBe 2
            seen[1] shouldContainExactly listOf("a", "z", "c")
        }

        test("current should reject mutation") {
            val list = ObservableMutableList(1, 2, 3)
            shouldThrowAny { (list.current as MutableList<Int>).add(4) }
            list.current shouldContainExactly listOf(1, 2, 3)
        }

        test("clear should empty the list and notify") {
            val list = ObservableMutableList(1, 2, 3)
            val seen = mutableListOf<List<Int>>()
            list.onChange(this) { seen.add(it) }
            list.clear()
            seen.last() shouldContainExactly emptyList()
            list.current shouldContainExactly emptyList()
        }
    }

    context("ObservableList extensions") {
        test("combineLatest should aggregate mapped observables") {
            val list = ObservableMutableList("a", "b")
            val sources = mapOf(
                "a" to observe(1),
                "b" to observe(2),
                "c" to observe(3),
            )
            val combined = list.combineLatest(
                transformer = { sources[it] ?: observe(-1) },
                aggregator = { it.sum() },
            )
            var currentSum = 0
            combined.onChange(this) { currentSum = it }
            currentSum shouldBe 3
            list.add("c")
            currentSum shouldBe 6
            sources["a"]?.update { 10 }
            currentSum shouldBe 15
            list.remove("b")
            currentSum shouldBe 13
        }

        test("flatMap should flatten observable") {
            val list = ObservableMutableList("a")
            val sources = mapOf(
                "a" to observe("A"),
                "b" to observe("B"),
            )
            val flattened = list.flatMap { sources[it] ?: observe("") }
            var currentVal = ""
            flattened.onChange(this) { currentVal = it.map { v -> v }.getOrNull() ?: "NONE" }
            currentVal shouldBe "A"
            list.add(0, "b")
            currentVal shouldBe "B"
            sources["b"]?.update { "Z" }
            currentVal shouldBe "Z"
            list.clear()
            flattened.current shouldBe none()
        }

        test("merging a list should emit updates both for list and members") {
            val sources = listOf(observe(10), observe(20), observe(30))
            val list: ObservableMutableList<Observable<Int>> = sources.toObservableList()
            var changeCounter = 0
            list.merge().onChange(this) { changeCounter++ }
            val baseline = changeCounter
            sources[0].update { it + 100 }
            changeCounter shouldBe baseline + 1
            list.add(observe(40))
            changeCounter shouldBe baseline + 2
        }
    }

    context("explicit mutations") {
        test("add and remove should mutate the list") {
            val list = ObservableMutableList<Int>()
            list.add(1)
            list.add(2)
            list.current shouldContainExactly listOf(1, 2)
            list.remove(1)
            list.current shouldContainExactly listOf(2)
        }
    }
})
