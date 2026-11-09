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
import it.unibo.alchemist.boundary.composeui.model.throttling.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling

/**
 * State for transport controls and simulator metrics.
 *
 * @property status the simulation status.
 * @property timeLabel the simulation time, as text.
 * @property step the number of steps done.
 * @property progress the simulation progress.
 * @property toTimeInput the text in the "to time" box.
 * @property toStepInput the text in the "to step" box.
 * @property fpsInput the text in the FPS box.
 * @property uiFps the UI frame rate.
 * @property maxUiFps the highest allowed UI frame rate.
 * @property simulationEventThrottling the limit on simulation speed.
 * @property dialog the error to show, if any.
 */
@Immutable
data class SimulationControlsState(
    val status: SimulationStatus = SimulationStatus.INIT,
    val timeLabel: String = "0",
    val step: Long = 0L,
    val progress: SimulationProgress = SimulationProgress(),
    val toTimeInput: String = "",
    val toStepInput: String = "",
    val fpsInput: String = SimulationControlsConfig.DEFAULT_UI_FPS.toString(),
    val uiFps: Int = SimulationControlsConfig.DEFAULT_UI_FPS,
    val maxUiFps: Int = SimulationControlsConfig.DEFAULT_MAX_UI_FPS,
    val simulationEventThrottling: SimulationEventThrottling = FullThrottle,
//    val eventRateSliderValue: Int = MIN_SIMULATION_EVENTS_PER_SECOND,
//    val maxEventRateSliderValue: Int = MAX_SIMULATION_EVENTS_PER_SECOND,
    val dialog: ControlDialogState? = null,
) {
    init {
        require(maxUiFps >= SimulationControlsConfig.MIN_UI_FPS) {
            "Maximum UI fps must be at least ${SimulationControlsConfig.MIN_UI_FPS}."
        }
        require(uiFps in SimulationControlsConfig.MIN_UI_FPS..maxUiFps) {
            "UI fps must remain within [${SimulationControlsConfig.MIN_UI_FPS}, $maxUiFps]."
        }
//        require(eventRateSliderValue in MIN_SIMULATION_EVENTS_PER_SECOND..maxEventRateSliderValue) {
//            "Slider value must remain within [${MIN_SIMULATION_EVENTS_PER_SECOND}, $maxEventRateSliderValue]."
//        }
//        require(maxEventRateSliderValue > MIN_SIMULATION_EVENTS_PER_SECOND) {
//            "Maximum slider value must exceed the minimum event rate."
//        }
    }

    /** Whether Play is enabled. */
    val canPlay: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED

    /** Whether Pause is enabled. */
    val canPause: Boolean = status == SimulationStatus.RUNNING

    /** Whether Step is enabled. */
    val canStep: Boolean = status == SimulationStatus.READY || status == SimulationStatus.PAUSED

    /** The status, as text. */
    val statusLabel: String = status.name.lowercase().replaceFirstChar(Char::uppercaseChar)

    /** Whether the simulation runs at full speed. */
    val isFullThrottle: Boolean = simulationEventThrottling is FullThrottle

//    val effectiveEventsPerSecond: Int? = eventRateSliderValue.takeUnless { isFullThrottle }
//    val eventRateLabel: String =
//        effectiveEventsPerSecond?.let { "$it evt/s" } ?: "∞"

    /** The allowed frame rates, as text. */
    val fpsRangeLabel: String = "${SimulationControlsConfig.MIN_UI_FPS}-$maxUiFps FPS"

    /** Returns a copy with the speed limit set by the slider at [newValue]. */
    fun updateEventThrottling(newValue: Int): SimulationControlsState = copy(
        simulationEventThrottling = simulationEventThrottling.update(newValue),
    )
}
