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
import androidx.compose.runtime.Composable
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.view.components.MetricBlock

@Composable
internal fun MetricsSection(controls: SimulationControlsState) {
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

private data class MetricValue(val label: String, val value: String)

private fun metrics(controls: SimulationControlsState): List<MetricValue> = listOf(
    MetricValue(label = "Time", value = controls.timeLabel),
    MetricValue(label = "Step", value = controls.step.toString()),
)
