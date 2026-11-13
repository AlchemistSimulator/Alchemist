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
     * A selection box is being drawn.
     *
     * @property anchor where the drag started.
     * @property current where the pointer is now.
     */
    data class Select(val anchor: Offset, val current: Offset) : ViewportDrag {
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
     * Selected nodes are being moved.
     *
     * @property anchor where the drag started.
     * @property current where the pointer is now.
     * @property nodeIds the nodes being moved.
     */
    data class MoveNodes(val anchor: Offset, val current: Offset, val nodeIds: Set<Int>) : ViewportDrag {
        /** How far the nodes moved on screen. */
        val delta: Offset get() = current - anchor
    }
}
