/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewportProjectionTest {
    @Test
    fun `projection is unavailable without nodes or viewport size`() {
        assertNull(ViewportScene().createViewportProjection(IntSize(1280, 720)))
        assertNull(sampleScene().createViewportProjection(IntSize.Zero))
    }

    @Test
    fun `projection stays fixed when nodes move beyond initial bounds`() {
        val viewportSize = IntSize(1000, 500)
        val projection = assertNotNull(sampleScene().createViewportProjection(viewportSize))

        val movedNode = ViewportNode(id = 3, coordinates = listOf(20.0, 20.0))
        val movedPosition = movedNode.toViewportPosition(viewportSize, projection)

        assertTrue(movedPosition.x > viewportSize.width)
        assertTrue(movedPosition.y < 0f)
    }
}

private fun sampleScene(): ViewportScene = ViewportScene(
    nodes = listOf(
        ViewportNode(id = 1, coordinates = listOf(0.0, 0.0)),
        ViewportNode(id = 2, coordinates = listOf(10.0, 10.0)),
    ),
)
