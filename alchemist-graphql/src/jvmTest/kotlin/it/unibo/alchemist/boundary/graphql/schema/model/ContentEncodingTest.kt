/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import it.unibo.alchemist.boundary.graphql.schema.util.encodeConcentrationContentToString
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

class ContentEncodingTest {

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    fun `concentration contents are encoded as Json when serializable, as strings otherwise`() {
        listOf(1.0, TestSerializableContent(1, "a"), TestNonSerializableContent(1, "a")).forEach { content ->
            checkConcentrationContent(content, encodeConcentrationContentToString(content))
        }
    }

    @Serializable
    private data class TestSerializableContent(val a: Int, val b: String)

    private data class TestNonSerializableContent(val a: Int, val b: String)

    companion object {

        private fun <T : Any> canSerialize(content: T): Boolean =
            runCatching { Json.Default.encodeToString(serializer(content::class.java), content) }.isSuccess

        fun <T : Any> checkConcentrationContent(c: T, cs: String) {
            if (canSerialize(c)) {
                checkJsonContent(c, cs)
            } else {
                checkGenericContent(c, cs)
            }
        }

        private fun <T : Any> checkGenericContent(c: T, cs: String) {
            assertEquals(encodeConcentrationContentToString(c), cs, "Expected generic content encoding to match")
        }

        private fun <T : Any> checkJsonContent(c: T, cs: String) {
            val jsonContent = Json.Default.encodeToString(serializer(c::class.java), c)
            assertEquals(jsonContent, cs, "Expected JSON content encoding to match")
            val content = Json.Default.decodeFromString(serializer(c::class.java), cs)
            assertEquals(content, c, "Expected decoded content to match original")
        }
    }
}
