/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TestCanonicalJson : StringSpec({

    "map keys are sorted, producing a deterministic result" {
        CanonicalJson.encode(mapOf("b" to "1", "a" to "2")) shouldBe """{"a":"2","b":"1"}"""
    }

    "the same map written with different key order encodes identically" {
        val items = listOf("dataset" to "x", "variable" to listOf("dis24"), "year" to "2024")
        val map1 = mapOf(*items.toTypedArray())
        val map2 = mapOf(*items.reversed().toTypedArray())
        CanonicalJson.encode(map1) shouldBe CanonicalJson.encode(map2)
    }

    "key sorting recurses into nested maps" {
        val a = mapOf("outer" to mapOf("b" to "1", "a" to "2"))
        val b = mapOf("outer" to mapOf("a" to "2", "b" to "1"))
        CanonicalJson.encode(a) shouldBe CanonicalJson.encode(b)
    }

    "list order is significant: reordering elements yields a different encoding" {
        val list = listOf(50.0, 5.0, 45.0, 10.0)
        val nwse = mapOf("area" to list)
        val shuffled = mapOf("area" to list.reversed())
        CanonicalJson.encode(nwse) shouldNotBe CanonicalJson.encode(shuffled)
    }

    "list order is preserved verbatim, including zero-padded strings" {
        CanonicalJson.encode(mapOf("day" to listOf("02", "01"))) shouldBe """{"day":["02","01"]}"""
    }

    "explicit nulls are encoded, distinguishing a present but null-key from an absent one" {
        CanonicalJson.encode(mapOf("a" to null)) shouldNotBe CanonicalJson.encode(emptyMap<String, Any?>())
    }

    "a realistic GloFAS request is order-stable across its keys" {
        val request = mapOf(
            "system_version" to listOf("version_3_1"),
            "hydrological_model" to listOf("lisflood"),
            "variable" to listOf("river_discharge_in_the_last_24_hours"),
            "hyear" to listOf("2024"),
            "hmonth" to listOf("06"),
            "hday" to listOf("10"),
            "data_format" to "netcdf",
        )
        val reordered = mapOf(
            "data_format" to "netcdf",
            "variable" to listOf("river_discharge_in_the_last_24_hours"),
            "hday" to listOf("10"),
            "hmonth" to listOf("06"),
            "hyear" to listOf("2024"),
            "hydrological_model" to listOf("lisflood"),
            "system_version" to listOf("version_3_1"),
        )
        CanonicalJson.encode(request) shouldBe CanonicalJson.encode(reordered)
    }
})
