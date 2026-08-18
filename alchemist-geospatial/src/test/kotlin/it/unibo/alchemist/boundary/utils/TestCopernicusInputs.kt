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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

class TestCopernicusInputs : StringSpec({

    val tempDir: Path = Files.createTempDirectory("copernicus-inputs-test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    fun inputsFile(content: String): Path = Files.createTempFile(tempDir, "inputs", ".json").apply {
        Files.writeString(this, content)
    }

    "reads a flat JSON object into a map of lists" {
        val path = inputsFile("""{ "variable": ["2m_temperature"], "year": ["2024"] }""".trimIndent())
        CopernicusInputs.read(path) shouldBe mapOf(
            "variable" to listOf("2m_temperature"),
            "year" to listOf("2024"),
        )
    }

    "reads a 'type' key without any loader involved" {
        val path = inputsFile("""{ "type": ["validated_reanalysis"] }""".trimIndent())
        CopernicusInputs.read(path) shouldBe mapOf("type" to listOf("validated_reanalysis"))
    }

    "preserves list order" {
        val path = inputsFile("""{ "area": [44.0, 11.0, 45.0, 12.0] }""".trimIndent())
        CopernicusInputs.read(path) shouldBe mapOf("area" to listOf(44.0, 11.0, 45.0, 12.0))
    }

    "throws JsonSyntaxException when the file holds a JSON array instead of an object" {
        val path = inputsFile("[1, 2, 3]")
        shouldThrow<JsonSyntaxException> { CopernicusInputs.read(path) }
    }

    "throws JsonSyntaxException when the file holds a scalar" {
        val path = inputsFile("\"cmon, this is just a string!\"")
        shouldThrow<JsonSyntaxException> { CopernicusInputs.read(path) }
    }
})
