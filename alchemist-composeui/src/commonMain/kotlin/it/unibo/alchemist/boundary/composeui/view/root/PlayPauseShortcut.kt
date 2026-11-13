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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState

/**
 * Space plays or pauses the simulation, unless the focused control already used the key.
 */
internal fun Modifier.playPauseOnSpace(
    controls: SimulationControlsState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
): Modifier = onKeyEvent { event ->
    val action = when {
        event.type != KeyEventType.KeyDown || event.key != Key.Spacebar -> null
        controls.canPause -> onPause
        controls.canPlay -> onPlay
        else -> null
    }
    action?.invoke() != null
}
