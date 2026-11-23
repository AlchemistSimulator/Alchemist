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
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.min

/**
 * What the pointer is dragging in the viewport.
 */
@Immutable
internal sealed interface ViewportDrag {
    /**
     * The drag after the pointer moves to [position].
     * Holding the secondary button pans; otherwise the drag follows the pointer.
     */
    fun movedTo(position: Offset, secondaryPressed: Boolean): ViewportDrag = when {
        secondaryPressed -> Pan(position)
        this is Select -> copy(current = position)
        this is MoveNodes -> copy(current = position)
        else -> None
    }

    /** Nothing is being dragged. */
    data object None : ViewportDrag

    /**
     * The camera is being panned.
     *
     * @property last the last pointer position.
     */
    data class Pan(val last: Offset) : ViewportDrag

    /**
     * A selection box is being drawn, or a node clicked.
     *
     * @property anchor where the drag started.
     * @property current where the pointer is now.
     * @property mode how the nodes change the selection.
     */
    data class Select(
        val anchor: Offset,
        val current: Offset,
        val mode: SelectionMode = SelectionMode.REPLACE,
    ) : ViewportDrag {
        /** How far the pointer moved. */
        val length: Float get() = (current - anchor).getDistance()

        /** The selection box. */
        val rect: Rect get() = Rect(
            left = min(anchor.x, current.x),
            top = min(anchor.y, current.y),
            right = max(anchor.x, current.x),
            bottom = max(anchor.y, current.y),
        )
    }

    /**
     * Selected nodes are being moved: the point grabbed stays under the pointer, even if the camera moves.
     *
     * @property anchor where the drag started, on screen.
     * @property anchorBase where the drag started, in base coordinates when it started.
     * @property current where the pointer is now, on screen.
     * @property nodeIds the nodes being moved.
     */
    data class MoveNodes(
        val anchor: Offset,
        val anchorBase: Offset,
        val current: Offset,
        val nodeIds: Set<Int>,
    ) : ViewportDrag {
        /** How far the pointer moved on screen. */
        val delta: Offset get() = current - anchor

        /** How far the nodes moved, in base coordinates, as seen through [camera]. */
        fun baseDelta(camera: ViewportCameraState, viewportSize: IntSize): Offset =
            camera.toBase(current, viewportSize) - anchorBase

        /** How far the nodes appear moved on screen, as seen through [camera]. */
        fun screenDelta(camera: ViewportCameraState, viewportSize: IntSize): Offset =
            baseDelta(camera, viewportSize) * camera.zoom
    }
}
