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

/**
 * Errors to show under the dock text fields; `null` where the input is fine.
 */
internal data class DockValidationMessages(
    val toTime: String? = null,
    val toStep: String? = null,
    val fps: String? = null,
) {
    companion object {
        /** The errors in the inputs of [controls]. */
        fun of(controls: SimulationControlsState): DockValidationMessages = with(controls) {
            DockValidationMessages(
                toTime = toTimeInput.numericValidationError("Use a numeric time"),
                toStep = toStepValidationError(),
                fps = fpsValidationError(),
            )
        }

        private fun String.numericValidationError(parseError: String): String? =
            takeIf { it.isNotBlank() }?.let { if (it.toDoubleOrNull() == null) parseError else null }

        private fun SimulationControlsState.toStepValidationError(): String? =
            toStepInput.takeIf { it.isNotBlank() }?.let { input ->
                val targetStep = input.toLongOrNull() ?: return@let "Use an integer step"
                if (targetStep < step) "Target is before current step" else null
            }

        private fun SimulationControlsState.fpsValidationError(): String? =
            fpsInput.takeIf { it.isNotBlank() }?.let { input ->
                val fps = input.toIntOrNull() ?: return@let "Use an integer FPS"
                if (fps in MIN_UI_FPS..maxUiFps) null else "Use $MIN_UI_FPS-$maxUiFps FPS"
            }
    }
}
