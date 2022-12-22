/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class Position2DSurrogateTest : StringSpec({

    val positionSurrogate: PositionSurrogate = Position2DSurrogate(5.012345, 1.0000000001)

    "Position2DSurrogate should have the correct number of coordinates" {
        positionSurrogate.coordinates.size shouldBe 2
    }

    "Position2DSurrogate should have the correct number of dimensions" {
        positionSurrogate.dimensions shouldBe 2
    }

    "Position2DSurrogate should have the correct coordinates" {
        positionSurrogate.coordinates[0] shouldBe 5.012345
        positionSurrogate.coordinates[1] shouldBe 1.0000000001
    }

    "Position2DSurrogate hashCode and equals should work as expected" {
        val pos1 = Position2DSurrogate(1.0, 2.8)
        val pos2 = Position2DSurrogate(1.0, 2.8)
        val pos3 = Position2DSurrogate(2.0, 2.8)
        pos1.hashCode() shouldBe pos2.hashCode()
        pos1.hashCode() shouldNotBe pos3.hashCode()
        pos2.hashCode() shouldNotBe pos3.hashCode()
        (pos1 == pos2) shouldBe true
        (pos1 == pos3) shouldBe false
        (pos2 == pos3) shouldBe false
    }

    "Position2DSurrogate should be serialized correctly" {
        Position2DSurrogate.serializer().descriptor.serialName shouldBe "Position2D"
        val serialized = Json.encodeToString(positionSurrogate)
        val deserializedPolymorphic = Json.decodeFromString<PositionSurrogate>(serialized)
        deserializedPolymorphic shouldBe positionSurrogate
    }
})
