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
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
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
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val validation = controls.validationMessages()
    Surface(
        modifier = modifier,
        color = SurfaceStrong,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        if (compact) {
            CompactDockContent(
                controls = controls,
                validation = validation,
                onPlay = onPlay,
                onPause = onPause,
                onStep = onStep,
                onToTimeInputChanged = onToTimeInputChanged,
                onToTimeSubmit = onToTimeSubmit,
                onToStepInputChanged = onToStepInputChanged,
                onToStepSubmit = onToStepSubmit,
                onFpsInputChanged = onFpsInputChanged,
                onFpsSubmit = onFpsSubmit,
                onEventRateChanged = onEventRateChanged,
            )
        } else {
            WideDockContent(
                controls = controls,
                validation = validation,
                onPlay = onPlay,
                onPause = onPause,
                onStep = onStep,
                onToTimeInputChanged = onToTimeInputChanged,
                onToTimeSubmit = onToTimeSubmit,
                onToStepInputChanged = onToStepInputChanged,
                onToStepSubmit = onToStepSubmit,
                onFpsInputChanged = onFpsInputChanged,
                onFpsSubmit = onFpsSubmit,
                onEventRateChanged = onEventRateChanged,
            )
        }
    }
}

@Composable
private fun WideDockContent(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
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
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(dockSectionSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        TransportSection(controls, onPlay, onPause, onStep)
        MetricsSection(controls)
        JumpSection(
            controls = controls,
            validation = validation,
            onToTimeInputChanged = onToTimeInputChanged,
            onToTimeSubmit = onToTimeSubmit,
            onToStepInputChanged = onToStepInputChanged,
            onToStepSubmit = onToStepSubmit,
        )
        PacingSection(
            controls = controls,
            validation = validation,
            onFpsInputChanged = onFpsInputChanged,
            onFpsSubmit = onFpsSubmit,
            onEventRateChanged = onEventRateChanged,
        )
    }
}

@Composable
private fun CompactDockContent(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(dockSectionSpacing),
    ) {
        CompactDockRow {
            TransportSection(controls, onPlay, onPause, onStep)
            MetricsSection(controls)
        }
        CompactDockRow {
            JumpSection(
                controls = controls,
                validation = validation,
                onToTimeInputChanged = onToTimeInputChanged,
                onToTimeSubmit = onToTimeSubmit,
                onToStepInputChanged = onToStepInputChanged,
                onToStepSubmit = onToStepSubmit,
            )
            PacingSection(
                controls = controls,
                validation = validation,
                onFpsInputChanged = onFpsInputChanged,
                onFpsSubmit = onFpsSubmit,
                onEventRateChanged = onEventRateChanged,
            )
        }
    }
}

@Composable
private fun CompactDockRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(dockSectionSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        content()
    }
}

@Composable
private fun TransportSection(
    controls: SimulationControlsState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
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
}

@Composable
private fun MetricsSection(controls: SimulationControlsState) {
    DockSection(title = "Metrics") {
        Row(horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing)) {
            metrics(controls).forEach { metric ->
                MetricBlock(
                    label = metric.label,
                    value = metric.value,
                )
            }
        }
    }
}

@Composable
private fun JumpSection(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    onToTimeInputChanged: (String) -> Unit,
    onToTimeSubmit: () -> Unit,
    onToStepInputChanged: (String) -> Unit,
    onToStepSubmit: () -> Unit,
) {
    DockSection(title = "Jump") {
        Row(
            horizontalArrangement = Arrangement.spacedBy(sectionItemSpacing),
            verticalAlignment = Alignment.Top,
        ) {
            DockTextField(
                label = "To Time",
                value = controls.toTimeInput,
                caption = validation.toTime ?: "Enter to jump",
                isError = validation.toTime != null,
                onValueChange = onToTimeInputChanged,
                onSubmit = onToTimeSubmit,
            )
            DockTextField(
                label = "To Step",
                value = controls.toStepInput,
                caption = validation.toStep ?: "Enter to jump",
                isError = validation.toStep != null,
                onValueChange = onToStepInputChanged,
                onSubmit = onToStepSubmit,
            )
        }
    }
}

@Composable
private fun PacingSection(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    onFpsInputChanged: (String) -> Unit,
    onFpsSubmit: () -> Unit,
    onEventRateChanged: (Float) -> Unit,
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
    isError: Boolean,
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
            isError = isError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = TextPrimary,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = Outline,
                errorBorderColor = Danger,
                errorLabelColor = Danger,
                errorCursorColor = Danger,
                focusedLabelColor = PrimaryAccent,
                unfocusedLabelColor = TextSecondary,
                cursorColor = PrimaryAccent,
            ),
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.caption,
            color = if (isError) Danger else TextSecondary,
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

private data class DockValidationMessages(
    val toTime: String? = null,
    val toStep: String? = null,
    val fps: String? = null,
)

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

private val SimulationControlsState.eventRateSliderValue: Float
    get() = simulationEventThrottling.value
        .coerceAtMost(MAX_SIMULATION_EVENTS_PER_SECOND)
        .coerceAtLeast(MIN_SIMULATION_EVENTS_PER_SECOND)
        .toFloat()

private fun SimulationControlsState.validationMessages(): DockValidationMessages = DockValidationMessages(
    toTime = toTimeInput.numericValidationError("Use a numeric time"),
    toStep = toStepValidationError(),
    fps = fpsValidationError(),
)

private fun String.numericValidationError(parseError: String): String? = takeIf { it.isNotBlank() }?.let {
    if (it.toDoubleOrNull() == null) parseError else null
}

private fun SimulationControlsState.toStepValidationError(): String? = toStepInput.takeIf {
    it.isNotBlank()
}?.let { input ->
    val targetStep = input.toLongOrNull() ?: return@let "Use an integer step"
    if (targetStep < step) "Target is before current step" else null
}

private fun SimulationControlsState.fpsValidationError(): String? = fpsInput.takeIf { it.isNotBlank() }?.let {
    val fps = it.toIntOrNull() ?: return@let "Use an integer FPS"
    if (fps in MIN_UI_FPS..maxUiFps) null else "Use ${MIN_UI_FPS}-$maxUiFps FPS"
}
