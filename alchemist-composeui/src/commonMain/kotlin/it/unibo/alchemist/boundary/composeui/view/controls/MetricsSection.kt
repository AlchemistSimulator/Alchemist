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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent

private val readoutSpacing = 4.dp

private val SimulationStatus.color: Color get() = when (this) {
    SimulationStatus.RUNNING -> Positive
    SimulationStatus.PAUSED -> PrimaryAccent
    SimulationStatus.TERMINATED -> Danger
    else -> SecondaryAccent
}

/**
 * The status, the time, and the step, in one line.
 */
@Composable
internal fun MetricsSection(controls: SimulationControlsState) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(stripItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = controls.statusLabel,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.SemiBold,
            color = controls.status.color,
        )
        Readout(label = "time", value = controls.timeLabel)
        Readout(label = "step", value = controls.step.toString())
    }
}

@Composable
private fun Readout(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(readoutSpacing), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.caption, color = SecondaryAccent)
        Text(text = value, style = MaterialTheme.typography.subtitle1.copy(fontFamily = FontFamily.Monospace))
    }
}
