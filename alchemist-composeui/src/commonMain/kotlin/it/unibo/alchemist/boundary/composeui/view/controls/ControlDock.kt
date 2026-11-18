/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.theme.FloatingPanel
import it.unibo.alchemist.boundary.composeui.view.theme.Outline

private const val DOCK_ALPHA = 0.8f

private val dockHorizontalPadding = 12.dp
private val dockVerticalPadding = 6.dp
private val dockSectionSpacing = 12.dp
private val dockCornerRadius = 24.dp
private val dividerInset = 6.dp

/** The space between the items of a dock section. */
internal val stripItemSpacing = 8.dp

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
    val validation = DockValidationMessages.of(controls)
    val actions = DockActions(
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
    Surface(
        modifier = modifier,
        color = FloatingPanel.copy(alpha = DOCK_ALPHA),
        shape = RoundedCornerShape(dockCornerRadius),
        elevation = 0.dp,
    ) {
        DockContent(controls, validation, actions)
    }
}

@Composable
private fun DockContent(controls: SimulationControlsState, validation: DockValidationMessages, actions: DockActions) {
    DockRow(
        spacing = dockSectionSpacing,
        modifier = Modifier.padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
    ) {
        TransportSection(controls, actions)
        DockDivider()
        MetricsSection(controls)
        DockDivider()
        JumpSection(controls, validation, actions)
        DockDivider()
        PacingSection(controls, validation, actions)
    }
}

@Composable
private fun DockDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = dividerInset)
            .width(1.dp)
            .background(Outline),
    )
}

/**
 * Lays out [content] in one row of equally tall items, each at its own width.
 * Only if the row does not fit do the items wrap onto more rows, each centered.
 */
@Composable
private fun DockRow(spacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SubcomposeLayout(modifier) { constraints ->
        val gap = spacing.roundToPx()
        val natural = subcompose(DockRowSlot.NATURAL, content).map { it.measure(Constraints()) }
        val height = natural.maxOfOrNull { it.height } ?: 0
        val widths = natural.map { it.width.coerceAtMost(constraints.maxWidth) }
        val rows = widths.indices.fold(emptyList<List<Int>>()) { rows, index ->
            val last = rows.lastOrNull()
            when {
                last != null && last.sumOf { widths[it] + gap } + widths[index] <= constraints.maxWidth ->
                    rows.dropLast(1) + listOf(last + index)
                else -> rows + listOf(listOf(index))
            }
        }
        val cells = subcompose(DockRowSlot.FINAL, content).mapIndexed { index, measurable ->
            measurable.measure(Constraints.fixed(widths[index], height))
        }
        val rowWidths = rows.map { row -> row.sumOf { widths[it] } + gap * (row.size - 1) }
        val width = rowWidths.maxOrNull() ?: 0
        layout(width, (height * rows.size + gap * (rows.size - 1)).coerceAtLeast(0)) {
            rows.forEachIndexed { rowIndex, row ->
                row.fold((width - rowWidths[rowIndex]) / 2) { x, index ->
                    cells[index].place(x, rowIndex * (height + gap))
                    x + widths[index] + gap
                }
            }
        }
    }
}

private enum class DockRowSlot { NATURAL, FINAL }
