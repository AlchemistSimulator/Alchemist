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

class EngineUiCallbacksTest {
    @Test
    fun `to step pauses at the target if the simulation was not running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            callback.transport.runToStep(5)
            assertEquals(SimulationState.PAUSED, simulation.state)
            assertEquals(5L, simulation.step)
            assertEquals(SimulationStatus.PAUSED, store.state.controls.status)
            assertEquals(5L, store.state.controls.step)
        }
    }

    @Test
    fun `step advances one step`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            callback.transport.step()
            assertEquals(1L, simulation.step)
            assertEquals(1L, store.state.controls.step)
        }
    }

    @Test
    fun `step quick jumps run ahead from the current step`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            callback.transport.jumpStepBy(3)
            callback.transport.jumpStepBy(4)
            assertEquals(7L, simulation.step)
            assertEquals(7L, store.state.controls.step)
        }
    }

    @Test
    fun `to step pauses at the target even if the simulation was running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        val requested = CompletableDeferred<Long>()
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            { transport.runToStep((simulation.step + 4).also(requested::complete)) },
        )
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            val target = requested.await()
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
        val callback = EngineUiCallbacks(simulation, store)
        val requested = CompletableDeferred<Double>()
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            { transport.runToTime((simulation.currentTime.toDouble() + 3.5).also(requested::complete)) },
        )
        simulation.runningInBackground {
            simulation.play()
            commands.awaitAll()
            val target = requested.await()
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
        val callback = EngineUiCallbacks(simulation, store)
        val start = CompletableDeferred<Long>()
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            {
                start.complete(simulation.step)
                transport.jumpStepBy(1000)
            },
            { transport.jumpStepBy(3) },
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
        val callback = EngineUiCallbacks(simulation, store)
        val commands = simulation.issueOnFirstStep(
            this,
            callback,
            { transport.jumpStepBy(1000) },
            { transport.pause() },
        )
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
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            simulation.terminate().await()
            callback.transport.runToStep(5)
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
            callback: EngineUiCallbacks<Any, Euclidean2DPosition>,
            vararg commands: suspend EngineUiCallbacks<Any, Euclidean2DPosition>.() -> Unit,
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
