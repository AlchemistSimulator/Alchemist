/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.components.TransportIcon
import it.unibo.alchemist.boundary.composeui.view.components.TransportIconButton
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent

/**
 * One Play/Pause toggle, showing what a click does, and Step.
 */
@Composable
internal fun TransportSection(controls: SimulationControlsState, actions: DockActions) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (controls.canPause) {
            TransportIconButton(
                icon = TransportIcon.PAUSE,
                label = "Pause",
                enabled = true,
                accent = PrimaryAccent,
                onClick = actions.onPause,
            )
        } else {
            TransportIconButton(
                icon = TransportIcon.PLAY,
                label = "Play",
                enabled = controls.canPlay,
                accent = Positive,
                onClick = actions.onPlay,
            )
        }
        TransportIconButton(
            icon = TransportIcon.STEP,
            label = "Step",
            enabled = controls.canStep,
            accent = PrimaryAccent,
            onClick = actions.onStep,
        )
    }
}
