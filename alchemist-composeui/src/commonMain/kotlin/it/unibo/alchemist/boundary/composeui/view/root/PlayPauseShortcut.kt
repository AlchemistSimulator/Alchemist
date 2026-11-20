/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.root

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState

/**
 * Space always plays or pauses the simulation, whatever control has the focus:
 * the key is taken before that control sees it, both when pressed and when released.
 */
internal fun Modifier.playPauseOnSpace(
    controls: SimulationControlsState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
): Modifier = onPreviewKeyEvent { event ->
    val isSpace = event.key == Key.Spacebar
    if (isSpace && event.type == KeyEventType.KeyDown) {
        when {
            controls.canPause -> onPause()
            controls.canPlay -> onPlay()
        }
    }
    isSpace
}
