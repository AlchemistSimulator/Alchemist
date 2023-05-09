/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.serialization.decodeEnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.serialization.encodeEnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalSerializationApi::class)
class EnvironmentSurrogateTest : StringSpec({

    val position = Position2DSurrogate(5.6, 8.42)

    val nodesListSet = listOf(
        NodeSurrogate(
            0,
            mapOf(
                MoleculeSurrogate("test-0") to EmptyConcentrationSurrogate,
                MoleculeSurrogate("test-1") to EmptyConcentrationSurrogate,
            ),
            position,
        ),
        NodeSurrogate(
            1,
            mapOf(MoleculeSurrogate("test-2") to EmptyConcentrationSurrogate),
            position,
        ),
    )

    val envSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> = EnvironmentSurrogate(2, nodesListSet)

    "EnvironmentSurrogate should have the correct dimensions" {
        envSurrogate.dimensions shouldBe 2
    }

    "EnvironmentSurrogate should have the correct nodes" {
        envSurrogate.nodes.size shouldBe 2
        envSurrogate.nodes shouldBe nodesListSet
    }

    "EnvironmentSurrogate serialization and deserialization should work" {
        EnvironmentSurrogate.serializer(
            Int.serializer(),
            PositionSurrogate.serializer(),
        ).descriptor.serialName shouldBe "Environment"
        val serialized = jsonFormat.encodeEnvironmentSurrogate(envSurrogate)
        val deserializedPolymorphic = jsonFormat.decodeEnvironmentSurrogate(serialized)
        deserializedPolymorphic shouldBe envSurrogate
    }
})
