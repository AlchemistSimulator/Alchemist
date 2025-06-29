/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.renderer

import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.paint.ColorPaint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Bitmap32SerializerTest {

    @Test
    fun `bitmap32 should serialize and deserialize correctly`() {
        // Create a Bitmap32 and treat it as Bitmap
        val bmp: Bitmap = Bitmap32(1000, 1000, ColorPaint(0))
        // Ensure the original is indeed a Bitmap32
        assertTrue(bmp is Bitmap32, "Original bitmap should be a Bitmap32")

        // Serialize via the polymorphic Bitmap32Serializer
        val serialized = jsonFormat.encodeToString(Bitmap32Serializer, bmp.toBMP32IfRequired())
        // Deserialize back
        val deserialized: Bitmap32 = jsonFormat.decodeFromString(Bitmap32Serializer, serialized)
        // Now check that key properties match
        assertEquals(bmp.bounds, deserialized.bounds, "Bounds should match after round-trip")
        assertEquals(bmp.width, deserialized.width, "Width should match after round-trip")
        assertEquals(bmp.height, deserialized.height, "Height should match after round-trip")
        assertEquals(bmp.ints.toList(), deserialized.ints.toList(), "Pixel data should match after round-trip")
    }
}
