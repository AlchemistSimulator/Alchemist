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
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistSimulations.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ControlDialogState
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Position
import kotlin.math.roundToInt
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext

/**
 * UI callbacks that drive a [simulation] running in the same JVM and publish the resulting UI state to [store].
 * Every command wakes up the [throttle] first, so it never waits for the pause between events.
 * The latest command wins: playing or pausing drops the pending jumps, and of several jumps the first one reached wins.
 */
internal class DesktopAlchemistUiCallback<T, P : Position<P>>(
    private val simulation: Simulation<T, P>,
    private val store: ComposeUiStateStore,
    private val throttle: SimulationThrottle = SimulationThrottle(),
) : AlchemistUiCallbacks {
    private val stops = JumpStops(simulation).also(simulation::addOutputMonitor)

    override suspend fun onPlay() {
        whileActive {
            stops.clear()
            simulation.play().await()
            updateState {
                it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
            }
        }
    }

    override suspend fun onPause() {
        whileActive {
            stops.clear()
            simulation.pause().await()
            updateState {
                it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
            }
        }
    }

    override suspend fun onStep() {
        whileActive { jumpToStep(simulation.step + 1) }
    }

    override suspend fun onToTimeInputChanged(value: String) {
        updateState {
            it.copy(controls = it.controls.copy(toTimeInput = value))
        }
    }

    override suspend fun onToTimeSubmit() {
        whileActive {
            store.state.controls.targetTime?.let { jumpToTime(it) }
                ?: showDialog("Invalid time", "Insert a valid numeric time.")
        }
    }

    override suspend fun onToStepInputChanged(value: String) {
        updateState {
            it.copy(controls = it.controls.copy(toStepInput = value))
        }
    }

    override suspend fun onToStepSubmit() {
        whileActive {
            store.state.controls.targetStep?.let { jumpToStep(it) }
                ?: showDialog("Invalid step", "Insert a valid integer step.")
        }
    }

    override suspend fun onJumpTimeBy(delta: Double) {
        whileActive { jumpToTime(simulation.currentTime.toDouble() + delta) }
    }

    override suspend fun onJumpStepBy(delta: Long) {
        whileActive { jumpToStep(simulation.step + delta) }
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
        throttle.wakeUp()
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

    override suspend fun onNodesMoved(nodePositions: List<NodePositionUpdate>) {
        whileActive {
            if (nodePositions.isEmpty()) {
                return@whileActive
            }
            val environment = simulation.environment
            val completion = CompletableDeferred<Unit>()
            simulation.schedule {
                runCatching {
                    nodePositions.forEach { nodePosition ->
                        val node = environment.nodeWithId(nodePosition.nodeId)
                        val newPosition = environment.makePosition(nodePosition.coordinates)
                        environment.moveNodeTo(node, newPosition)
                    }
                }.onSuccess {
                    completion.complete(Unit)
                }.onFailure { error ->
                    completion.completeExceptionally(error)
                    throw error
                }
            }
            completion.await()
            syncSimulationState(refreshScene = true)
        }
    }

    override suspend fun onInspectorDismiss() {
        updateState {
            it.withSelection(emptyList())
        }
    }

    override suspend fun onToggleLinks() {
        updateState {
            val nextShowLinks = !it.scene.showLinks
            it.copy(
                scene = simulation.environment
                    .toViewport(renderLinks = nextShowLinks)
                    .copy(showLinks = nextShowLinks),
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

    private suspend fun syncSimulationState(refreshScene: Boolean = false) {
        updateState {
            val nextScene = if (refreshScene) {
                simulation.environment.toViewport(renderLinks = it.scene.showLinks).copy(showLinks = it.scene.showLinks)
            } else {
                it.scene
            }
            it.copy(
                scene = nextScene,
                controls = it.controls.copy(
                    status = simulation.toSimulationStatus(),
                    time = simulation.currentTime.toDouble(),
                    step = simulation.step,
                    dialog = null,
                ),
            ).withSelection(it.selectedNodeIds)
        }
    }

    /**
     * Runs [action] unless the simulation has ended: a terminated engine rejects every command,
     * so the user gets a dialog instead.
     */
    private suspend fun whileActive(action: suspend () -> Unit) {
        throttle.wakeUp()
        if (simulation.state == SimulationState.TERMINATED) {
            showDialog("Simulation ended", "The simulation has terminated: it can no longer run, jump, or change.")
        } else {
            action()
        }
    }

    private suspend fun jumpToStep(target: Long) {
        when {
            target < simulation.step -> showDialog(
                title = "Invalid step",
                message = "Target step $target cannot be lower than current step ${simulation.step}.",
            )
            else -> {
                if (target > simulation.step) {
                    runUntil(stops.atStep(target))
                }
                syncSimulationState()
            }
        }
    }

    private suspend fun jumpToTime(target: Double) {
        val currentTime = simulation.currentTime.toDouble()
        when {
            target < currentTime -> showDialog(
                title = "Invalid time",
                message = "Target time $target cannot be lower than current time ${
                    currentTime.formatFixed(DISPLAYED_TIME_DECIMALS)
                }.",
            )
            else -> {
                if (target > currentTime) {
                    runUntil(stops.atTime(target))
                }
                syncSimulationState()
            }
        }
    }

    /** Runs until [stopped] completes: at a stop, or when a later command takes over. */
    private suspend fun runUntil(stopped: Deferred<Unit>) {
        simulation.play().await()
        stopped.await()
    }

    private suspend fun showDialog(title: String, message: String) {
        updateState {
            it.copy(
                controls = it.controls.copy(dialog = ControlDialogState(title, message)),
            )
        }
    }
}
