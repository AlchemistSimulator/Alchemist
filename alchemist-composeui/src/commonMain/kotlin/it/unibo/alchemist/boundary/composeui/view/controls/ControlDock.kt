/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.EventsPerSecond
import it.unibo.alchemist.boundary.composeui.model.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationEventThrottling.Companion.MAX_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.model.SimulationEventThrottling.Companion.MIN_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.view.components.MetricBlock
import it.unibo.alchemist.boundary.composeui.view.components.StatusPill
import it.unibo.alchemist.boundary.composeui.view.components.TransportButton
import it.unibo.alchemist.boundary.composeui.view.theme.Danger
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.Positive
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.Surface as SurfaceColor
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
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
            horizontalArrangement = Arrangement.spacedBy(dockSectionSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            DockSection(title = "Transport") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusPill(controls)
                    transportActions(controls, onPlay, onPause, onStep).forEach { action ->
                        TransportButton(
                            label = action.label,
                            enabled = action.enabled,
                            accent = action.accent,
                            onClick = action.onClick,
                        )
                    }
                }
            }
            DockSection(title = "Metrics") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
                ) {
                    metrics(controls).forEach { metric ->
                        MetricBlock(
                            label = metric.label,
                            value = metric.value,
                        )
                    }
                }
            }
            DockSection(title = "Jump") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
                    verticalAlignment = Alignment.Top,
                ) {
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
                }
            }
            DockSection(title = "Pacing") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
                    verticalAlignment = Alignment.Top,
                ) {
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
    }
}

@Composable
private fun DockSection(title: String, content: @Composable () -> Unit) {
    Surface(
        color = SurfaceColor.copy(alpha = dockSectionSurfaceAlpha),
        shape = RoundedCornerShape(dockSectionCornerRadius),
        border = BorderStroke(dockSectionBorderWidth, Outline.copy(alpha = dockSectionBorderAlpha)),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = dockSectionHorizontalPadding,
                vertical = dockSectionVerticalPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(dockSectionContentSpacing),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.caption,
                color = SecondaryAccent,
                fontWeight = FontWeight.SemiBold,
            )
            Box(contentAlignment = Alignment.CenterStart) {
                content()
            }
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
private val dockHorizontalPadding = 18.dp
private val dockVerticalPadding = 16.dp
private val dockSectionSpacing = 14.dp
private val dockSectionCornerRadius = 12.dp
private val dockSectionBorderWidth = 1.dp
private val dockSectionHorizontalPadding = 14.dp
private val dockSectionVerticalPadding = 12.dp
private val dockSectionContentSpacing = 10.dp
private val sectionItemSpacing = 12.dp
private const val dockSectionSurfaceAlpha = 0.78f
private const val dockSectionBorderAlpha = 0.7f

private data class TransportAction(
    val label: String,
    val enabled: Boolean,
    val accent: Color,
    val onClick: () -> Unit,
)

private data class MetricValue(val label: String, val value: String)

private fun transportActions(
    controls: SimulationControlsState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
): List<TransportAction> = listOf(
    TransportAction(label = "Play", enabled = controls.canPlay, accent = Positive, onClick = onPlay),
    TransportAction(label = "Pause", enabled = controls.canPause, accent = Danger, onClick = onPause),
    TransportAction(label = "Step", enabled = controls.canStep, accent = PrimaryAccent, onClick = onStep),
)

private fun metrics(controls: SimulationControlsState): List<MetricValue> = listOf(
    MetricValue(label = "Time", value = controls.timeLabel),
    MetricValue(label = "Step", value = controls.step.toString()),
)
