/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class GeneralPositionSurrogateTest : StringSpec({

    val generalPositionSurrogate: PositionSurrogate = GeneralPositionSurrogate(
        doubleArrayOf(5.0, 1.1, 6.0),
        3,
    )

    "GeneralPositionSurrogate should have the correct number of coordinates" {
        generalPositionSurrogate.coordinates.size shouldBe 3
    }

    "GeneralPositionSurrogate should have the correct number of dimensions" {
        generalPositionSurrogate.dimensions shouldBe 3
    }

    "GeneralPositionSurrogate should have the correct coordinates" {
        generalPositionSurrogate.coordinates[0] shouldBe 5.0
        generalPositionSurrogate.coordinates[1] shouldBe 1.1
        generalPositionSurrogate.coordinates[2] shouldBe 6.0
    }

    "GeneralPositionSurrogate should fail on creation if coordinates size is different from dimensions" {
        shouldThrow<IllegalArgumentException> {
            GeneralPositionSurrogate(doubleArrayOf(5.0, 1.1), 3)
        }
    }

    "GeneralPositionSurrogate hashCode and equals should work as expected" {
        val pos1 = GeneralPositionSurrogate(doubleArrayOf(1.0, 2.8, 3.0), 3)
        val pos2 = GeneralPositionSurrogate(doubleArrayOf(1.0, 2.8, 3.0), 3)
        val pos3 = GeneralPositionSurrogate(doubleArrayOf(1.0), 1)
        pos1.hashCode() shouldBe pos2.hashCode()
        pos1.hashCode() shouldNotBe pos3.hashCode()
        pos2.hashCode() shouldNotBe pos3.hashCode()
        (pos1 == pos2) shouldBe true
        (pos1 == pos3) shouldBe false
        (pos2 == pos3) shouldBe false
    }

    "GeneralPositionSurrogate should be serialized and deserialized correctly" {
        GeneralPositionSurrogate.serializer().descriptor.serialName shouldBe "Position"
        val serialized = Json.encodeToString(generalPositionSurrogate)
        val deserializedPolymorphic = Json.decodeFromString<PositionSurrogate>(serialized)
        deserializedPolymorphic shouldBe generalPositionSurrogate
    }
})
