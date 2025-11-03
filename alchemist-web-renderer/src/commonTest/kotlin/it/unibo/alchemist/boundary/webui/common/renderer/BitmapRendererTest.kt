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
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.MoleculeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.NodeSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BitmapRendererTest {

    private val envSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> =
        EnvironmentSurrogate(
            dimensions = 2,
            nodes = listOf(
                NodeSurrogate(
                    id = 0,
                    contents = mapOf(MoleculeSurrogate("concentration") to EmptyConcentrationSurrogate),
                    position = Position2DSurrogate(5.6, 8.42),
                ),
            ),
        )

    private val renderer: Renderer<Any, PositionSurrogate, Bitmap> = BitmapRenderer()

    @Test
    fun `bitmap renderer should output a Bitmap32 correctly`() {
        val bmp = renderer.render(envSurrogate)
        assertTrue(bmp is Bitmap32, "Expected a Bitmap32 output")
        val encoded = jsonFormat.encodeToString(Bitmap32Serializer, bmp.toBMP32IfRequired())
        val decoded = jsonFormat.decodeFromString(Bitmap32Serializer, encoded)
        assertEquals(bmp.height, decoded.height, "Height should match after serialization round-trip")
        assertEquals(bmp.width, decoded.width, "Width should match after serialization round-trip")
        assertEquals(bmp.ints.toList(), decoded.ints.toList(), "Pixel data should match after round-trip")
    }

    @Test
    fun `bitmap renderer should throw on non-2D environments`() {
        val mockEnv = EnvironmentSurrogate<Any, PositionSurrogate>(
            dimensions = -1,
            nodes = emptyList(),
        )
        assertFailsWith<IllegalArgumentException> {
            renderer.render(mockEnv)
        }
    }
}
