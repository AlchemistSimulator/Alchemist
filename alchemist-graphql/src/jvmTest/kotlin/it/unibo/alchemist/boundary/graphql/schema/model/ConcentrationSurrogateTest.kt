/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLConcentrationSurrogate
import it.unibo.alchemist.boundary.graphql.schema.util.encodeConcentrationContentToString
import it.unibo.alchemist.model.Concentration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class ConcentrationSurrogateTest : StringSpec({
    "ConcentrationSurrogate should map a Concentration to a GraphQL compliant object" {
        // Basic concentration content test
        val c1: Concentration<Double> = Concentration { 1.0 }
        checkConcentrationContent(c1.content, c1.toGraphQLConcentrationSurrogate().content)
        // Test with a serializable content
        val c2: Concentration<TestSerializableContent> = Concentration { TestSerializableContent(1, "a") }
        checkConcentrationContent(c2.content, c2.toGraphQLConcentrationSurrogate().content)
        // Test with a non-serializable content
        val c3: Concentration<TestNonSerializableContent> = Concentration { TestNonSerializableContent(1, "a") }
        checkConcentrationContent(c3.content, c3.toGraphQLConcentrationSurrogate().content)
    }
}) {
    companion object {
        @Serializable
        private data class TestSerializableContent(val a: Int, val b: String)

        private data class TestNonSerializableContent(val a: Int, val b: String)

        fun <T : Any> checkConcentrationContent(c: T, cs: String) {
            if (canSerialize(c)) {
                checkJsonContent(c, cs)
            } else {
                checkGenericContent(c, cs)
            }
        }

        fun <T : Any> checkGenericContent(c: T, cs: String) {
            encodeConcentrationContentToString(c) shouldBe cs
        }

        fun <T : Any> checkJsonContent(c: T, cs: String) {
            val jsonContent = Json.Default.encodeToString(serializer(c::class.java), c)
            jsonContent shouldBe cs
            val content = Json.Default.decodeFromString(serializer(c::class.java), cs)
            content shouldBe c
        }

        private fun <T : Any> canSerialize(content: T): Boolean =
            runCatching { Json.Default.encodeToString(serializer(content::class.java), content) }.isSuccess
    }
}
