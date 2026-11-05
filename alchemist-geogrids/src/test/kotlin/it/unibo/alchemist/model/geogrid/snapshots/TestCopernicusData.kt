/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.snapshots

import it.unibo.alchemist.boundary.copernicus.CopernicusRequest
import it.unibo.alchemist.boundary.copernicus.FakeDataStore
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.TOKEN_HEADER
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.zipOf
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeBytes
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestCopernicusData {

    private val tempDir: Path = createTempDirectory("copernicus-data-test")

    private val endpoint = "https://ewds.climate.copernicus.eu/api"

    private val dataset = "cems-glofas-historical"

    /**
     * The inputs of the cached request, with integers: if any form turned them into `44.0`, it would miss the cache.
     */
    private val inputs: Map<String, Any> = mapOf("variable" to listOf("river_discharge"), "area" to listOf(44, 12))

    /**
     * The [inputs] as JSON, also held by the `copernicus-inputs/request.json` resource.
     */
    private val inputsJson = """{ "variable": ["river_discharge"], "area": [44, 12] }"""

    /**
     * A `.cdsapirc` file that does not exist, so that missing the cache fails before any network access.
     */
    private val missingCdsApiRc: Path = tempDir.resolve("missing.cdsapirc")

    private val glofas: ByteArray = Files.readAllBytes(
        Path.of(requireNotNull(javaClass.getResource("/copernicus/glofas-cesena/glofas-cesena.nc")).toURI()),
    )

    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    // inputs in all their forms
    @Test
    fun `reads inputs written as a JSON string`() = assertFindsCachedData(inputsJson)

    @Test
    fun `reads inputs written as a mapping`() =
        assertFindsCachedData(mapOf("variable" to listOf("river_discharge"), "area" to listOf(44, 12)))

    @Test
    fun `reads inputs from a JSON file on the file system`() =
        assertFindsCachedData(tempDir.resolve("inputs.json").apply { writeText(inputsJson) }.toString())

    @Test
    fun `reads inputs from a JSON file among the classpath resources`() =
        assertFindsCachedData("copernicus-inputs/request.json")

    @Test
    fun `fails clearly on a string that is neither a file nor JSON`() {
        listOf("missing/request.json", """{ "unterminated": [ }""", "", "copernicus-inputs").forEach {
            assertInvalidInputs("neither an existing file nor a valid JSON string", it)
        }
    }

    @Test
    fun `fails clearly on a file that does not hold JSON`() {
        mapOf("inputs.yml" to "variable: river_discharge", "empty.json" to "").forEach { (name, content) ->
            val file = tempDir.resolve(name).apply { writeText(content) }
            assertInvalidInputs("does not hold valid JSON", file.toString())
        }
    }

    @Test
    fun `fails clearly on valid JSON that is not an object`() {
        assertInvalidInputs("not a JSON object", tempDir.resolve("array.json").apply { writeText("[1, 2]") }.toString())
        assertInvalidInputs("not a JSON object", "\"cmon, this is just a string!\"")
    }

    @Test
    fun `fails clearly on a mapping with no JSON equivalent`() {
        assertInvalidInputs("is not a string", mapOf("area" to mapOf(1 to 2)))
        // e.g., an unquoted date in YAML
        assertInvalidInputs("inputs.hday[0]", mapOf("hday" to listOf(LocalDate.of(2024, 6, 10))))
    }

    @Test
    fun `fails clearly on inputs of another type`() =
        assertInvalidInputs("must be a Map, a JSON file, or a JSON string", 42)

    // credentials
    @Test
    fun `downloads the data with the token of the cdsapirc file`() {
        FakeDataStore().use { fake ->
            fake.completingWith(zipOf("glofas-cesena.nc" to glofas))
            val cdsApiRc = tempDir.resolve(".cdsapirc").apply {
                writeText(
                    """
                    # key: not-this-one
                    url: https://cds.climate.copernicus.eu/api
                    key :  secret:with-colons
                    """.trimIndent(),
                )
            }
            val data = fake.copernicusData(cdsApiRc)
            assertTrue(data.instants.isNotEmpty())
            val toDataStore = fake.server.requests.filter { it.route != fake.store.assetPath }
            assertTrue(toDataStore.isNotEmpty())
            toDataStore.forEach { assertEquals("secret:with-colons", it.header(TOKEN_HEADER)) }
        }
    }

    @Test
    fun `fails clearly when the cdsapirc file is missing`() {
        FakeDataStore().use { fake ->
            val failure = assertFailsWith<IllegalStateException> { fake.copernicusData(missingCdsApiRc) }
            assertContains(failure.message.orEmpty(), "No .cdsapirc file at $missingCdsApiRc")
            assertTrue(fake.server.requests.isEmpty(), "No request can be sent without a token")
        }
    }

    @Test
    fun `fails clearly when the cdsapirc file has no key, or an empty one`() {
        listOf("", "\nkey:", "\nkey:   ").forEach { keyLine ->
            FakeDataStore().use { fake ->
                val cdsApiRc = tempDir.resolve(".cdsapirc")
                cdsApiRc.writeText("url: https://cds.climate.copernicus.eu/api$keyLine")
                val failure = assertFails { fake.copernicusData(cdsApiRc) }
                assertTrue(
                    generateSequence(failure) { it.cause }.any { "No non-empty 'key:' line" in it.message.orEmpty() },
                    "Expected a missing key to be reported for '$keyLine', but got $failure",
                )
                assertTrue(fake.server.requests.isEmpty(), "No request can be sent without a token")
            }
        }
    }

    /**
     * Checks that [inputs], written in any form, find the data cached for [inputs].
     */
    private fun assertFindsCachedData(inputs: Any) {
        val cache = tempDir.resolve("cache")
        cache.resolve(CopernicusRequest(endpoint, dataset, this.inputs).directoryName)
            .createDirectories()
            .resolve("glofas-cesena.nc")
            .writeBytes(glofas)
        val data = CopernicusData(
            endpoint,
            dataset,
            inputs,
            cacheDirectory = cache.toString(),
            cdsApiRcFile = missingCdsApiRc.toString(),
        )
        assertTrue(data.instants.isNotEmpty())
    }

    private fun assertInvalidInputs(expected: String, inputs: Any) {
        val failure = assertFailsWith<IllegalArgumentException> {
            CopernicusData(endpoint, dataset, inputs, cdsApiRcFile = missingCdsApiRc.toString())
        }
        assertContains(failure.message.orEmpty(), expected)
    }

    /**
     * Data from this fake data store, downloaded in a new cache with the token in [cdsApiRc].
     */
    private fun FakeDataStore.copernicusData(cdsApiRc: Path) = CopernicusData(
        endpoint,
        store.dataset,
        "{}",
        cacheDirectory = tempDir.resolve("cache").toString(),
        cdsApiRcFile = cdsApiRc.toString(),
    )
}
