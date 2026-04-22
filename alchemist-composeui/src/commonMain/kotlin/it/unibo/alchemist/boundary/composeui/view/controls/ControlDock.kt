/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.EventsPerSecond
import it.unibo.alchemist.boundary.composeui.model.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationEventThrottling.Companion.MAX_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.model.SimulationEventThrottling.Companion.MIN_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.view.components.MetricBlock
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.TextSecondary

@Composable
internal fun ControlDock(
    controls: SimulationControlsState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
    onToTimeInputChanged: (String) -> Unit,
    onToTimeSubmit: () -> Unit,
    onToStepInputChanged: (String) -> Unit,
    onToStepSubmit: () -> Unit,
    onFpsInputChanged: (String) -> Unit,
    onFpsSubmit: () -> Unit,
    onEventRateChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = SurfaceStrong,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            TransportButton(label = "Play", enabled = controls.canPlay, accent = Positive, onClick = onPlay)
            TransportButton(label = "Pause", enabled = controls.canPause, accent = Danger, onClick = onPause)
            TransportButton(label = "Step", enabled = controls.canStep, accent = PrimaryAccent, onClick = onStep)
            MetricBlock(label = "Time", value = controls.timeLabel)
            MetricBlock(label = "Step", value = controls.step.toString())
            DockTextField(
                label = "To Time",
                value = controls.toTimeInput,
                caption = "Enter to jump",
                onValueChange = onToTimeInputChanged,
                onSubmit = onToTimeSubmit,
            )
            DockTextField(
                label = "To Step",
                value = controls.toStepInput,
                caption = "Enter to jump",
                onValueChange = onToStepInputChanged,
                onSubmit = onToStepSubmit,
            )
            DockTextField(
                label = "FPS",
                value = controls.fpsInput,
                caption = controls.fpsRangeLabel,
                onValueChange = onFpsInputChanged,
                onSubmit = onFpsSubmit,
            )
            EventRateSlider(
                controls = controls,
                onValueChange = onEventRateChanged,
            )
        }
    }
}

@Composable
private fun DockTextField(
    label: String,
    value: String,
    caption: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .width(DockTextFieldWidth)
                .onPreviewKeyEvent {
                    val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
                    if (it.type == KeyEventType.KeyUp && isEnter) {
                        onSubmit()
                        true
                    } else {
                        false
                    }
                },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = TextPrimary,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = Outline,
                focusedLabelColor = PrimaryAccent,
                unfocusedLabelColor = TextSecondary,
                cursorColor = PrimaryAccent,
            ),
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.caption,
            color = TextSecondary,
        )
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
            value = controls.simulationEventThrottling.value.toFloat(),
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

private val DockTextFieldWidth = 132.dp
private val EventRateControlWidth = 240.dp
