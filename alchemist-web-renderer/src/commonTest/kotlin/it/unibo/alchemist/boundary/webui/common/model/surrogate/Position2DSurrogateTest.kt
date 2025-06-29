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
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
class Position2DSurrogateTest {

    private val positionSurrogate: PositionSurrogate = Position2DSurrogate(5.012345, 1.0000000001)

    @Test
    fun `position 2d surrogates should have the correct number of coordinates`() {
        assertEquals(
            2,
            positionSurrogate.coordinates.size,
            "Expected exactly two coordinates",
        )
    }

    @Test
    fun `position 2d surrogates should have the correct number of dimensions`() {
        assertEquals(
            2,
            positionSurrogate.dimensions,
            "Expected dimensions property to be 2",
        )
    }

    @Test
    fun `position 2d surrogates should have the correct coordinates`() {
        val coords = positionSurrogate.coordinates
        assertEquals(5.012345, coords[0], "First coordinate mismatch")
        assertEquals(1.0000000001, coords[1], "Second coordinate mismatch")
    }

    @Test
    fun `position 2d surrogates hashCode and equals should work as expected`() {
        val pos1 = Position2DSurrogate(1.0, 2.8)
        val pos2 = Position2DSurrogate(1.0, 2.8)
        val pos3 = Position2DSurrogate(2.0, 2.8)
        // hashCode
        assertEquals(pos1.hashCode(), pos2.hashCode(), "Equal objects must have same hashCode")
        assertNotEquals(pos1.hashCode(), pos3.hashCode(), "Different objects should have different hashCodes")
        assertNotEquals(pos2.hashCode(), pos3.hashCode(), "Different objects should have different hashCodes")
        // equals
        assertTrue(pos1 == pos2, "pos1 should equal pos2")
        assertTrue(pos1 != pos3, "pos1 should not equal pos3")
        assertTrue(pos2 != pos3, "pos2 should not equal pos3")
    }

    @Test
    fun `position 2d surrogates should be serialized and deserialized correctly`() {
        // Verify serialName
        val serialName = Position2DSurrogate.serializer().descriptor.serialName
        assertEquals("Position2D", serialName, "Serializer serialName should be 'Position2D'")
        // Round-trip serialization
        val serialized = Json.encodeToString(positionSurrogate)
        val deserialized = Json.decodeFromString<PositionSurrogate>(serialized)
        assertEquals(
            positionSurrogate,
            deserialized,
            "Deserialized Position2DSurrogate should equal the original",
        )
    }
}
