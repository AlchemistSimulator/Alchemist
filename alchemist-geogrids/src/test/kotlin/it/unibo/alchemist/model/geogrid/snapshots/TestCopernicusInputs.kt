/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.snapshots

import it.unibo.alchemist.model.geogrid.snapshots.CopernicusData.Companion.toRequestInputs
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCopernicusInputs {

    private val tempDir: Path = Files.createTempDirectory("copernicus-inputs-test")

    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    private fun inputsFile(content: String): String = Files.createTempFile(tempDir, "inputs", ".json").apply {
        Files.writeString(this, content)
    }.toString()

    private fun assertFailsMentioning(expected: String, inputs: Any) {
        val error = assertFailsWith<IllegalArgumentException> { inputs.toRequestInputs() }
        assertContains(error.message.orEmpty(), expected)
    }

    @Test
    fun `reads a JSON string`() {
        assertEquals(
            mapOf("variable" to listOf("2m_temperature"), "year" to listOf("2024")),
            """{ "variable": ["2m_temperature"], "year": ["2024"] }""".toRequestInputs(),
        )
    }

    @Test
    fun `reads a JSON file`() {
        assertEquals(
            mapOf("type" to listOf("validated_reanalysis")),
            inputsFile("""{ "type": ["validated_reanalysis"] }""").toRequestInputs(),
        )
    }

    @Test
    fun `reads a JSON file among the classpath resources`() {
        assertEquals(
            mapOf("variable" to listOf("2m_temperature"), "year" to listOf("2024")),
            "copernicus-inputs/request.json".toRequestInputs(),
        )
    }

    @Test
    fun `reads a mapping as the equivalent JSON string`() {
        val json = """{ "area": [44, 11.5], "data_format": "netcdf", "nested": { "flag": true } }"""
        val mapping = mapOf("area" to listOf(44, 11.5), "data_format" to "netcdf", "nested" to mapOf("flag" to true))
        assertEquals(json.toRequestInputs(), mapping.toRequestInputs())
    }

    @Test
    fun `preserves list order`() {
        assertEquals(
            mapOf("area" to listOf(44.0, 11.0, 45.0, 12.0)),
            """{ "area": [44.0, 11.0, 45.0, 12.0] }""".toRequestInputs(),
        )
    }

    @Test
    fun `fails clearly on a string that is neither a file nor JSON`() {
        assertFailsMentioning("neither an existing file nor a valid JSON string", "missing/request.json")
        assertFailsMentioning("neither an existing file nor a valid JSON string", """{ "unterminated": [ }""")
        assertFailsMentioning("neither an existing file nor a valid JSON string", "")
        // a directory among the classpath resources is not a file
        assertFailsMentioning("neither an existing file nor a valid JSON string", "copernicus-inputs")
    }

    @Test
    fun `fails clearly on a file that does not hold JSON`() {
        assertFailsMentioning("does not hold valid JSON", inputsFile("variable: 2m_temperature"))
    }

    @Test
    fun `fails clearly on valid JSON that is not an object`() {
        assertFailsMentioning("not a JSON object", inputsFile("[1, 2, 3]"))
        assertFailsMentioning("not a JSON object", "\"cmon, this is just a string!\"")
    }

    @Test
    fun `fails clearly on a mapping with no JSON equivalent`() {
        assertFailsMentioning("is not a string", mapOf("area" to mapOf(1 to 2)))
        assertFailsMentioning("inputs.hday[0]", mapOf("hday" to listOf(LocalDate.of(2024, 6, 10))))
    }

    @Test
    fun `fails clearly on inputs of another type`() {
        assertFailsMentioning("must be a ", 42)
    }
}
