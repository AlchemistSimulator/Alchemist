/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistSimulations.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ControlDialogState
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.withContext

/**
 * What the engine callbacks share: publishing to the [store] what the [simulation] does,
 * running code on the simulation thread, and refusing commands once the simulation has ended.
 * Every command wakes up the [throttle] first, so it never waits for the pause between events.
 */
internal class EngineBridge<T, P : Position<P>>(
    val simulation: Simulation<T, P>,
    private val store: ComposeUiStateStore,
    private val throttle: SimulationThrottle,
) {
    /**
     * Runs [action] unless the simulation has ended, like [whileActive],
     * but returns a failure instead of showing a dialog.
     */
    suspend fun ifActive(action: suspend () -> Result<Unit>): Result<Unit> {
        throttle.wakeUp()
        return when (simulation.state) {
            SimulationState.TERMINATED -> Result.failure(IllegalStateException(ENDED_MESSAGE))
            else -> action()
        }
    }

    /**
     * Runs [action] on the simulation thread, between two steps, and returns how it went.
     * A failure does not stop the simulation.
     */
    suspend fun onSimulationThread(action: () -> Unit): Result<Unit> {
        val outcome = CompletableDeferred<Result<Unit>>()
        simulation.schedule { outcome.complete(runCatching(action)) }
        return outcome.await()
    }

    /**
     * Runs [query] on the simulation thread, between two steps, and returns its result.
     * Once the simulation has ended, nothing changes the environment, so it runs right away.
     */
    suspend fun <R> read(query: Environment<T, P>.() -> R): R = when (simulation.state) {
        SimulationState.TERMINATED -> simulation.environment.query()
        else -> {
            val result = CompletableDeferred<R>()
            simulation.schedule { result.completeWith(runCatching { simulation.environment.query() }) }
            result.await()
        }
    }

    /** Shows an error dialog. */
    suspend fun showDialog(title: String, message: String) = updateState {
        it.copy(controls = it.controls.copy(dialog = ControlDialogState(title, message)))
    }

    /**
     * Publishes the simulation status, time and step, and closes the dialog.
     * With [refreshScene], it also takes a new snapshot of the nodes.
     */
    suspend fun syncSimulationState(refreshScene: Boolean = false) = updateState {
        val scene = when {
            refreshScene ->
                simulation.environment
                    .toViewport(renderLinks = it.scene.showLinks, inspected = it.selectedNodeIds)
                    .copy(showLinks = it.scene.showLinks)
            else -> it.scene
        }
        it.copy(
            scene = scene,
            controls = it.controls.copy(
                status = simulation.toSimulationStatus(),
                time = simulation.currentTime.toDouble(),
                step = simulation.step,
                dialog = null,
            ),
        ).withSelection(it.selectedNodeIds)
    }

    /** Changes the UI state on the UI thread. */
    suspend fun updateState(transform: (AlchemistUiState) -> AlchemistUiState) {
        withContext(Dispatchers.Main.immediate) {
            store.update(transform)
        }
    }

    /**
     * Runs [action] unless the simulation has ended: a terminated engine rejects every command,
     * so the user gets a dialog instead.
     */
    suspend fun whileActive(action: suspend () -> Unit) {
        throttle.wakeUp()
        if (simulation.state == SimulationState.TERMINATED) {
            showDialog("Simulation ended", ENDED_MESSAGE)
        } else {
            action()
        }
    }

    private companion object {
        const val ENDED_MESSAGE = "The simulation has terminated: it can no longer run, jump, or change."
    }
}
