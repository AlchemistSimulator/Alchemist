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
import it.unibo.alchemist.boundary.composeui.model.GroupInspectorState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SimulationControlsStateTest {
    @Test
    fun `play is available when ready or paused`() {
        assertTrue(SimulationControlsState(status = SimulationStatus.READY).canPlay)
        assertTrue(SimulationControlsState(status = SimulationStatus.PAUSED).canPlay)
        assertFalse(SimulationControlsState(status = SimulationStatus.RUNNING).canPlay)
    }

    @Test
    fun `pause is only available while running`() {
        assertTrue(SimulationControlsState(status = SimulationStatus.RUNNING).canPause)
        assertFalse(SimulationControlsState(status = SimulationStatus.PAUSED).canPause)
        assertFalse(SimulationControlsState(status = SimulationStatus.TERMINATED).canPause)
    }

    @Test
    fun `step is available only in controllable idle states`() {
        assertTrue(SimulationControlsState(status = SimulationStatus.READY).canStep)
        assertTrue(SimulationControlsState(status = SimulationStatus.PAUSED).canStep)
        assertFalse(SimulationControlsState(status = SimulationStatus.RUNNING).canStep)
        assertFalse(SimulationControlsState(status = SimulationStatus.TERMINATED).canStep)
    }

    @Test
    fun `store updates atomically`() {
        val store = ComposeUiStateStore(AlchemistUiState())
        store.update {
            it.copy(
                controls = it.controls.copy(step = 7L),
            )
        }
        assertEquals(7L, store.state.controls.step)
    }

    @Test
    fun `viewport links are hidden by default`() {
        assertFalse(ViewportScene().showLinks)
    }

    @Test
    fun `demo controller toggles links without changing selection`() {
        val controller = demoController()
        controller.store.update { it.withSelection(listOf(3)) }

        runSuspend { controller.callbacks.onToggleLinks() }

        assertTrue(controller.store.state.scene.showLinks)
        assertEquals(listOf(3), controller.store.state.selectedNodeIds)
    }

    @Test
    fun `ui fps is clamped to the configured range`() {
        val controls = SimulationControlsState(maxUiFps = 45).withUiFps(120)

        assertEquals(45, controls.uiFps)
        assertEquals("45", controls.fpsInput)
    }

    @Test
    fun `full throttle is represented by the max slider value`() {
        val controls =
            SimulationControlsState(maxEventRateSliderValue = 50).withEventRateSliderValue(50)

        assertTrue(controls.isFullThrottle)
        assertNull(controls.effectiveEventsPerSecond)
        assertEquals(FULL_THROTTLE_LABEL, controls.eventRateLabel)
    }

    @Test
    fun `demo controller rejects backward time jumps`() {
        val controller = demoController()

        runSuspend {
            controller.callbacks.onToTimeInputChanged("1.5")
            controller.callbacks.onToTimeSubmit()
        }

        val dialog = controller.store.state.controls.dialog
        assertNotNull(dialog)
        assertEquals("Invalid time", dialog.title)
    }

    @Test
    fun `demo controller updates fps on submit`() {
        val controller = demoController()

        runSuspend {
            controller.callbacks.onFpsInputChanged("120")
            controller.callbacks.onFpsSubmit()
        }

        assertEquals(DEFAULT_MAX_UI_FPS, controller.store.state.controls.uiFps)
        assertEquals(DEFAULT_MAX_UI_FPS.toString(), controller.store.state.controls.fpsInput)
    }

    @Test
    fun `demo controller preserves running state after jump`() {
        val controller = demoController()
        controller.store.update {
            it.copy(
                controls = it.controls.copy(status = SimulationStatus.RUNNING),
            )
        }

        runSuspend {
            controller.callbacks.onToStepInputChanged("99")
            controller.callbacks.onToStepSubmit()
        }

        assertEquals(SimulationStatus.RUNNING, controller.store.state.controls.status)
        assertEquals(99L, controller.store.state.controls.step)
    }

    @Test
    fun `demo controller builds a group inspector for multi selection`() {
        val controller = demoController()

        runSuspend {
            controller.callbacks.onNodesSelected(listOf(1, 3))
        }

        assertEquals(listOf(1, 3), controller.store.state.selectedNodeIds)
        val inspector = controller.store.state.inspector as GroupInspectorState
        assertEquals(listOf(1, 3), inspector.nodeIds)
        assertEquals("Mixed", inspector.concentrations.first { it.label == "signal" }.value)
    }
}

private fun runSuspend(block: suspend () -> Unit) {
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                result.getOrThrow()
            }
        },
    )
}
