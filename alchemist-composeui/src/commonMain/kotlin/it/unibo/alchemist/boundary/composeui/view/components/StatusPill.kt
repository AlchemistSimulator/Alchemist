/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.StatusPillWidth

@Composable
internal fun StatusPill(controls: SimulationControlsState) {
    val presentation = controls.toStatusPillPresentation()
    ComponentSurface(
        modifier = Modifier.width(StatusPillWidth),
        color = presentation.color.copy(alpha = StatusPillAlpha),
        shape = pillShape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(componentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color = presentation.color, shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(statusIndicatorSpacing))
            Text(
                text = presentation.label,
                style = MaterialTheme.typography.subtitle1,
            )
        }
    }
}

private data class StatusPillPresentation(val label: String, val color: Color)

private fun SimulationControlsState.toStatusPillPresentation(): StatusPillPresentation =
    StatusPillPresentation(
        label = statusLabel,
        color =
            when (status) {
                SimulationStatus.RUNNING -> Positive
                SimulationStatus.PAUSED -> PrimaryAccent
                SimulationStatus.TERMINATED -> Danger
                else -> SecondaryAccent
            },
    )

private const val StatusPillAlpha = 0.14f
private val statusIndicatorSpacing = 10.dp
