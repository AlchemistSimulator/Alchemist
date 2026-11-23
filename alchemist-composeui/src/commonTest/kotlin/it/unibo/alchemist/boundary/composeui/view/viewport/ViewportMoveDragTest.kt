/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class ViewportMoveDragTest {
    @Test
    fun `the grabbed point stays under the pointer when the camera pans and zooms`() {
        val camera = ViewportCameraState()
        val drag = ViewportDrag.MoveNodes(
            anchor = GRABBED,
            anchorBase = camera.toBase(GRABBED, SIZE),
            current = GRABBED,
            nodeIds = setOf(1),
        ).movedTo(POINTER, secondaryPressed = false) as ViewportDrag.MoveNodes
        listOf(camera, camera.panBy(Offset(30f, -12f)), camera.zoomIn(SIZE).panBy(Offset(-7f, 4f))).forEach { moved ->
            val drawnAt = moved.toScreen(drag.anchorBase, SIZE) + drag.screenDelta(moved, SIZE)
            assertTrue(
                abs(drawnAt.x - POINTER.x) < TOLERANCE && abs(drawnAt.y - POINTER.y) < TOLERANCE,
                "With $moved, the grabbed point is drawn at $drawnAt instead of under the pointer at $POINTER",
            )
        }
    }

    private companion object {
        const val TOLERANCE = 1e-3f
        val SIZE = IntSize(200, 100)
        val GRABBED = Offset(40f, 30f)
        val POINTER = Offset(120f, 60f)
    }
}
