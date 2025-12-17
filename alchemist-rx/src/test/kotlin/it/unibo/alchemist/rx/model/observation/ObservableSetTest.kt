/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.observation

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.rx.model.observation.MutableObservable.Companion.observe
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.filter
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.merge
import it.unibo.alchemist.rx.model.observation.ObservableExtensions.ObservableSetExtensions.union
import it.unibo.alchemist.rx.model.observation.ObservableMutableSet.Companion.toObservableSet

class ObservableSetTest : FunSpec({
    context("ObservableSet factories tests") {

        test("an observable set can be created with varaargs") {
            val set = ObservableSet(1, 2, 3)

            with(set.toSet()) {
                this shouldContainExactlyInAnyOrder listOf(1, 2, 3)
                this.size shouldBe 3
            }

            (1 in set) shouldBe true
            (4 in set) shouldBe false
        }

        test("List.toObservableSet should create observable set with unique elements") {
            val list = listOf(1, 2, 2, 3, 3, 3)

            val set = list.toObservableSet()

            with(set.toSet()) {
                this shouldContainExactlyInAnyOrder listOf(1, 2, 3)
                this.size shouldBe 3
            }
        }

        test("Set.toObservableSet should preserve all elements") {
            val src = setOf("a", "b", "c")

            val set = src.toObservableSet()

            with(set.toSet()) {
                this shouldContainExactlyInAnyOrder listOf("a", "b", "c")
                this.size shouldBe 3
            }
        }
    }

    context("basic set operations tests") {
        test("add should notify observers only on real changes") {
            val set = ObservableMutableSet<Int>()

            val seen = mutableListOf<Set<Int>>()

            set.onChange(this) { seen.add(it) }

            set.add(1)
            set.add(2)
            set.add(2) // duplicate, should not change

            seen.size shouldBe 3
            seen[1] shouldContainExactlyInAnyOrder listOf(1)
            seen[2] shouldContainExactlyInAnyOrder listOf(1, 2)
        }

        test("set size should be observable") {
            val set = ObservableMutableSet(1, 2, 3)
            var nextExpected = 3

            set.observableSize.onChange(this) { it shouldBe nextExpected }

            repeat(3) {
                nextExpected++
                set.add(it + 100)
            }
        }

        test("remove should notify observers only when element is present") {
            val set = ObservableMutableSet("a", "b", "c")

            val seen = mutableListOf<Set<String>>()
            set.onChange(this) { seen.add(it) }

            seen.size shouldBe 1
            seen[0] shouldContainExactlyInAnyOrder listOf("a", "b", "c")

            set.remove("b")
            set.remove("b")
            set.remove("x")

            seen.size shouldBe 2
            seen[1] shouldContainExactlyInAnyOrder listOf("a", "c")
        }

        test("observeMembership should emit false when element not present and true when added") {
            val set = ObservableMutableSet<String>()

            val membership = set.observeMembership("foo")

            val seen = mutableListOf<Boolean>()
            membership.onChange(this) { seen.add(it) }

            set.add("foo")
            set.add("foo")

            seen.size shouldBe 2
            seen[1] shouldBe true

            set.remove("foo")
            seen.size shouldBe 3
            seen[2] shouldBe false
        }

        test("copy should create independent set with same elements") {
            val original = ObservableMutableSet(1, 2, 3)

            val copy = original.copy()

            original.toSet() shouldContainExactlyInAnyOrder copy.toSet()

            copy.add(4)
            copy.remove(1)

            original.toSet() shouldContainExactlyInAnyOrder listOf(1, 2, 3)
            copy.toSet() shouldContainExactlyInAnyOrder listOf(2, 3, 4)
        }
    }

    context("ObservableSet extensions") {

        test("filter should filter set elements") {
            val set = ObservableMutableSet(1, 2, 3, 4, 5)
            set.filter { it % 2 == 0 }.toSet() shouldContainExactlyInAnyOrder listOf(2, 4)
        }

        test("merging a set should emit updates both for sets and members") {
            val sources = listOf(observe(10), observe(20), observe(30))
            val set: ObservableSet<Observable<Int>> = sources.toObservableSet()

            var changeCounter = 0
            set.merge().onChange(this) { changeCounter++ }

            val baseline = changeCounter
            sources[0].update { it + 100 }
            changeCounter shouldBe baseline + 1
        }
    }

    context("set operators tests") {
        test("plus and minus operators should mirror add and remove") {
            val set = ObservableMutableSet<Int>()

            set + 1
            set + 2
            (1 in set) shouldBe true
            (2 in set) shouldBe true
            (3 in set) shouldBe false

            set - 1
            (1 in set) shouldBe false
            (2 in set) shouldBe true
        }

        test("union should perform set union of two sets") {
            val s1 = ObservableSet(1, 2, 3)
            val s2 = ObservableSet(2, 3, 4)

            (s1 union s2).toSet() shouldContainExactlyInAnyOrder setOf(1, 2, 3, 4)
        }
    }
})
