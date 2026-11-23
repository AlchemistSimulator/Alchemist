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
import it.unibo.alchemist.boundary.composeui.adapter.AlchemistSimulations.toSimulationStatus
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatFixed
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.await

/**
 * Runs, pauses and jumps the simulation of [bridge].
 * The latest command wins: playing or pausing drops the pending jumps, and of several jumps the first one reached wins.
 */
internal class EngineTransport<T, P : Position<P>>(private val bridge: EngineBridge<T, P>) :
    AlchemistUiCallbacks.Transport {
    private val simulation = bridge.simulation
    private val stops = JumpStops(simulation).also(simulation::addOutputMonitor)

    override suspend fun play() = bridge.whileActive {
        stops.clear()
        simulation.play().await()
        publishStatus()
    }

    override suspend fun pause() = bridge.whileActive {
        stops.clear()
        simulation.pause().await()
        publishStatus()
    }

    override suspend fun step() = bridge.whileActive { jumpToStep(simulation.step + 1) }

    override suspend fun runToTime(target: Double) = bridge.whileActive { jumpToTime(target) }

    override suspend fun runToStep(target: Long) = bridge.whileActive { jumpToStep(target) }

    override suspend fun jumpTimeBy(delta: Double) =
        bridge.whileActive { jumpToTime(simulation.currentTime.toDouble() + delta) }

    override suspend fun jumpStepBy(delta: Long) = bridge.whileActive { jumpToStep(simulation.step + delta) }

    private suspend fun jumpToStep(target: Long) = when {
        target < simulation.step -> bridge.showDialog(
            title = "Invalid step",
            message = "Target step $target cannot be lower than current step ${simulation.step}.",
        )
        else -> {
            if (target > simulation.step) {
                runUntil(stops.atStep(target))
            }
            bridge.syncSimulationState()
        }
    }

    private suspend fun jumpToTime(target: Double) {
        val currentTime = simulation.currentTime.toDouble()
        when {
            target < currentTime -> bridge.showDialog(
                title = "Invalid time",
                message = "Target time $target cannot be lower than current time ${
                    currentTime.formatFixed(DISPLAYED_TIME_DECIMALS)
                }.",
            )
            else -> {
                if (target > currentTime) {
                    runUntil(stops.atTime(target))
                }
                bridge.syncSimulationState()
            }
        }
    }

    private suspend fun publishStatus() = bridge.updateState {
        it.copy(controls = it.controls.copy(status = simulation.toSimulationStatus()))
    }

    /** Runs until [stopped] completes: at a stop, or when a later command takes over. */
    private suspend fun runUntil(stopped: Deferred<Unit>) {
        simulation.play().await()
        stopped.await()
    }
}
