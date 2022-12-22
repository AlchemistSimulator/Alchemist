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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class NodeSurrogateTest : StringSpec({

    val mapping = mapOf(MoleculeSurrogate("test-0") to 0, MoleculeSurrogate("test-1") to 1)

    val nodePosition = Position2DSurrogate(5.6, 1.2)

    val nodeSurrogate = NodeSurrogate(29, mapping, nodePosition)

    "NodeSurrogate should have the correct id" {
        nodeSurrogate.id shouldBe 29
    }

    "NodeSurrogate should have contents" {
        nodeSurrogate.contents[MoleculeSurrogate("test-0")] shouldBe 0
        nodeSurrogate.contents[MoleculeSurrogate("test-1")] shouldBe 1
        nodeSurrogate.contents.size shouldBe 2
    }

    "NodeSurrogate should have a position" {
        nodeSurrogate.position shouldBe nodePosition
    }

    "NodeSurrogate should be serialized correctly" {
        NodeSurrogate.serializer(
            Int.serializer(),
            PositionSurrogate.serializer()
        ).descriptor.serialName shouldBe "Node"
        val serialized = Json.encodeToString(nodeSurrogate)
        val deserialized: NodeSurrogate<Int, Position2DSurrogate> = Json.decodeFromString(serialized)
        deserialized shouldBe nodeSurrogate
    }
})
