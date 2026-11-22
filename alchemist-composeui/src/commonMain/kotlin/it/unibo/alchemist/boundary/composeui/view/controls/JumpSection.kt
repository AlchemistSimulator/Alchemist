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
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatCompact
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import it.unibo.alchemist.boundary.composeui.view.components.TransportButtonSize
import it.unibo.alchemist.boundary.composeui.view.components.componentShape
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.viewport.clickableKeepingShortcuts

private val jumpFieldWidth = 110.dp
private val quickJumpPadding = 6.dp
private val quickJumpSpacing = 4.dp

/**
 * The "to time" and "to step" jumps, split by a line.
 * Each has a button that jumps to the target in its box, then buttons that jump ahead by a fixed amount.
 */
@Composable
internal fun JumpSection(
    controls: SimulationControlsState,
    nodeCount: Int,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        JumpButton(
            label = "to time",
            description = "Run to the time in the box",
            onClick = actions.onToTimeSubmit,
        )
        DockTextField(
            label = null,
            value = controls.toTimeInput,
            default = controls.defaultTargetTime.formatFixed(DISPLAYED_TIME_DECIMALS),
            width = jumpFieldWidth,
            error = validation.toTime,
            onValueChange = actions.onToTimeInputChanged,
            onSubmit = actions.onToTimeSubmit,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(quickJumpSpacing)) {
            controls.quickJumps.time.forEach { delta ->
                JumpButton(
                    label = "+${delta.formatCompact()}",
                    description = "Run ${delta.formatCompact()} time units ahead",
                    onClick = { actions.onJumpTimeBy(delta) },
                )
            }
        }
        DockDivider(Modifier.height(TransportButtonSize))
        JumpButton(
            label = "to step",
            description = "Run to the step in the box",
            onClick = actions.onToStepSubmit,
        )
        DockTextField(
            label = null,
            value = controls.toStepInput,
            default = controls.defaultTargetStep.toString(),
            width = jumpFieldWidth,
            error = validation.toStep,
            onValueChange = actions.onToStepInputChanged,
            onSubmit = actions.onToStepSubmit,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(quickJumpSpacing)) {
            controls.quickJumps.stepDeltas(nodeCount).forEach { delta ->
                JumpButton(
                    label = "+${delta.formatCompact()}",
                    description = "Run $delta steps ahead",
                    onClick = { actions.onJumpStepBy(delta) },
                )
            }
        }
    }
}

@Composable
private fun JumpButton(label: String, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(TransportButtonSize)
            .clip(componentShape)
            .border(width = 1.dp, color = Outline, shape = componentShape)
            .clickableKeepingShortcuts(onClickLabel = description, onClick = onClick)
            .padding(horizontal = quickJumpPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.caption, color = SecondaryAccent)
    }
}
