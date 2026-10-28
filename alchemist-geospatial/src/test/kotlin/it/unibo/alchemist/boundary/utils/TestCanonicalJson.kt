/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TestCanonicalJson {

    @Test
    fun `map keys are sorted, producing a deterministic result`() {
        assertEquals("""{"a":"2","b":"1"}""", CanonicalJson.encode(mapOf("b" to "1", "a" to "2")))
    }

    @Test
    fun `the same map written with different key order encodes identically`() {
        val items = listOf("dataset" to "x", "variable" to listOf("dis24"), "year" to "2024")
        val map1 = mapOf(*items.toTypedArray())
        val map2 = mapOf(*items.reversed().toTypedArray())
        assertEquals(CanonicalJson.encode(map2), CanonicalJson.encode(map1))
    }

    @Test
    fun `key sorting recurses into nested maps`() {
        val a = mapOf("outer" to mapOf("b" to "1", "a" to "2"))
        val b = mapOf("outer" to mapOf("a" to "2", "b" to "1"))
        assertEquals(CanonicalJson.encode(b), CanonicalJson.encode(a))
    }

    @Test
    fun `list order is significant so reordering elements yields a different encoding`() {
        val list = listOf(50.0, 5.0, 45.0, 10.0)
        val nwse = mapOf("area" to list)
        val shuffled = mapOf("area" to list.reversed())
        assertNotEquals(CanonicalJson.encode(shuffled), CanonicalJson.encode(nwse))
    }

    @Test
    fun `list order is preserved verbatim, including zero-padded strings`() {
        assertEquals("""{"day":["02","01"]}""", CanonicalJson.encode(mapOf("day" to listOf("02", "01"))))
    }

    @Test
    fun `explicit nulls are encoded, distinguishing a present but null key from an absent one`() {
        assertNotEquals(CanonicalJson.encode(emptyMap<String, Any?>()), CanonicalJson.encode(mapOf("a" to null)))
    }

    @Test
    fun `a realistic GloFAS request is order-stable across its keys`() {
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
        assertEquals(CanonicalJson.encode(reordered), CanonicalJson.encode(request))
    }
}
