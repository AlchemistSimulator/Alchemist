/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.SimulationControlsConfig.MAX_UI_FPS
import it.unibo.alchemist.boundary.composeui.demo.DemoUiCallbacks
import it.unibo.alchemist.boundary.composeui.demo.DemoUiState
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.InspectorState.NodeGroup
import it.unibo.alchemist.boundary.composeui.model.QuickJumps
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.throttling.EventsPerSecond
import it.unibo.alchemist.boundary.composeui.model.throttling.FullThrottle
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.MAX_SIMULATION_EVENTS_PER_SECOND
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.MAX_SLIDER_POSITION
import it.unibo.alchemist.boundary.composeui.model.throttling.SimulationEventThrottling.Companion.rateAt
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.math.log10
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

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
    fun `demo controller toggles links without changing selection`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        controller.store.update { it.withSelection(listOf(3)) }

        controller.callbacks.onToggleLinks()

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
    fun `a typed fps cap applies at once only if allowed`() {
        val controls = SimulationControlsState(uiFps = 30)
        assertEquals(12, controls.withFpsInput("12").uiFps)
        assertEquals(30, controls.withFpsInput("").uiFps)
        assertEquals(30, controls.withFpsInput("500").uiFps)
        assertEquals("500", controls.withFpsInput("500").fpsInput)
    }

    @Test
    fun `full throttle is represented by the max slider value`() {
        val controls = SimulationControlsState().updateEventThrottling(Int.MAX_VALUE)

        assertTrue(controls.isFullThrottle)
        assertEquals(FullThrottle, controls.simulationEventThrottling)
        assertEquals("Max", controls.simulationEventThrottling.toLabel())
    }

    @Test
    fun `the speed slider spans one to the highest rate`() {
        assertEquals(1, rateAt(0f))
        assertEquals(MAX_SIMULATION_EVENTS_PER_SECOND, rateAt(MAX_SLIDER_POSITION))
        val atTop = SimulationControlsState().updateEventThrottling(rateAt(MAX_SLIDER_POSITION))
        assertEquals(FullThrottle, atTop.simulationEventThrottling)
    }

    @Test
    fun `the speed slider is logarithmic and rounds to two significant digits`() {
        assertEquals(100, rateAt(2f))
        assertEquals(1200, rateAt(log10(1234f)))
        assertEquals(2f, EventsPerSecond(100).sliderPosition)
    }

    @Test
    fun `speed labels show their unit`() {
        assertEquals("10 events/s", EventsPerSecond(10).toLabel())
        assertEquals("1.2K events/s", EventsPerSecond(1200).toLabel())
        assertEquals("10K events/s", EventsPerSecond(10_000).toLabel())
        assertEquals("1M events/s", EventsPerSecond(1_000_000).toLabel())
    }

    @Test
    fun `empty jump boxes target the next step and one time unit ahead`() {
        val controls = SimulationControlsState(step = 7, time = 2.5)
        assertEquals(8L, controls.targetStep)
        assertEquals(3.5, controls.targetTime)
    }

    @Test
    fun `typed jump targets win over the defaults`() {
        val controls = SimulationControlsState(step = 7, time = 2.5, toStepInput = "20", toTimeInput = "x")
        assertEquals(20L, controls.targetStep)
        assertNull(controls.targetTime)
    }

    @Test
    fun `demo controller advances one step when to step is empty`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        val start = controller.store.state.controls.step

        controller.callbacks.onToStepSubmit()

        assertEquals(start + 1, controller.store.state.controls.step)
    }

    @Test
    fun `time quick jumps go from ten to a thousand`() {
        assertEquals(listOf(10.0, 100.0, 1000.0), QuickJumps.timeDeltas)
    }

    @Test
    fun `step quick jumps scale with the node count`() {
        assertEquals(listOf(100L, 200L, 1000L), QuickJumps.stepDeltas(100))
        assertEquals(listOf(2L, 10L), QuickJumps.stepDeltas(1))
        assertEquals(emptyList(), QuickJumps.stepDeltas(0))
    }

    @Test
    fun `demo controller quick jumps run ahead from the current step and time`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        val start = controller.store.state.controls

        controller.callbacks.onJumpStepBy(10)
        assertEquals(start.step + 10, controller.store.state.controls.step)

        val afterSteps = controller.store.state.controls.time
        controller.callbacks.onJumpTimeBy(100.0)
        assertEquals(afterSteps + 100.0, controller.store.state.controls.time)
    }

    @Test
    fun `demo controller rejects backward time jumps`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.callbacks.onToTimeInputChanged("1.5")
        controller.callbacks.onToTimeSubmit()

        val dialog = controller.store.state.controls.dialog
        assertNotNull(dialog)
        assertEquals("Invalid time", dialog.title)
    }

    @Test
    fun `demo controller updates fps on submit`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.callbacks.onFpsInputChanged("120")
        controller.callbacks.onFpsSubmit()

        assertEquals(MAX_UI_FPS, controller.store.state.controls.uiFps)
        assertEquals(MAX_UI_FPS.toString(), controller.store.state.controls.fpsInput)
    }

    @Test
    fun `demo controller pauses at the jump target even if running`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        controller.store.update {
            it.copy(
                controls = it.controls.copy(status = SimulationStatus.RUNNING),
            )
        }

        controller.callbacks.onToStepInputChanged("99")
        controller.callbacks.onToStepSubmit()

        assertEquals(SimulationStatus.PAUSED, controller.store.state.controls.status)
        assertEquals(99L, controller.store.state.controls.step)
    }

    @Test
    fun `demo controller builds a group inspector for multi selection`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.callbacks.onNodesSelected(listOf(1, 3))

        assertEquals(listOf(1, 3), controller.store.state.selectedNodeIds)
        val inspector = controller.store.state.inspector as NodeGroup
        assertEquals(listOf(1, 3), inspector.nodeIds)
        assertEquals("Mixed", inspector.concentrations.first { it.label == "signal" }.value)
    }
}
