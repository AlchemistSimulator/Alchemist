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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong

private const val COMPACT_COLUMNS = 2
private const val WIDE_COLUMNS = 4

private val dockHorizontalPadding = 18.dp
private val dockVerticalPadding = 16.dp
private val dockSectionSpacing = 14.dp

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
        color = SurfaceStrong,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        DockContent(controls, validation, actions, columns = if (compact) COMPACT_COLUMNS else WIDE_COLUMNS)
    }
}

@Composable
private fun DockContent(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    actions: DockActions,
    columns: Int,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
    ) {
        EqualCells(
            columns = columns,
            spacing = dockSectionSpacing,
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .widthIn(min = maxWidth),
        ) {
            TransportSection(controls, actions)
            MetricsSection(controls)
            JumpSection(controls, validation, actions)
            PacingSection(controls, validation, actions)
        }
    }
}

/**
 * Lays out [content] in a grid of [columns] cells of the same size:
 * as wide as the widest item, or wider to fill the minimum width, and as tall as the tallest.
 */
@Composable
private fun EqualCells(columns: Int, spacing: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SubcomposeLayout(modifier) { constraints ->
        val gap = spacing.roundToPx()
        val natural = subcompose(EqualCellsSlot.NATURAL, content).map { it.measure(Constraints()) }
        val cellWidth = maxOf(
            natural.maxOfOrNull { it.width } ?: 0,
            (constraints.minWidth - gap * (columns - 1)) / columns,
        )
        val cellHeight = natural.maxOfOrNull { it.height } ?: 0
        val cells = subcompose(EqualCellsSlot.FINAL, content).map {
            it.measure(Constraints.fixed(cellWidth, cellHeight))
        }
        val rows = (cells.size + columns - 1) / columns
        val width = (cellWidth * columns + gap * (columns - 1)).coerceAtLeast(constraints.minWidth)
        val height = (cellHeight * rows + gap * (rows - 1)).coerceAtLeast(0)
        layout(width, height) {
            cells.forEachIndexed { index, cell ->
                cell.place(x = index % columns * (cellWidth + gap), y = index / columns * (cellHeight + gap))
            }
        }
    }
}

private enum class EqualCellsSlot { NATURAL, FINAL }
