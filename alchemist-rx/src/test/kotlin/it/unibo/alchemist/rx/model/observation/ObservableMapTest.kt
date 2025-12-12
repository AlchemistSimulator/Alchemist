/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model.observation

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.rx.model.observation.ObservableMutableMap.ObservableMapExtensions.upsertValue

class ObservableMapTest : FunSpec({
    context("basic map operations") {
        test("put should insert and update values and notify map observers only on real changes") {
            val map = ObservableMutableMap<String, Int>()

            val seen = mutableListOf<Map<String, Int>>()
            map.onChange(this) { seen.add(it) }

            seen.size shouldBe 1
            seen[0].isEmpty() shouldBe true

            map.put("a", 1)
            map.put("b", 2)
            map.put("b", 3)

            seen.size shouldBe 4
            seen[1] shouldContainExactly mapOf("a" to 1)
            seen[2] shouldContainExactly mapOf("a" to 1, "b" to 2)
            seen[3] shouldContainExactly mapOf("a" to 1, "b" to 3)

            map.asMap() shouldContainExactly mapOf("a" to 1, "b" to 3)
        }

        test("set operator and plus operator should delegate to put") {
            val map = ObservableMutableMap<String, Int>()

            map["a"] = 1
            map + ("b" to 2)

            map.asMap() shouldContainExactly mapOf("a" to 1, "b" to 2)
        }

        test("remove and minus should notify observers only when key exists") {
            val map = ObservableMutableMap<String, Int>()

            val seen = mutableListOf<Map<String, Int>>()
            map.onChange(this) { seen.add(it) }

            map["a"] = 1
            map["b"] = 2

            seen[1] shouldContainExactly mapOf("a" to 1)
            seen[2] shouldContainExactly mapOf("a" to 1, "b" to 2)

            map.remove("b")
            map.remove("b")
            map - "x"

            seen.last() shouldContainExactly mapOf("a" to 1)
        }

        test("get(key) observable should emit None when absent, Some when present, and track updates") {
            val map = ObservableMutableMap<String, Int>()

            val obs: Observable<Option<Int>> = map["k"]

            val seen = mutableListOf<Option<Int>>()
            obs.onChange(this) { seen.add(it) }

            seen[0] shouldBe none()

            map["k"] = 42
            map["k"] = 100

            seen.size shouldBe 3
            seen[1].getOrElse { -1 } shouldBe 42
            seen[2].getOrElse { -1 } shouldBe 100

            map.remove("k")

            seen.size shouldBe 4
            seen[3] shouldBe none()
        }

        test("copy should produce independent map with same entries") {
            val original = ObservableMutableMap(
                mutableMapOf("a" to 1, "b" to 2),
            )

            val copy = original.copy()

            original.asMap() shouldContainExactly copy.asMap()

            copy["c"] = 3
            copy.remove("a")

            original.asMap() shouldContainExactly mapOf("a" to 1, "b" to 2)
            copy.asMap() shouldContainExactly mapOf("b" to 2, "c" to 3)
        }

        context("ObservableMap extension utils tests") {

            test("upsertValue should insert when key is missing") {
                with(ObservableMutableMap<String, Int>()) {
                    upsertValue("k") { curr -> (curr ?: 0) + 1 }
                    asMap() shouldContainExactly mapOf("k" to 1)
                }
            }

            test("upsertValue should update when key exists") {
                with(ObservableMutableMap<String, Int>()) {
                    this["k"] = 10
                    upsertValue("k") { curr -> (curr ?: 0) + 5 }
                    asMap() shouldContainExactly mapOf("k" to 15)
                }
            }
        }
    }
})
