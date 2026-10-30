/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TestCopernicusRequest {

    private val endpoint = "https://ewds.climate.copernicus.eu/api"
    private val dataset = "cems-glofas-historical"

    // a realistic request to the EWDS data store
    private val glofas = CopernicusRequest(
        endpoint = endpoint,
        dataset = dataset,
        inputs = mapOf(
            "variable" to listOf("river_discharge_in_the_last_24_hours"),
            "hyear" to listOf("2024"),
            "hmonth" to listOf("06"),
            "hday" to listOf("10"),
            "data_format" to "netcdf",
        ),
    )

    private fun emptyRequest(endpoint: String) = CopernicusRequest(endpoint, "dataset-name", mapOf())

    @Test
    fun `directoryName is deterministic, the same request yields the same name`() {
        assertEquals(glofas.directoryName, glofas.directoryName)
    }

    @Test
    fun `directoryName is stable under key reordering`() {
        // same logical request, just reordered
        val reordered = CopernicusRequest(
            endpoint = endpoint,
            dataset = dataset,
            inputs = mapOf(
                "data_format" to "netcdf",
                "hday" to listOf("10"),
                "hmonth" to listOf("06"),
                "hyear" to listOf("2024"),
                "variable" to listOf("river_discharge_in_the_last_24_hours"),
            ),
        )
        assertEquals(glofas.directoryName, reordered.directoryName)
    }

    @Test
    fun `a different dataset yields a different name`() {
        assertNotEquals(glofas.directoryName, glofas.copy(dataset = "reanalysis-era5-single-levels").directoryName)
    }

    @Test
    fun `a different input value yields a different name (the bytes change)`() {
        val otherDay = glofas.copy(inputs = glofas.inputs + ("hday" to listOf("11")))
        assertNotEquals(glofas.directoryName, otherDay.directoryName)
    }

    @Test
    fun `reordering a list inside inputs yields a different name (list order is semantic)`() {
        // area corners reordered = a different request, must not collide
        val area = listOf(50.0, 5.0, 45.0, 10.0)
        val nwse = glofas.copy(inputs = glofas.inputs + ("area" to area))
        val swapped = glofas.copy(inputs = glofas.inputs + ("area" to area.reversed()))
        assertNotEquals(swapped.directoryName, nwse.directoryName)
    }

    @Test
    fun `the readable prefix is the dataset`() {
        assertStartsWith("cems-glofas-historical_", glofas.directoryName)
    }

    @Test
    fun `dataset ids that are not a safe path segment are rejected`() {
        listOf("", ".", "..", "../x", "weird/name with:chars", ".hidden", "-flag").forEach { dataset ->
            assertFailsWith<IllegalArgumentException>("'$dataset'") { glofas.copy(dataset = dataset) }
        }
    }

    @Test
    fun `the name complies with the CachedDirectoryKey contract, a single file-system-safe segment`() {
        assertMatches(Regex("^[A-Za-z0-9._-]+$"), glofas.directoryName)
    }

    @Test
    fun `the name is a readable prefix followed by a lowercase-hex hash suffix`() {
        assertMatches(Regex("^cems-glofas-historical_[0-9a-f]+$"), glofas.directoryName)
    }

    @Test
    fun `should not throw on actual endpoints`() {
        val endpoints = setOf(
            "https://cds.climate.copernicus.eu/api",
            "https://ewds.climate.copernicus.eu/api",
            "https://ads.atmosphere.copernicus.eu/api",
        )
        endpoints.forEach { api ->
            emptyRequest(api)
        }
        emptyRequest("https://something.else.copernicus.eu/api")
        emptyRequest("http://127.0.0.1:8080")
        emptyRequest("https://127.0.0.1/any/path")
    }

    @Test
    fun `should throw on illegal endpoints`() {
        assertFailsWith<IllegalArgumentException> { emptyRequest("") }
        assertFailsWith<IllegalArgumentException> { emptyRequest("https://www.google.com") }
        assertFailsWith<IllegalArgumentException> { emptyRequest("https://cds.climate.eu/api") }
    }

    @Test
    fun `endpoints that only look like Copernicus data stores are rejected`() {
        listOf(
            "https://evil.example/.copernicus.eu/api",
            "https://evil.example/?x=.copernicus.eu/api",
            "https://evilcopernicus.eu/api",
            "http://cds.climate.copernicus.eu/api",
            "https://cds.climate.copernicus.eu/other",
            "https://cds.climate.copernicus.eu/api?x=1",
            "https://cds.climate.copernicus.eu/api#x",
            "https://user@cds.climate.copernicus.eu/api",
            "http://127.0.0.1:8080/api?x=1",
        ).forEach { endpoint ->
            assertFailsWith<IllegalArgumentException>("'$endpoint'") { emptyRequest(endpoint) }
        }
    }
}

private fun assertStartsWith(prefix: String, actual: String) {
    assertTrue(actual.startsWith(prefix), "Expected '$actual' to start with '$prefix'")
}

private fun assertMatches(regex: Regex, actual: String) {
    assertTrue(regex.matches(actual), "Expected '$actual' to match $regex")
}
