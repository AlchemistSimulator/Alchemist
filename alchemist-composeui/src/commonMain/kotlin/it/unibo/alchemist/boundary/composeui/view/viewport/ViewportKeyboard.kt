/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

private const val NANOS_PER_SECOND = 1e9f

/**
 * Keyboard controls: arrows or WASD pan while held, Enter or E select the node nearest to the pointer and open it,
 * Backspace clears the selection.
 * [onPanKey] gets each pan direction as it is pressed or released; the other callbacks return whether they
 * handled the key.
 */
internal fun Modifier.viewportKeyboard(
    onPanKey: (direction: PanDirection, pressed: Boolean) -> Unit,
    onSelectNearestNode: () -> Boolean,
    onClearSelection: () -> Boolean,
): Modifier = onPreviewKeyEvent { event ->
    val panDirection = PanDirection.of(event.key)
    when {
        panDirection != null -> {
            onPanKey(panDirection, event.type == KeyEventType.KeyDown)
            true
        }
        event.type != KeyEventType.KeyDown -> false
        event.key == Key.Enter || event.key == Key.NumPadEnter || event.key == Key.E -> onSelectNearestNode()
        event.key == Key.Backspace -> onClearSelection()
        else -> false
    }
}

/**
 * Pans every frame while [held] is not empty, at [speedPxPerSecond].
 * Opposite directions cancel out, and diagonals are as fast as straight pans.
 */
internal suspend fun panWhileHeld(held: Set<PanDirection>, speedPxPerSecond: Float, onPan: (Offset) -> Unit) {
    val sum = held.fold(Offset.Zero) { total, direction -> total + direction.direction }
    if (sum != Offset.Zero) {
        val velocity = sum / sum.getDistance() * speedPxPerSecond
        var lastFrame = withFrameNanos { it }
        while (currentCoroutineContext().isActive) {
            withFrameNanos { now ->
                onPan(velocity * ((now - lastFrame) / NANOS_PER_SECOND))
                lastFrame = now
            }
        }
    }
}
