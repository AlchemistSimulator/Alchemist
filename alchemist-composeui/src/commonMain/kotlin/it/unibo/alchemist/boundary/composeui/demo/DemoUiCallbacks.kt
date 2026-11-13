/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.DISPLAYED_TIME_DECIMALS
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationProgress
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Fake simulation controls for the demo scene, used when no simulation is attached.
 */
internal class DemoUiCallbacks(private val store: ComposeUiStateStore) : AlchemistUiCallbacks {
    override suspend fun onPlay() {
        store.update {
            it.copy(
                controls = it.controls.copy(status = SimulationStatus.RUNNING),
            )
        }
    }

    override suspend fun onPause() {
        store.update {
            it.copy(
                controls = it.controls.copy(status = SimulationStatus.PAUSED),
            )
        }
    }

    override suspend fun onStep() {
        store.update {
            val nextStep = it.controls.step + 1
            it.withStepProgress(nextStep)
        }
    }

    override suspend fun onToTimeInputChanged(value: String) {
        store.update {
            it.copy(controls = it.controls.copy(toTimeInput = value))
        }
    }

    override suspend fun onToTimeSubmit() {
        store.update { state ->
            val target = state.controls.toTimeInput.toDoubleOrNull()
                ?: return@update state.withDialog("Invalid time", "Insert a valid numeric time.")
            val currentTime = state.controls.timeLabel.toDoubleOrNull() ?: 0.0
            val wasRunning = state.controls.status == SimulationStatus.RUNNING
            if (target < currentTime) {
                return@update state.withDialog(
                    title = "Invalid time",
                    message = "Target time $target cannot be lower than current time $currentTime.",
                )
            }
            val targetStep = ceil(target * DemoUiState.TIME_SCALE).toLong()
            state.withStepProgress(
                targetStep,
                timeLabel = target.formatFixed(DISPLAYED_TIME_DECIMALS),
                status = if (wasRunning) SimulationStatus.RUNNING else SimulationStatus.PAUSED,
            )
        }
    }

    override suspend fun onToStepInputChanged(value: String) {
        store.update {
            it.copy(controls = it.controls.copy(toStepInput = value))
        }
    }

    override suspend fun onToStepSubmit() {
        store.update { state ->
            val target = state.controls.toStepInput.toLongOrNull()
                ?: return@update state.withDialog("Invalid step", "Insert a valid integer step.")
            val wasRunning = state.controls.status == SimulationStatus.RUNNING
            if (target < state.controls.step) {
                return@update state.withDialog(
                    title = "Invalid step",
                    message = "Target step $target cannot be lower than current step ${state.controls.step}.",
                )
            }
            state.withStepProgress(
                target,
                status = if (wasRunning) SimulationStatus.RUNNING else SimulationStatus.PAUSED,
            )
        }
    }

    override suspend fun onFpsInputChanged(value: String) {
        store.update {
            it.copy(controls = it.controls.copy(fpsInput = value))
        }
    }

    override suspend fun onFpsSubmit() {
        store.update { state ->
            val target = state.controls.fpsInput.toIntOrNull()
                ?: return@update state.withDialog("Invalid FPS", "Insert a valid integer FPS value.")
            state.copy(controls = state.controls.withUiFps(target))
        }
    }

    override suspend fun onEventRateChanged(value: Float) {
        store.update { state ->
            state.copy(controls = state.controls.updateEventThrottling(value.roundToInt()))
        }
    }

    override suspend fun onNodeSelected(nodeId: Int) {
        store.update {
            it.withSelection(listOf(nodeId))
        }
    }

    override suspend fun onNodesSelected(nodeIds: List<Int>) {
        store.update {
            it.withSelection(nodeIds)
        }
    }

    override suspend fun onNodesMoved(nodePositions: List<NodePositionUpdate>) {
        store.update { currentState ->
            currentState
                .copy(scene = currentState.scene.withMovedNodes(nodePositions))
                .withSelection(currentState.selectedNodeIds)
        }
    }

    override suspend fun onInspectorDismiss() {
        store.update {
            it.withSelection(emptyList())
        }
    }

    override suspend fun onToggleLinks() {
        store.update {
            it.copy(
                scene = it.scene.copy(showLinks = !it.scene.showLinks),
            )
        }
    }

    override suspend fun onDialogDismiss() {
        store.update {
            it.copy(controls = it.controls.copy(dialog = null))
        }
    }

    private fun AlchemistUiState.withStepProgress(
        step: Long,
        timeLabel: String = DemoUiState.timeLabel(step),
        status: SimulationStatus = SimulationStatus.PAUSED,
    ): AlchemistUiState = copy(
        controls = controls.copy(
            status = status,
            step = step,
            timeLabel = timeLabel,
            progress = SimulationProgress(
                fraction = (step % 100).toFloat() / 100f,
                label = "Scenario exploration",
            ),
            dialog = null,
        ),
    )
}
