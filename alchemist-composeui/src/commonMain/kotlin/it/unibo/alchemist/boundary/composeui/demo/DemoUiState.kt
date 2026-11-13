/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationProgress
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import kotlinx.collections.immutable.persistentListOf

/**
 * The demo scene shown when no simulation is attached.
 */
internal object DemoUiState {
    /** Demo time units per step. */
    const val TIME_SCALE = 10.0

    private const val INITIAL_STEP = 42L

    /** The scene at startup. */
    val initial: AlchemistUiState = sampleUiState()

    /** The time shown at [step]. */
    fun timeLabel(step: Long): String = (step / TIME_SCALE).formatFixed(2)

    private fun sampleUiState(): AlchemistUiState {
        val nodes = persistentListOf(
            ViewportNode(
                id = 1,
                coordinates = persistentListOf(-3.5, 1.7),
                metadata = persistentListOf(
                    Labeled("Neighbors", "4"),
                    Labeled("Reactions", "3"),
                    Labeled("Properties", "2"),
                ),
                concentrations = persistentListOf(
                    Labeled("signal", "0.91"),
                    Labeled("gradient", "0.42"),
                ),
            ),
            ViewportNode(
                id = 2,
                coordinates = persistentListOf(-1.2, 0.3),
                metadata = persistentListOf(
                    Labeled("Neighbors", "5"),
                    Labeled("Reactions", "2"),
                    Labeled("Properties", "1"),
                ),
                concentrations = persistentListOf(
                    Labeled("source", "true"),
                    Labeled("gradient", "0.68"),
                ),
            ),
            ViewportNode(
                id = 3,
                coordinates = persistentListOf(0.8, 2.2),
                metadata = persistentListOf(
                    Labeled("Neighbors", "3"),
                    Labeled("Reactions", "4"),
                    Labeled("Properties", "2"),
                ),
                concentrations = persistentListOf(
                    Labeled("signal", "0.77"),
                    Labeled("temperature", "296 K"),
                ),
            ),
            ViewportNode(
                id = 4,
                coordinates = persistentListOf(2.1, -0.8),
                metadata = persistentListOf(
                    Labeled("Neighbors", "6"),
                    Labeled("Reactions", "2"),
                    Labeled("Properties", "3"),
                ),
                concentrations = persistentListOf(
                    Labeled("gradient", "0.18"),
                    Labeled("payload", "ready"),
                ),
            ),
            ViewportNode(
                id = 5,
                coordinates = persistentListOf(3.9, 1.4),
                metadata = persistentListOf(
                    Labeled("Neighbors", "2"),
                    Labeled("Reactions", "1"),
                    Labeled("Properties", "1"),
                ),
                concentrations = persistentListOf(
                    Labeled("goal", "true"),
                    Labeled("signal", "0.12"),
                ),
            ),
        )
        return AlchemistUiState(
            scene = ViewportScene(
                nodes = nodes,
                edges = persistentListOf(
                    ViewportEdge(1, 2),
                    ViewportEdge(2, 3),
                    ViewportEdge(3, 4),
                    ViewportEdge(4, 5),
                    ViewportEdge(1, 3),
                ),
                dimensions = 2,
                summary = persistentListOf(
                    Labeled("Nodes", nodes.size),
                    Labeled("Dimensions", "2D"),
                ),
                message = "Attach a simulation monitor to replace this demo scenario.",
            ),
            controls = SimulationControlsState(
                status = SimulationStatus.PAUSED,
                timeLabel = timeLabel(INITIAL_STEP),
                step = INITIAL_STEP,
                progress = SimulationProgress(
                    fraction = 0.42f,
                    label = "Scenario exploration",
                ),
            ),
        )
    }
}
