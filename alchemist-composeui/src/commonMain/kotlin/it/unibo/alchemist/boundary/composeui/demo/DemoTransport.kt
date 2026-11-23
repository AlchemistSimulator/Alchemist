/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationProgress
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.math.ceil

/**
 * Fake running and jumping for the demo scene: jumps move the counters at once.
 */
internal class DemoTransport(private val store: ComposeUiStateStore) : AlchemistUiCallbacks.Transport {
    override suspend fun play() = store.update {
        it.copy(controls = it.controls.copy(status = SimulationStatus.RUNNING))
    }

    override suspend fun pause() = store.update {
        it.copy(controls = it.controls.copy(status = SimulationStatus.PAUSED))
    }

    override suspend fun step() = store.update { it.withStepProgress(it.controls.step + 1) }

    override suspend fun runToTime(target: Double) = store.update { it.jumpedToTime(target) }

    override suspend fun runToStep(target: Long) = store.update { it.jumpedToStep(target) }

    override suspend fun jumpTimeBy(delta: Double) = store.update { it.jumpedToTime(it.controls.time + delta) }

    override suspend fun jumpStepBy(delta: Long) = store.update { it.jumpedToStep(it.controls.step + delta) }

    private fun AlchemistUiState.jumpedToStep(target: Long): AlchemistUiState = when {
        target < controls.step -> withDialog(
            title = "Invalid step",
            message = "Target step $target cannot be lower than current step ${controls.step}.",
        )
        else -> withStepProgress(target)
    }

    private fun AlchemistUiState.jumpedToTime(target: Double): AlchemistUiState = when {
        target < controls.time -> withDialog(
            title = "Invalid time",
            message = "Target time $target cannot be lower than current time ${controls.time}.",
        )
        else -> withStepProgress(ceil(target * DemoUiState.TIME_SCALE).toLong(), time = target)
    }

    /** Every jump ends paused. */
    private fun AlchemistUiState.withStepProgress(
        step: Long,
        time: Double = DemoUiState.time(step),
    ): AlchemistUiState = copy(
        controls = controls.copy(
            status = SimulationStatus.PAUSED,
            step = step,
            time = time,
            progress = SimulationProgress(
                fraction = (step % 100).toFloat() / 100f,
                label = "Scenario exploration",
            ),
            dialog = null,
        ),
    )
}
