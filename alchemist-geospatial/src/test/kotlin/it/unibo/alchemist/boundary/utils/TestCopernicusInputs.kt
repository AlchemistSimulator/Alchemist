/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import com.google.gson.JsonSyntaxException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCopernicusInputs {

    private val tempDir: Path = Files.createTempDirectory("copernicus-inputs-test")

    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    private fun inputsFile(content: String): Path = Files.createTempFile(tempDir, "inputs", ".json").apply {
        Files.writeString(this, content)
    }

    @Test
    fun `reads a flat JSON object into a map of lists`() {
        val path = inputsFile("""{ "variable": ["2m_temperature"], "year": ["2024"] }""".trimIndent())
        assertEquals(
            mapOf(
                "variable" to listOf("2m_temperature"),
                "year" to listOf("2024"),
            ),
            CopernicusInputs.read(path),
        )
    }

    @Test
    fun `reads a 'type' key without any loader involved`() {
        val path = inputsFile("""{ "type": ["validated_reanalysis"] }""".trimIndent())
        assertEquals(mapOf("type" to listOf("validated_reanalysis")), CopernicusInputs.read(path))
    }

    @Test
    fun `preserves list order`() {
        val path = inputsFile("""{ "area": [44.0, 11.0, 45.0, 12.0] }""".trimIndent())
        assertEquals(mapOf("area" to listOf(44.0, 11.0, 45.0, 12.0)), CopernicusInputs.read(path))
    }

    @Test
    fun `throws JsonSyntaxException when the file holds a JSON array instead of an object`() {
        val path = inputsFile("[1, 2, 3]")
        assertFailsWith<JsonSyntaxException> { CopernicusInputs.read(path) }
    }

    @Test
    fun `throws JsonSyntaxException when the file holds a scalar`() {
        val path = inputsFile("\"cmon, this is just a string!\"")
        assertFailsWith<JsonSyntaxException> { CopernicusInputs.read(path) }
    }
}
