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
 * A camera pan direction, held with the arrow keys or WASD, or stepped by accessibility actions.
 *
 * @property label the accessibility action name.
 * @property keys the keys that hold it.
 * @property direction which way the camera pans, as a unit vector.
 */
internal enum class PanDirection(val label: String, val keys: Set<Key>, val direction: Offset) {
    LEFT("Pan left", setOf(Key.DirectionLeft, Key.A), Offset(1f, 0f)),
    RIGHT("Pan right", setOf(Key.DirectionRight, Key.D), Offset(-1f, 0f)),
    UP("Pan up", setOf(Key.DirectionUp, Key.W), Offset(0f, 1f)),
    DOWN("Pan down", setOf(Key.DirectionDown, Key.S), Offset(0f, -1f)),
    ;

    /** One accessibility step in this direction. */
    val step: Offset get() = direction * PAN_STEP_PX

    companion object {
        /** The direction bound to [key], if any. */
        fun of(key: Key): PanDirection? = entries.firstOrNull { key in it.keys }
    }
}
