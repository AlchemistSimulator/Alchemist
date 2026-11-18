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
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed

private val jumpFieldWidth = 150.dp

/**
 * The "to time" and "to step" boxes: Enter jumps there.
 */
@Composable
internal fun JumpSection(controls: SimulationControlsState, validation: DockValidationMessages, actions: DockActions) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DockTextField(
            label = "to time",
            value = controls.toTimeInput,
            default = controls.defaultTargetTime.formatFixed(DISPLAYED_TIME_DECIMALS),
            width = jumpFieldWidth,
            error = validation.toTime,
            onValueChange = actions.onToTimeInputChanged,
            onSubmit = actions.onToTimeSubmit,
        )
        DockTextField(
            label = "to step",
            value = controls.toStepInput,
            default = controls.defaultTargetStep.toString(),
            width = jumpFieldWidth,
            error = validation.toStep,
            onValueChange = actions.onToStepInputChanged,
            onSubmit = actions.onToStepSubmit,
        )
    }
}
