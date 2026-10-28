/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
    fun `the readable prefix comes from the dataset, sanitized`() {
        assertStartsWith("cems-glofas-historical_", glofas.directoryName)
    }

    @Test
    fun `a dataset id with unsafe characters is sanitized in the prefix`() {
        val weird = glofas.copy(dataset = "weird/name with:chars")
        assertStartsWith("weird_name_with_chars_", weird.directoryName)
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
        emptyRequest("https://SOMETHING.SOMETHING_ELSE.copernicus.eu/api")
    }

    @Test
    fun `should throw on illegal endpoints`() {
        assertFailsWith<IllegalArgumentException> { emptyRequest("") }
        assertFailsWith<IllegalArgumentException> { emptyRequest("https://www.google.com") }
        assertFailsWith<IllegalArgumentException> { emptyRequest("https://cds.climate.eu/api") }
    }

    // toFileSystemSafe extension function tests
    @Test
    fun `a plain name with only safe characters is left unchanged`() {
        assertEquals(dataset, dataset.toFileSystemSafe())
        assertEquals("ERA5_2024.v3", "ERA5_2024.v3".toFileSystemSafe())
    }

    @Test
    fun `spaces are replaced`() {
        assertEquals("New_York", "New York".toFileSystemSafe())
    }

    @Test
    fun `path separators are replaced`() {
        assertEquals("a_b", "a/b".toFileSystemSafe())
        assertEquals("a_b", "a\\b".toFileSystemSafe())
    }

    @Test
    fun `Windows-illegal characters are replaced`() {
        // < > : " | ? * are illegal in a Windows filename
        assertEquals("a_b_c_d_e_f_g_h", """a:b<c>d"e|f?g*h""".toFileSystemSafe())
    }

    @Test
    fun `non-ASCII letters are replaced`() {
        assertEquals("Forl_-Cesena", "Forlì-Cesena".toFileSystemSafe())
    }

    @Test
    fun `output never contains a path separator, for any input`() {
        listOf("a/b", "a\\b", "/", "\\", "C:/x", "../../etc").forEach { raw ->
            val safe = raw.toFileSystemSafe()
            assertFalse("/" in safe, "Expected '$safe' (from '$raw') not to contain '/'")
            assertFalse("\\" in safe, "Expected '$safe' (from '$raw') not to contain '\\'")
        }
    }

    @Test
    fun `output contains only allowlisted characters`() {
        assertMatches(Regex("^[A-Za-z0-9._-]*$"), """!"£$%&/()=?^@#°""".toFileSystemSafe())
    }

    @Test
    fun `sanitizing a sanitized name changes nothing`() {
        val once = "a b/c:d".toFileSystemSafe()
        assertEquals(once, once.toFileSystemSafe())
    }
}

private fun assertStartsWith(prefix: String, actual: String) {
    assertTrue(actual.startsWith(prefix), "Expected '$actual' to start with '$prefix'")
}

private fun assertMatches(regex: Regex, actual: String) {
    assertTrue(regex.matches(actual), "Expected '$actual' to match $regex")
}
