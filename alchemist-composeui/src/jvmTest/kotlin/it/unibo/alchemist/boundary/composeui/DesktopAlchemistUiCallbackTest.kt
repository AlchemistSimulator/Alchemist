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
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.util.TestSimulations
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.onFirstStep
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.runningInBackground
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class DesktopAlchemistUiCallbackTest {
    @Test
    fun `to step pauses at the target if the simulation was not running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            store.update { it.copy(controls = it.controls.copy(toStepInput = "5")) }
            callback.onToStepSubmit()
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertEquals(5L, simulation.step)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
            assertEquals(5L, store.state.controls.step)
        }
    }

    @Test
    fun `to step with an empty box advances one step`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            callback.onToStepSubmit()
            assertEquals(1L, simulation.step)
            assertEquals(1L, store.state.controls.step)
        }
    }

    @Test
    fun `step quick jumps run ahead from the current step`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            callback.onJumpStepBy(3)
            callback.onJumpStepBy(4)
            assertEquals(7L, simulation.step)
            assertEquals(7L, store.state.controls.step)
        }
    }

    @Test
    fun `to step pauses at the target even if the simulation was running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            {
                store.update { it.copy(controls = it.controls.copy(toStepInput = (simulation.step + 4).toString())) }
                onToStepSubmit()
            },
        )
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            val target = store.state.controls.toStepInput.toLong()
            assertNull(store.state.controls.dialog)
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertEquals(target, simulation.step)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
            assertEquals(target, store.state.controls.step)
        }
    }

    @Test
    fun `to time pauses at the target even if the simulation was running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            {
                val target = simulation.currentTime.toDouble() + 3.5
                store.update { it.copy(controls = it.controls.copy(toTimeInput = target.toString())) }
                onToTimeSubmit()
            },
        )
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            val target = store.state.controls.toTimeInput.toDouble()
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertTrue(simulation.currentTime.toDouble() >= target)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
            assertTrue(store.state.controls.time >= target)
        }
    }

    @Test
    fun `of two jumps the first one reached wins`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        val start = CompletableDeferred<Long>()
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            {
                start.complete(simulation.step)
                onJumpStepBy(1000)
            },
            { onJumpStepBy(3) },
        )
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            assertEquals(SimulationState.PAUSED, simulation.state)
            val target = start.await() + 3
            assertEquals(target, simulation.step)
            assertEquals(target, store.state.controls.step)
        }
    }

    @Test
    fun `pause drops a pending jump`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        val commands = simulation.issueOnFirstStep(this, callback, { onJumpStepBy(1000) }, { onPause() })
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertTrue(simulation.step < 1000)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
        }
    }

    @Test
    fun `commands after the end show a dialog instead of failing`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            simulation.terminate().await()
            store.update { it.copy(controls = it.controls.copy(toStepInput = "5")) }
            callback.onToStepSubmit()
            assertEquals("Simulation ended", store.state.controls.dialog?.title)
        }
    }

    private companion object {
        /**
         * Starts [commands] on [callback] in [scope], in order, right after the first step, on the simulation thread.
         * Each command runs synchronously up to its first suspension,
         * so it reads the simulation state and issues its engine commands before the simulation can make progress.
         * The result holds one [Deferred] per command, completing when the command does.
         */
        fun Simulation<Any, Euclidean2DPosition>.issueOnFirstStep(
            scope: CoroutineScope,
            callback: DesktopAlchemistUiCallback<Any, Euclidean2DPosition>,
            vararg commands: suspend DesktopAlchemistUiCallback<Any, Euclidean2DPosition>.() -> Unit,
        ): List<Deferred<Unit>> {
            val completions = commands.map { CompletableDeferred<Unit>() }
            onFirstStep {
                commands.zip(completions).forEach { (command, completion) ->
                    scope.launch(start = CoroutineStart.UNDISPATCHED) {
                        callback.command()
                        completion.complete(Unit)
                    }
                }
            }
            return completions
        }
    }
}
