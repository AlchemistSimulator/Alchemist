/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.toSimulationStatus
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext

class DesktopAlchemistUiCallback<T, P : Position<P>>(
    private val simulation: Simulation<T, P>,
    private val store: ComposeUiStateStore
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
        updateState {
            it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
        }
    }

    override suspend fun onNodeSelected(nodeId: Int) {
        updateState { currentState ->
            val node = currentState.scene.nodes.firstOrNull { it.id == nodeId } ?: return@updateState currentState
            currentState.copy(
                selectedNodeId = nodeId,
                inspector = node.toInspectorState(),
            )
        }
    }

    override suspend fun onInspectorDismiss() {
        updateState {
            it.copy(
                selectedNodeId = null,
                inspector = null,
            )
        }
    }

    override suspend fun onToggleLinks() {
        updateState {
            it.copy(
                scene = it.scene.copy(showLinks = !it.scene.showLinks),
            )
        }
    }

    private suspend fun updateState(transform: (AlchemistUiState) -> AlchemistUiState) {
        withContext(Dispatchers.Main.immediate) {
            store.update(transform)
        }
    }
}
