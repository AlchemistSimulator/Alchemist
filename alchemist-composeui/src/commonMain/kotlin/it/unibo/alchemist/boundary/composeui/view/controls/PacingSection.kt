/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.boundary.composeui.view.components.TransportButtonSize
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private val pacingFieldWidth = 80.dp
private val modeButtonPadding = 6.dp
private val modeButtonSpacing = 4.dp

/**
 * The simulation speed limit: one button per [PacingMode], and a box for the rate of the selected one.
 * A rate applies as soon as it is typed.
 */
@Composable
internal fun PacingSection(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(modeButtonSpacing)) {
            PacingMode.entries.forEach { mode ->
                ModeButton(
                    mode = mode,
                    selected = mode == controls.pacing.mode,
                    onClick = { actions.onPacingModeChanged(mode) },
                )
            }
        }
        if (controls.pacing.mode != PacingMode.MAX_SPEED) {
            DockTextField(
                label = null,
                value = controls.pacingInput,
                error = validation.pacing,
                onValueChange = actions.onPacingInputChanged,
                onSubmit = {},
                width = pacingFieldWidth,
            )
        }
    }
}

@Composable
private fun ModeButton(mode: PacingMode, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(TransportButtonSize)
            .clip(componentShape)
            .border(width = 1.dp, color = if (selected) PrimaryAccent else Outline, shape = componentShape)
            .semantics { this.selected = selected }
            .clickableKeepingShortcuts(onClickLabel = mode.description, onClick = onClick)
            .padding(horizontal = modeButtonPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mode.label,
            style = MaterialTheme.typography.caption,
            color = if (selected) PrimaryAccent else SecondaryAccent,
        )
    }
}
