/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MIN_UI_FPS
import it.unibo.alchemist.boundary.composeui.model.pacing.Pacing
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed

/**
 * State for transport controls and simulator metrics.
 *
 * @property status the simulation status.
 * @property time the simulation time.
 * @property step the number of steps done.
 * @property progress the simulation progress.
 * @property toTimeInput the text in the "to time" box; empty means one time unit ahead.
 * @property toStepInput the text in the "to step" box; empty means the next step.
 * @property fpsInput the text in the FPS box.
 * @property uiFps the UI frame rate.
 * @property maxUiFps the highest allowed UI frame rate.
 * @property pacing the limit on simulation speed.
 * @property pacingInput the text in the pacing box.
 * @property quickJumps how far the quick-jump buttons go.
 * @property dialog the error to show, if any.
 */
@Immutable
internal data class SimulationControlsState(
    val status: SimulationStatus = SimulationStatus.INIT,
    val time: Double = 0.0,
    val step: Long = 0L,
    val progress: SimulationProgress = SimulationProgress(),
    val toTimeInput: String = "",
    val toStepInput: String = "",
    val fpsInput: String = SimulationControlsConfig.DEFAULT_UI_FPS.toString(),
    val uiFps: Int = SimulationControlsConfig.DEFAULT_UI_FPS,
    val maxUiFps: Int = SimulationControlsConfig.MAX_UI_FPS,
    val pacing: Pacing = Pacing(),
    val pacingInput: String = pacing.rateLabel,
    val quickJumps: QuickJumps = QuickJumps(),
    val dialog: ControlDialogState? = null,
) {
    init {
        require(maxUiFps >= SimulationControlsConfig.MIN_UI_FPS) {
            "Maximum UI fps must be at least ${SimulationControlsConfig.MIN_UI_FPS}."
        }
        require(uiFps in SimulationControlsConfig.MIN_UI_FPS..maxUiFps) {
            "UI fps must remain within [${SimulationControlsConfig.MIN_UI_FPS}, $maxUiFps]."
        }
    }

    /** Whether Play is enabled. */
    val canPlay: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED

    /** Whether Pause is enabled. */
    val canPause: Boolean = status == SimulationStatus.RUNNING

    /** Whether Step is enabled. */
    val canStep: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED

    /** The time, as text. */
    val timeLabel: String = time.formatFixed(DISPLAYED_TIME_DECIMALS)

    /** The step "To Step" shows when the user is not editing it. */
    val defaultTargetStep: Long = step + 1

    /** The time "To Time" shows when the user is not editing it. */
    val defaultTargetTime: Double = time + 1

    /** The step to jump to, or null if the input is not an integer. */
    val targetStep: Long? = if (toStepInput.isBlank()) defaultTargetStep else toStepInput.toLongOrNull()

    /** The time to jump to, or null if the input is not a number. */
    val targetTime: Double? = if (toTimeInput.isBlank()) defaultTargetTime else toTimeInput.toDoubleOrNull()

    /** The status, as text. */
    val statusLabel: String = status.name.lowercase().replaceFirstChar(Char::uppercaseChar)

    /** The allowed frame rates, as text. */
    val fpsRangeLabel: String = "${SimulationControlsConfig.MIN_UI_FPS}-$maxUiFps FPS"

    /** Returns a copy with [value] in the frame rate box, applied at once if it is an allowed frame rate. */
    internal fun withFpsInput(value: String): SimulationControlsState = copy(
        fpsInput = value,
        uiFps = value.toIntOrNull()?.takeIf { it in MIN_UI_FPS..maxUiFps } ?: uiFps,
    )

    /** Returns a copy with [value] in the pacing box, applied at once if it is a positive number. */
    internal fun withPacingInput(value: String): SimulationControlsState =
        copy(pacingInput = value, pacing = pacing.withRate(value))

    /** Returns a copy paced by [mode], at its default rate. */
    internal fun withPacingMode(mode: PacingMode): SimulationControlsState =
        Pacing(mode).let { copy(pacing = it, pacingInput = it.rateLabel) }

    /** Returns a copy with the frame rate set to [target], clamped to the allowed range. */
    internal fun withUiFps(target: Int): SimulationControlsState {
        val clamped = target.coerceIn(MIN_UI_FPS, maxUiFps)
        return copy(fpsInput = clamped.toString(), uiFps = clamped, dialog = null)
    }
}
