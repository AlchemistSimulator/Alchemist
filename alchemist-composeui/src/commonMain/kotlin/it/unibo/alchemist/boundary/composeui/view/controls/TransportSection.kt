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
import androidx.compose.ui.graphics.Color
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.components.StatusPill
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent

@Composable
internal fun TransportSection(controls: SimulationControlsState, actions: DockActions) {
    DockSection(title = "Transport") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(controls)
            transportActions(controls, actions).forEach { action ->
                TransportButton(
                    label = action.label,
                    enabled = action.enabled,
                    accent = action.accent,
                    onClick = action.onClick,
                )
            }
        }
    }
}

private data class TransportAction(val label: String, val enabled: Boolean, val accent: Color, val onClick: () -> Unit)

private fun transportActions(controls: SimulationControlsState, actions: DockActions): List<TransportAction> = listOf(
    TransportAction(label = "Play", enabled = controls.canPlay, accent = Positive, onClick = actions.onPlay),
    TransportAction(label = "Pause", enabled = controls.canPause, accent = Danger, onClick = actions.onPause),
    TransportAction(label = "Step", enabled = controls.canStep, accent = PrimaryAccent, onClick = actions.onStep),
)
