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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel

private const val DOCK_ALPHA = 0.8f

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
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStep: () -> Unit,
    onToTimeInputChanged: (String) -> Unit,
    onToTimeSubmit: () -> Unit,
    onToStepInputChanged: (String) -> Unit,
    onToStepSubmit: () -> Unit,
    onJumpTimeBy: (Double) -> Unit,
    onJumpStepBy: (Long) -> Unit,
    onFpsInputChanged: (String) -> Unit,
    onFpsSubmit: () -> Unit,
    onEventRateChanged: (Float) -> Unit,
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
        onFpsInputChanged = onFpsInputChanged,
        onFpsSubmit = onFpsSubmit,
        onEventRateChanged = onEventRateChanged,
    )
    Surface(
        modifier = modifier,
        color = FloatingPanel.copy(alpha = DOCK_ALPHA),
        shape = RoundedCornerShape(dockCornerRadius),
        elevation = 0.dp,
    ) {
        DockContent(controls, nodeCount, validation, actions)
    }
}

@Composable
private fun DockContent(
    controls: SimulationControlsState,
    nodeCount: Int,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    DockRow(
        spacing = dockSectionSpacing,
        modifier = Modifier.padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
    ) {
        TransportSection(controls, actions)
        DockDivider()
        MetricsSection(controls)
        DockDivider()
        JumpSection(controls, nodeCount, validation, actions)
        DockDivider()
        PacingSection(controls, validation, actions)
    }
}

/**
 * Lays out [content] in a single row of equally tall items, each at its own width.
 * If the row does not fit, it shrinks as a whole, text included, until it does.
 */
@Composable
private fun DockRow(spacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    SubcomposeLayout(modifier) { constraints ->
        val naturalWidth = subcompose(DockRowSlot.NATURAL, content)
            .map { it.measure(Constraints()) }
            .let { items -> items.sumOf { it.width } + spacing.roundToPx() * (items.size - 1) }
        val scale = when {
            constraints.hasBoundedWidth && naturalWidth > constraints.maxWidth ->
                constraints.maxWidth.toFloat() / naturalWidth
            else -> 1f
        }
        val scaled = Density(density.density * scale, density.fontScale)
        val items = subcompose(DockRowSlot.SCALED) {
            CompositionLocalProvider(LocalDensity provides scaled, content = content)
        }.map { it.measure(Constraints()) }
        val gap = with(scaled) { spacing.roundToPx() }
        val height = items.maxOfOrNull { it.height } ?: 0
        val cells = subcompose(DockRowSlot.SIZED) {
            CompositionLocalProvider(LocalDensity provides scaled, content = content)
        }.mapIndexed { index, measurable -> measurable.measure(Constraints.fixed(items[index].width, height)) }
        val width = (cells.sumOf { it.width } + gap * (cells.size - 1)).coerceIn(0, constraints.maxWidth)
        layout(width, height) {
            cells.fold(0) { x, cell ->
                cell.place(x, 0)
                x + cell.width + gap
            }
        }
    }
}

private enum class DockRowSlot { NATURAL, SCALED, SIZED }
