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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong

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
        if (compact) {
            CompactDockContent(controls, validation, actions)
        } else {
            WideDockContent(controls, validation, actions)
        }
    }
}

@Composable
private fun WideDockContent(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(dockSectionSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        TransportSection(controls, actions)
        MetricsSection(controls)
        JumpSection(controls, validation, actions)
        PacingSection(controls, validation, actions)
    }
}

@Composable
private fun CompactDockContent(
    controls: SimulationControlsState,
    validation: DockValidationMessages,
    actions: DockActions,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dockHorizontalPadding, vertical = dockVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(dockSectionSpacing),
    ) {
        CompactDockRow {
            TransportSection(controls, actions)
            MetricsSection(controls)
        }
        CompactDockRow {
            JumpSection(controls, validation, actions)
            PacingSection(controls, validation, actions)
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
