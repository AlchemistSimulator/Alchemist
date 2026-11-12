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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import it.unibo.alchemist.boundary.composeui.model.throttling.EventsPerSecond
import it.unibo.alchemist.boundary.composeui.model.throttling.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.MAX_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.MIN_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

private val EventRateControlWidth = 240.dp

@Composable
internal fun PacingSection(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    DockSection(title = "Pacing") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            DockTextField(
                label = "FPS",
                value = controls.fpsInput,
                caption = validation.fps ?: controls.fpsRangeLabel,
                isError = validation.fps != null,
                onValueChange = actions.onFpsInputChanged,
                onSubmit = actions.onFpsSubmit,
            )
            EventRateSlider(
                controls = controls,
                onValueChange = actions.onEventRateChanged,
            )
        }
    }
}

@Composable
private fun EventRateSlider(controls: SimulationControlsState, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.width(EventRateControlWidth),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Events / second",
            style = MaterialTheme.typography.caption,
            color = SecondaryAccent,
        )
        Slider(
            value = controls.eventRateSliderValue,
            onValueChange = onValueChange,
            valueRange =
            MIN_SIMULATION_EVENTS_PER_SECOND.toFloat()..MAX_SIMULATION_EVENTS_PER_SECOND.toFloat(),
            steps = MAX_SIMULATION_EVENTS_PER_SECOND - MIN_SIMULATION_EVENTS_PER_SECOND - 1,
            colors = SliderDefaults.colors(
                thumbColor = PrimaryAccent,
                activeTrackColor = PrimaryAccent,
                inactiveTrackColor = Outline,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = EventsPerSecond(MIN_SIMULATION_EVENTS_PER_SECOND).toLabel(),
                style = MaterialTheme.typography.caption,
                color = TextSecondary,
            )
            if (!controls.isFullThrottle) {
                Text(
                    text = controls.simulationEventThrottling.toLabel(),
                    style = MaterialTheme.typography.caption,
                    color = TextPrimary,
                )
            }
            Text(
                text = FullThrottle.toLabel(),
                fontWeight = if (controls.isFullThrottle) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.caption,
                color = if (controls.isFullThrottle) PrimaryAccent else TextSecondary,
            )
        }
    }
}

private val SimulationControlsState.eventRateSliderValue: Float
    get() = simulationEventThrottling.value
        .coerceAtMost(MAX_SIMULATION_EVENTS_PER_SECOND)
        .coerceAtLeast(MIN_SIMULATION_EVENTS_PER_SECOND)
        .toFloat()
