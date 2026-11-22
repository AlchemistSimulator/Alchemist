/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import it.unibo.alchemist.boundary.composeui.view.theme.ZOOM_STEP

/**
 * How the viewport is moved and scaled.
 *
 * "Base" positions are screen positions with no pan and no zoom.
 *
 * @property pan the shift, in pixels.
 * @property zoom the scale factor.
 */
@Immutable
internal data class ViewportCameraState(val pan: Offset = Offset.Zero, val zoom: Float = 1f) {
    /** This camera moved by [delta]. */
    fun panBy(delta: Offset): ViewportCameraState = copy(pan = pan + delta)

    /** Base position of the screen [position]. */
    fun toBase(position: Offset, viewportSize: IntSize): Offset =
        NEUTRAL.toScreen(toWorld(position, viewportSize), viewportSize)

    /** Base rectangle covering the screen [rect]. */
    fun toBase(rect: Rect, viewportSize: IntSize): Rect {
        val topLeft = toBase(rect.topLeft, viewportSize)
        val bottomRight = toBase(rect.bottomRight, viewportSize)
        return Rect(
            left = minOf(topLeft.x, bottomRight.x),
            top = minOf(topLeft.y, bottomRight.y),
            right = maxOf(topLeft.x, bottomRight.x),
            bottom = maxOf(topLeft.y, bottomRight.y),
        )
    }

    /** Screen position of the base [position]. */
    fun toScreen(position: Offset, viewportSize: IntSize): Offset {
        val center = viewportSize.toSize().center
        return center + (position - center) * zoom + pan
    }

    /** Zooms in on the viewport center. */
    fun zoomIn(viewportSize: IntSize): ViewportCameraState = zoomBy(viewportSize, viewportSize.toSize().center, -1f)

    /** Zooms out from the viewport center. */
    fun zoomOut(viewportSize: IntSize): ViewportCameraState = zoomBy(viewportSize, viewportSize.toSize().center, 1f)

    /** Zooms around [pivot]: in for a negative [scrollDelta], out for a positive one. */
    fun zoomBy(viewportSize: IntSize, pivot: Offset, scrollDelta: Float): ViewportCameraState {
        val zoomFactor = when {
            scrollDelta < 0f -> ZOOM_STEP
            scrollDelta > 0f -> 1f / ZOOM_STEP
            else -> 1f
        }
        val targetZoom = applyInfiniteZoomFactor(zoom, zoomFactor)
        return when {
            viewportSize.width == 0 || viewportSize.height == 0 || targetZoom == zoom -> this
            else -> {
                val center = viewportSize.toSize().center
                val worldPoint = toWorld(pivot, viewportSize)
                copy(zoom = targetZoom, pan = pivot - center - (worldPoint - center) * targetZoom)
            }
        }
    }

    private fun toWorld(position: Offset, viewportSize: IntSize): Offset {
        val center = viewportSize.toSize().center
        return center + (position - center - pan) / zoom
    }

    companion object {
        private val NEUTRAL = ViewportCameraState()

        /** Multiplies [currentZoom] by [zoomFactor], staying positive and finite. */
        fun applyInfiniteZoomFactor(currentZoom: Float, zoomFactor: Float): Float {
            val validInputs = currentZoom > 0f && currentZoom.isFinite() && zoomFactor > 0f && zoomFactor.isFinite()
            val targetZoom = currentZoom * zoomFactor
            return when {
                !validInputs || targetZoom.isNaN() -> currentZoom
                targetZoom == 0f -> Float.MIN_VALUE
                targetZoom == Float.POSITIVE_INFINITY -> Float.MAX_VALUE
                else -> targetZoom
            }
        }
    }
}
