/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.view.viewport.applyInfiniteZoomFactor
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class ViewportCameraMathTest {
    @Test
    fun `zoom is not clamped while finite`() {
        val zoomIn = applyInfiniteZoomFactor(10f, 1.12f)
        val zoomOut = applyInfiniteZoomFactor(0.0112f, 1f / 1.12f)
        assertTrue(abs(zoomIn - 11.2f) < 1e-4f)
        assertTrue(abs(zoomOut - 0.01f) < 1e-4f)
    }

    @Test
    fun `zoom saturates at float bounds instead of becoming invalid`() {
        val maxedZoom = applyInfiniteZoomFactor(Float.MAX_VALUE, 1.12f)
        val minedZoom = applyInfiniteZoomFactor(Float.MIN_VALUE, 1f / 1.12f)
        assertTrue(maxedZoom.isFinite())
        assertTrue(maxedZoom >= Float.MAX_VALUE / 2)
        assertTrue(minedZoom > 0f)
        assertTrue(minedZoom <= 1e-30f)
    }
}
