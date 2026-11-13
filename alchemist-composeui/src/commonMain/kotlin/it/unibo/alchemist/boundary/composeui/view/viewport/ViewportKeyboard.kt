/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Keyboard controls: arrows pan, Enter selects the first node, Backspace clears the selection.
 * Each callback returns whether it handled the key.
 */
internal fun Modifier.viewportKeyboard(
    onPan: (Offset) -> Unit,
    onSelectFirstNode: () -> Boolean,
    onClearSelection: () -> Boolean,
): Modifier = onPreviewKeyEvent { event ->
    event.type == KeyEventType.KeyDown &&
        when (event.key) {
            Key.Enter, Key.NumPadEnter -> onSelectFirstNode()
            Key.Backspace -> onClearSelection()
            else -> PanDirection.of(event.key)?.also { onPan(it.offset) } != null
        }
}
