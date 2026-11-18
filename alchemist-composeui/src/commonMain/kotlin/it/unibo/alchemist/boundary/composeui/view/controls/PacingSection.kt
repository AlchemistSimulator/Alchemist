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
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.MAX_SLIDER_POSITION
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.rateAt
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary

private val eventRateSliderWidth = 160.dp
private val eventRateLabelWidth = 100.dp
private val fpsFieldWidth = 84.dp

/**
 * The UI frame rate box and the simulation speed slider.
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
        DockTextField(
            label = "FPS",
            value = controls.fpsInput,
            error = validation.fps,
            onValueChange = actions.onFpsInputChanged,
            onSubmit = actions.onFpsSubmit,
            width = fpsFieldWidth,
        )
        Text(text = "speed", style = MaterialTheme.typography.caption, color = SecondaryAccent)
        Slider(
            value = controls.simulationEventThrottling.sliderPosition,
            onValueChange = { actions.onEventRateChanged(rateAt(it).toFloat()) },
            modifier = Modifier.width(eventRateSliderWidth),
            valueRange = 0f..MAX_SLIDER_POSITION,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryAccent,
                activeTrackColor = PrimaryAccent,
                inactiveTrackColor = Outline,
            ),
        )
        Text(
            text = controls.simulationEventThrottling.toLabel(),
            modifier = Modifier.width(eventRateLabelWidth),
            style = MaterialTheme.typography.caption,
            fontWeight = if (controls.isFullThrottle) FontWeight.Bold else FontWeight.Normal,
            color = if (controls.isFullThrottle) PrimaryAccent else TextPrimary,
        )
    }
}
