/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import it.unibo.alchemist.boundary.composeui.view.theme.GRID_HORIZONTAL_DIVISIONS
import it.unibo.alchemist.boundary.composeui.view.theme.GRID_VERTICAL_DIVISIONS
import kotlin.math.min

private const val MIN_GRID_STEP_PX = 10f
private const val MAX_GRID_STEP_PX = 100f

/**
 * Spacing of the background grid lines.
 * The base spacing divides the viewport evenly; zooming doubles or halves it to keep lines readable.
 *
 * @property basePixels the spacing with no zoom.
 * @property pixels the spacing on screen.
 * @property multiplier how many base steps one line step spans.
 */
internal data class GridSpacing(val basePixels: Float, val pixels: Float, val multiplier: Double) {
    /** The world distance between two lines. */
    fun worldStep(projection: ViewportProjection): Double = basePixels * multiplier / projection.pixelsPerUnit

    companion object {
        /** The spacing for a [width] by [height] viewport at [zoom]. */
        fun of(width: Float, height: Float, zoom: Float): GridSpacing {
            val basePixels = min(width / GRID_VERTICAL_DIVISIONS, height / GRID_HORIZONTAL_DIVISIONS)
            return fit(GridSpacing(basePixels, basePixels * zoom, 1.0))
        }

        private tailrec fun fit(spacing: GridSpacing): GridSpacing {
            val pixels = spacing.pixels
            val multiplier = spacing.multiplier
            return when {
                pixels <= 0f || !pixels.isFinite() -> spacing
                pixels < MIN_GRID_STEP_PX -> fit(spacing.copy(pixels = pixels * 2, multiplier = multiplier * 2))
                pixels > MAX_GRID_STEP_PX -> fit(spacing.copy(pixels = pixels / 2, multiplier = multiplier / 2))
                else -> spacing
            }
        }
    }
}
