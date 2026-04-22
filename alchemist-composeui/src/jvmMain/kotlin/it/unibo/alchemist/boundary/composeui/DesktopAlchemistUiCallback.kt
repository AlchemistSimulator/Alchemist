/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.adapter.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ControlDialogState
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.times.DoubleTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class DesktopAlchemistUiCallback<T, P : Position<P>>(
    private val simulation: Simulation<T, P>,
    private val store: ComposeUiStateStore,
) : AlchemistUiCallbacks {
    override suspend fun onPlay() {
        simulation.play().await()
        updateState {
            it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
        }
    }

    override suspend fun onPause() {
        simulation.pause().await()
        updateState {
            it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
        }
    }

    override suspend fun onStep() {
        val nextStep = simulation.step + 1
        val stepCompletion = simulation.goToStep(nextStep)
        simulation.play().await()
        stepCompletion.await()
        syncSimulationState()
    }

    override suspend fun onToTimeInputChanged(value: String) {
        updateState {
            it.copy(controls = it.controls.copy(toTimeInput = value))
        }
    }

    override suspend fun onToTimeSubmit() {
        val currentState = store.state.controls
        val target = currentState.toTimeInput.toDoubleOrNull()
            ?: return showDialog("Invalid time", "Insert a valid numeric time.")
        val currentTime = simulation.time.toDouble()
        val wasRunning = simulation.status == Status.RUNNING
        if (target < currentTime) {
            return showDialog(
                title = "Invalid time",
                message = "Target time $target cannot be lower than current time ${currentTime.formatFixed(DISPLAYED_TIME_DECIMALS)}.",
            )
        }
        if (target > currentTime) {
            val jumpCompletion = simulation.goToTime(DoubleTime(target))
            simulation.play().await()
            jumpCompletion.await()
            if (wasRunning) {
                simulation.play().await()
            }
        }
        syncSimulationState()
    }

    override suspend fun onToStepInputChanged(value: String) {
        updateState {
            it.copy(controls = it.controls.copy(toStepInput = value))
        }
    }

    override suspend fun onToStepSubmit() {
        val currentState = store.state.controls
        val target = currentState.toStepInput.toLongOrNull()
            ?: return showDialog("Invalid step", "Insert a valid integer step.")
        val wasRunning = simulation.status == Status.RUNNING
        if (target < simulation.step) {
            return showDialog(
                title = "Invalid step",
                message = "Target step $target cannot be lower than current step ${simulation.step}.",
            )
        }
        if (target > simulation.step) {
            val jumpCompletion = simulation.goToStep(target)
            simulation.play().await()
            jumpCompletion.await()
            if (wasRunning) {
                simulation.play().await()
            }
        }
        syncSimulationState()
    }

    override suspend fun onFpsInputChanged(value: String) {
        updateState {
            it.copy(controls = it.controls.copy(fpsInput = value))
        }
    }

    override suspend fun onFpsSubmit() {
        val currentState = store.state.controls
        val target = currentState.fpsInput.toIntOrNull()
            ?: return showDialog("Invalid FPS", "Insert a valid integer FPS value.")
        updateState {
            it.copy(controls = it.controls.withUiFps(target))
        }
    }

    override suspend fun onEventRateChanged(value: Float) {
        updateState {
            it.copy(controls = it.controls.updateEventThrottling(value.roundToInt()))
        }
    }

    override suspend fun onNodeSelected(nodeId: Int) {
        updateState { currentState ->
            currentState.withSelection(listOf(nodeId))
        }
    }

    override suspend fun onNodesSelected(nodeIds: List<Int>) {
        updateState { currentState ->
            currentState.withSelection(nodeIds)
        }
    }

    override suspend fun onInspectorDismiss() {
        updateState {
            it.withSelection(emptyList())
        }
    }

    override suspend fun onToggleLinks() {
        updateState {
            it.copy(
                scene = it.scene.copy(showLinks = !it.scene.showLinks),
            )
        }
    }

    override suspend fun onDialogDismiss() {
        updateState {
            it.copy(controls = it.controls.copy(dialog = null))
        }
    }

    private suspend fun updateState(transform: (AlchemistUiState) -> AlchemistUiState) {
        withContext(Dispatchers.Main.immediate) {
            store.update(transform)
        }
    }

    private suspend fun syncSimulationState() {
        updateState {
            it.copy(
                controls = it.controls.copy(
                    status = simulation.toSimulationStatus(),
                    timeLabel = simulation.time.toDouble().formatFixed(DISPLAYED_TIME_DECIMALS),
                    step = simulation.step,
                    dialog = null,
                ),
            )
        }
    }

    private suspend fun showDialog(title: String, message: String) {
        updateState {
            it.copy(
                controls = it.controls.copy(dialog = ControlDialogState(title, message)),
            )
        }
    }
}
