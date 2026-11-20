/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.controls

import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.pacing.Pacing

/**
 * Errors to show under the dock text fields; `null` where the input is fine.
 */
internal data class DockValidationMessages(
    val toTime: String? = null,
    val toStep: String? = null,
    val fps: String? = null,
    val pacing: String? = null,
) {
    companion object {
        /** The errors in the inputs of [controls]. */
        fun of(controls: SimulationControlsState): DockValidationMessages = with(controls) {
            DockValidationMessages(
                toTime = jumpError(targetTime, time, "Use a numeric time", "Target is before current time"),
                toStep = jumpError(targetStep, step, "Use an integer step", "Target is before current step"),
                fps = fpsValidationError(),
                pacing = pacingValidationError(),
            )
        }

        private fun <N : Comparable<N>> jumpError(
            target: N?,
            current: N,
            parseError: String,
            pastError: String,
        ): String? = when {
            target == null -> parseError
            target < current -> pastError
            else -> null
        }

        private fun SimulationControlsState.fpsValidationError(): String? =
            fpsInput.takeIf { it.isNotBlank() }?.let { input ->
                val fps = input.toIntOrNull() ?: return@let "Use an integer FPS"
                if (fps in MIN_UI_FPS..maxUiFps) null else "Use $MIN_UI_FPS-$maxUiFps FPS"
            }

        private fun SimulationControlsState.pacingValidationError(): String? =
            "Use a positive number".takeIf { Pacing.parseRate(pacingInput) == null }
    }
}
