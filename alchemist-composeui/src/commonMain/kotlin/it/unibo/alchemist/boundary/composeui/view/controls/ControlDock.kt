/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import kotlin.math.roundToInt

private const val DOCK_ALPHA = 0.8f
private const val DOCK_DIVIDER = "divider"

private val dockHorizontalPadding = 12.dp
private val dockVerticalPadding = 6.dp
private val dockSectionSpacing = 12.dp
private val dockCornerRadius = 24.dp

/** The space between the items of a dock section. */
internal val stripItemSpacing = 8.dp

@Composable
internal fun ControlDock(
    controls: SimulationControlsState,
    nodeCount: Int,
    settingsOpen: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
    onToTimeInputChanged: (String) -> Unit,
    onToTimeSubmit: () -> Unit,
    onToStepInputChanged: (String) -> Unit,
    onToStepSubmit: () -> Unit,
    onJumpTimeBy: (Double) -> Unit,
    onJumpStepBy: (Long) -> Unit,
    onPacingInputChanged: (String) -> Unit,
    onPacingModeChanged: (PacingMode) -> Unit,
    onToggleSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val validation = DockValidationMessages.of(controls)
    val actions = DockActions(
        onPlay = onPlay,
        onPause = onPause,
        onStep = onStep,
        onToTimeInputChanged = onToTimeInputChanged,
        onToTimeSubmit = onToTimeSubmit,
        onToStepInputChanged = onToStepInputChanged,
        onToStepSubmit = onToStepSubmit,
        onJumpTimeBy = onJumpTimeBy,
        onJumpStepBy = onJumpStepBy,
        onPacingInputChanged = onPacingInputChanged,
        onPacingModeChanged = onPacingModeChanged,
        onToggleSettings = onToggleSettings,
    )
    Surface(
        modifier = modifier,
        color = FloatingPanel.copy(alpha = DOCK_ALPHA),
        shape = RoundedCornerShape(dockCornerRadius),
        elevation = 0.dp,
    ) {
        DockContent(controls, nodeCount, settingsOpen, validation, actions)
    }
}

@Composable
private fun DockContent(
    controls: SimulationControlsState,
    nodeCount: Int,
    settingsOpen: Boolean,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    DockRow(
        spacing = dockSectionSpacing,
        modifier = Modifier.padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
    ) {
        TransportSection(controls, actions)
        DockDivider(Modifier.layoutId(DOCK_DIVIDER))
        MetricsSection(controls)
        DockDivider(Modifier.layoutId(DOCK_DIVIDER))
        JumpSection(controls, nodeCount, validation, actions)
        DockDivider(Modifier.layoutId(DOCK_DIVIDER))
        PacingSection(controls, validation, actions)
        DockDivider(Modifier.layoutId(DOCK_DIVIDER))
        SettingsSection(settingsOpen, actions)
    }
}

/**
 * Lays out [content] in a single row, each item at its own width and centered vertically.
 * The items tagged [DOCK_DIVIDER] get the height of the tallest other item.
 * If the row does not fit, it is drawn smaller as a whole, text included, until it does.
 * Everything is composed once: the hidden copies of a measuring pass would show up as duplicate controls.
 */
@Composable
private fun DockRow(spacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val items = measurables
            .filter { it.layoutId != DOCK_DIVIDER }
            .associateWith { it.measure(Constraints()) }
        val height = items.values.maxOfOrNull { it.height } ?: 0
        val placeables = measurables.map { items[it] ?: it.measure(Constraints.fixedHeight(height)) }
        val naturalWidth = placeables.sumOf { it.width } + gap * (placeables.size - 1).coerceAtLeast(0)
        val scale = when {
            constraints.hasBoundedWidth && naturalWidth > constraints.maxWidth ->
                constraints.maxWidth.toFloat() / naturalWidth
            else -> 1f
        }
        layout((naturalWidth * scale).roundToInt(), (height * scale).roundToInt()) {
            placeables.fold(0) { x, placeable ->
                placeable.placeWithLayer(
                    x = (x * scale).roundToInt(),
                    y = ((height - placeable.height) / 2 * scale).roundToInt(),
                ) {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                x + placeable.width + gap
            }
        }
    }
}
