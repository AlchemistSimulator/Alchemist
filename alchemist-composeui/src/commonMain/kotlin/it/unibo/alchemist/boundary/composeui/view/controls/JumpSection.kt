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
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed

@Composable
internal fun JumpSection(controls: SimulationControlsState, validation: DockValidationMessages, actions: DockActions) {
    DockSection(title = "Jump") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            DockTextField(
                label = "To Time",
                value = controls.toTimeInput,
                default = controls.defaultTargetTime.formatFixed(DISPLAYED_TIME_DECIMALS),
                hint = "Enter to jump",
                error = validation.toTime,
                onValueChange = actions.onToTimeInputChanged,
                onSubmit = actions.onToTimeSubmit,
            )
            DockTextField(
                label = "To Step",
                value = controls.toStepInput,
                default = controls.defaultTargetStep.toString(),
                hint = "Enter to jump",
                error = validation.toStep,
                onValueChange = actions.onToStepInputChanged,
                onSubmit = actions.onToStepSubmit,
            )
        }
    }
}
