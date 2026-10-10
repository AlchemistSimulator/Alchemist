/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.observation

import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.observables.ObservableMutableSet
import it.unibo.alchemist.model.observables.ObservableMutableSet.Companion.toObservableSet
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.ObservableSets.filter
import it.unibo.alchemist.model.observables.util.ObservableSets.merge
import it.unibo.alchemist.model.observables.util.ObservableSets.union

class ObservableSetTest : FunSpec({
    context("ObservableSet factories tests") {

        test("an observable set can be created with varaargs") {
            val set = ObservableMutableSet(1, 2, 3)
            with(set.current) {
                this shouldContainExactlyInAnyOrder listOf(1, 2, 3)
                this.size shouldBe 3
            }
            (1 in set.current) shouldBe true
            (4 in set.current) shouldBe false
        }

        test("List.toObservableSet should create observable set with unique elements") {
            val list = listOf(1, 2, 2, 3, 3, 3)
            val set = list.toObservableSet()
            with(set.current) {
                this shouldContainExactlyInAnyOrder listOf(1, 2, 3)
                this.size shouldBe 3
            }
        }

        test("Set.toObservableSet should preserve all elements") {
            val src = setOf("a", "b", "c")
            val set = src.toObservableSet()
            with(set.current) {
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
            set.size.onChange(this) { it shouldBe nextExpected }
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

        test("containsItem should emit false when element not present and true when added") {
            val set = ObservableMutableSet<String>()
            val membership = set.containsItem("foo")
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

        test("current should reject mutation") {
            val set = ObservableMutableSet(1, 2, 3)
            shouldThrowAny { (set.current as MutableSet<Int>).add(4) }
            set.current shouldContainExactlyInAnyOrder listOf(1, 2, 3)
        }
    }

    context("ObservableSet extensions") {

        test("filter should filter set elements") {
            val set = ObservableMutableSet(1, 2, 3, 4, 5)
            set.filter { it % 2 == 0 }.current shouldContainExactlyInAnyOrder listOf(2, 4)
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

    context("explicit mutations") {
        test("add and remove should mutate the set") {
            val set = ObservableMutableSet<Int>()
            set.add(1)
            set.add(2)
            (1 in set.current) shouldBe true
            (2 in set.current) shouldBe true
            (3 in set.current) shouldBe false
            set.remove(1)
            (1 in set.current) shouldBe false
            (2 in set.current) shouldBe true
        }

        test("union should perform set union of two sets") {
            val s1 = ObservableMutableSet(1, 2, 3)
            val s2 = ObservableMutableSet(2, 3, 4)
            (s1 union s2).current shouldContainExactlyInAnyOrder setOf(1, 2, 3, 4)
        }
    }
})
