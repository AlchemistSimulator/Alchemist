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
import androidx.compose.ui.input.key.Key

private const val PAN_STEP_PX = 48f

/**
 * A fixed camera move, triggered by the arrow keys or by accessibility actions.
 *
 * @property label the accessibility action name.
 * @property key the arrow key.
 * @property offset how far the camera pans.
 */
internal enum class PanDirection(val label: String, val key: Key, val offset: Offset) {
    LEFT("Pan left", Key.DirectionLeft, Offset(PAN_STEP_PX, 0f)),
    RIGHT("Pan right", Key.DirectionRight, Offset(-PAN_STEP_PX, 0f)),
    UP("Pan up", Key.DirectionUp, Offset(0f, PAN_STEP_PX)),
    DOWN("Pan down", Key.DirectionDown, Offset(0f, -PAN_STEP_PX)),
    ;

    companion object {
        /** The direction bound to [key], if any. */
        fun of(key: Key): PanDirection? = entries.firstOrNull { it.key == key }
    }
}
