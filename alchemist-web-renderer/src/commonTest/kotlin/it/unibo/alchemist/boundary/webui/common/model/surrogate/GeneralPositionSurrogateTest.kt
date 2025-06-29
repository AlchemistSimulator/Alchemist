/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class GeneralPositionSurrogateTest {

    private val generalPositionSurrogate: PositionSurrogate = GeneralPositionSurrogate(
        doubleArrayOf(5.0, 1.1, 6.0),
        3,
    )

    @Test
    fun `GeneralPositionSurrogate should have the correct number of coordinates`() {
        assertEquals(3, generalPositionSurrogate.coordinates.size)
    }

    @Test
    fun `GeneralPositionSurrogate should have the correct number of dimensions`() {
        assertEquals(3, generalPositionSurrogate.dimensions)
    }

    @Test
    fun `GeneralPositionSurrogate should have the correct coordinates`() {
        val coords = generalPositionSurrogate.coordinates
        assertEquals(5.0, coords[0])
        assertEquals(1.1, coords[1])
        assertEquals(6.0, coords[2])
    }

    @Test
    fun `GeneralPositionSurrogate should fail on creation if coordinates size is different from dimensions`() {
        assertFailsWith<IllegalArgumentException> {
            GeneralPositionSurrogate(doubleArrayOf(5.0, 1.1), 3)
        }
    }

    @Test
    fun `GeneralPositionSurrogate hashCode and equals should work as expected`() {
        val pos1 = GeneralPositionSurrogate(doubleArrayOf(1.0, 2.8, 3.0), 3)
        val pos2 = GeneralPositionSurrogate(doubleArrayOf(1.0, 2.8, 3.0), 3)
        val pos3 = GeneralPositionSurrogate(doubleArrayOf(1.0), 1)
        // Equal objects
        assertEquals(pos1.hashCode(), pos2.hashCode())
        assertEquals(pos1, pos2)
        // Unequal objects
        assertNotEquals(pos1.hashCode(), pos3.hashCode())
        assertNotEquals(pos2.hashCode(), pos3.hashCode())
        assertNotEquals(pos1, pos3)
        assertNotEquals(pos2, pos3)
    }

    @Test
    fun `GeneralPositionSurrogate should be serialized and deserialized correctly`() {
        // Check serialName
        assertEquals(
            "Position",
            GeneralPositionSurrogate.serializer().descriptor.serialName,
        )
        // Round-trip
        val serialized = Json.encodeToString(generalPositionSurrogate)
        val deserializedPolymorphic = Json.decodeFromString<PositionSurrogate>(serialized)
        assertEquals(generalPositionSurrogate, deserializedPolymorphic)
    }
}
