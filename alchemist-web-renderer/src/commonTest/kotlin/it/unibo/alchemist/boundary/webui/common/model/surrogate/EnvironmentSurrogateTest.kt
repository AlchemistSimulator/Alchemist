/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import it.unibo.alchemist.boundary.webui.common.model.serialization.decodeEnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.serialization.encodeEnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.serializer

@OptIn(ExperimentalSerializationApi::class)
class EnvironmentSurrogateTest {

    private val position = Position2DSurrogate(5.6, 8.42)

    private val nodesList = listOf(
        NodeSurrogate(
            id = 0,
            contents = mapOf(
                MoleculeSurrogate("test-0") to EmptyConcentrationSurrogate,
                MoleculeSurrogate("test-1") to EmptyConcentrationSurrogate,
            ),
            position = position,
        ),
        NodeSurrogate(
            id = 1,
            contents = mapOf(
                MoleculeSurrogate("test-2") to EmptyConcentrationSurrogate,
            ),
            position = position,
        ),
    )

    private val envSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> =
        EnvironmentSurrogate(dimensions = 2, nodes = nodesList)

    @Test
    fun `dimensions should be correct`() {
        assertEquals(2, envSurrogate.dimensions, "EnvironmentSurrogate.dimensions should match")
    }

    @Test
    fun `nodes should be correct`() {
        assertEquals(2, envSurrogate.nodes.size, "EnvironmentSurrogate should contain two nodes")
        assertEquals(nodesList, envSurrogate.nodes, "EnvironmentSurrogate.nodes should match the provided list")
    }

    @Test
    fun `serialization and deserialization should round-trip successfully`() {
        // Check serializer name
        val descriptorName = EnvironmentSurrogate
            .serializer(Int.serializer(), PositionSurrogate.serializer())
            .descriptor
            .serialName
        assertEquals("Environment", descriptorName, "Serializer serialName should be 'Environment'")

        // Round-trip
        val serialized = jsonFormat.encodeEnvironmentSurrogate(envSurrogate)
        val deserialized = jsonFormat.decodeEnvironmentSurrogate(serialized)
        assertEquals(envSurrogate, deserialized, "Deserialized surrogate should equal the original")
    }
}
