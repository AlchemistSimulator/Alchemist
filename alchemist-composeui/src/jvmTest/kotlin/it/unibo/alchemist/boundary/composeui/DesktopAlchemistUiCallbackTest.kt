/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.util.TestSimulations
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.onFirstStep
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.runningInBackground
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class DesktopAlchemistUiCallbackTest {
    @Test
    fun `to step pauses at the target if the simulation was not running`() {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            store.update { it.copy(controls = it.controls.copy(toStepInput = "5")) }
            runBlocking { callback.onToStepSubmit() }
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertEquals(5L, simulation.step)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
            assertEquals(5L, store.state.controls.step)
        }
    }

    @Test
    fun `to step resumes the simulation if it was running`() {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val jump = simulation.jumpWhileRunning(store) { controls, current ->
            controls.copy(toStepInput = (current.step + 4).toString())
        }
        simulation.runningInBackground {
            simulation.play()
            jump.join()
            val target = store.state.controls.toStepInput.toLong()
            assertNull(store.state.controls.dialog)
            assertEquals(SimulationState.RUNNING, simulation.state)
            assertTrue(simulation.step >= target)
            assertEquals(SimulationStatus.RUNNING, store.state.controls.status)
            assertTrue(store.state.controls.step >= target)
        }
    }

    @Test
    fun `to time resumes the simulation if it was running`() {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val jump = simulation.jumpWhileRunning(store, submit = { onToTimeSubmit() }) { controls, current ->
            controls.copy(toTimeInput = (current.currentTime.toDouble() + 3.5).toString())
        }
        simulation.runningInBackground {
            simulation.play()
            jump.join()
            val target = store.state.controls.toTimeInput.toDouble()
            assertEquals(SimulationState.RUNNING, simulation.state)
            assertTrue(simulation.currentTime.toDouble() >= target)
            assertEquals(SimulationStatus.RUNNING, store.state.controls.status)
            assertTrue(store.state.controls.timeLabel.toDouble() >= target)
        }
    }

    private companion object {
        /**
         * Prepares a jump issued while the simulation is running.
         * Right after the first step, on the simulation thread, the jump target is written in the controls through
         * [target], and [submit] starts on the callback.
         * The coroutine runs synchronously up to its first suspension, so the callback reads the simulation state and
         * registers the jump before the simulation can make progress.
         * The returned future completes when the callback does.
         */
        fun Simulation<Any, Euclidean2DPosition>.jumpWhileRunning(
            store: ComposeUiStateStore,
            submit: suspend DesktopAlchemistUiCallback<Any, Euclidean2DPosition>.() -> Unit = { onToStepSubmit() },
            target: (SimulationControlsState, Simulation<Any, Euclidean2DPosition>) -> SimulationControlsState,
        ): CompletableFuture<Unit> {
            val callback = DesktopAlchemistUiCallback(this, store)
            val jump = CompletableFuture<Unit>()
            onFirstStep { simulation ->
                store.update { it.copy(controls = target(it.controls, simulation)) }
                suspend { callback.submit() }.startCoroutine(
                    Continuation(EmptyCoroutineContext) { result ->
                        result.fold(jump::complete, jump::completeExceptionally)
                    },
                )
            }
            return jump
        }
    }
}
