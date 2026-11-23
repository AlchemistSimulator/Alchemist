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
import it.unibo.alchemist.boundary.composeui.model.pacing.Pacing
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatCompact
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

        controller.callbacks.display.toggleLinks()

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
    fun `the simulation starts at max speed`() {
        assertEquals(PacingMode.MAX_SPEED, SimulationControlsState().pacing.mode)
    }

    @Test
    fun `choosing a pacing mode starts from its default rate`() {
        val controls = SimulationControlsState().withPacingMode(PacingMode.EVENTS_PER_SECOND)
        assertEquals(Pacing(PacingMode.EVENTS_PER_SECOND, 1000.0), controls.pacing)
        assertEquals("1000", controls.pacingInput)
    }

    @Test
    fun `a typed pacing rate applies at once only if positive`() {
        val controls = SimulationControlsState().withPacingMode(PacingMode.TIME_PER_SECOND)
        assertEquals(2.5, controls.withPacingInput("2.5").pacing.rate)
        assertEquals(1.0, controls.withPacingInput("0").pacing.rate)
        assertEquals(1.0, controls.withPacingInput("x").pacing.rate)
        assertEquals("x", controls.withPacingInput("x").pacingInput)
    }

    @Test
    fun `events per second pace events, time per second paces simulated time`() {
        assertEquals(0.5, Pacing(PacingMode.EVENTS_PER_SECOND, 10.0).secondsFor(events = 5, elapsedTime = 100.0))
        assertEquals(50.0, Pacing(PacingMode.TIME_PER_SECOND, 2.0).secondsFor(events = 5, elapsedTime = 100.0))
        assertEquals(0.0, Pacing(PacingMode.MAX_SPEED).secondsFor(events = 5, elapsedTime = 100.0))
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

        controller.callbacks.transport.runToStep(assertNotNull(controller.edits.jumps.stepTarget()))

        assertEquals(start + 1, controller.store.state.controls.step)
    }

    @Test
    fun `time quick jumps go from ten to a thousand`() {
        assertEquals(listOf(10.0, 100.0, 1000.0), QuickJumps().time)
    }

    @Test
    fun `step quick jumps scale with the node count`() {
        assertEquals(listOf(100L, 200L, 1000L), QuickJumps().stepDeltas(100))
        assertEquals(listOf(2L, 10L), QuickJumps().stepDeltas(1))
        assertEquals(emptyList(), QuickJumps().stepDeltas(0))
    }

    @Test
    fun `configured step quick jumps ignore the node count`() {
        assertEquals(listOf(5L, 50L), QuickJumps(steps = listOf(5L, 50L)).stepDeltas(100))
    }

    @Test
    fun `quick jumps must be positive`() {
        assertFailsWith<IllegalArgumentException> { QuickJumps(time = listOf(0.0)) }
        assertFailsWith<IllegalArgumentException> { QuickJumps(steps = listOf(-1L)) }
    }

    @Test
    fun `fractional time jumps keep their decimals`() {
        assertEquals("0.5", 0.5.formatCompact())
        assertEquals("1K", 1000.0.formatCompact())
    }

    @Test
    fun `demo controller quick jumps run ahead from the current step and time`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        val start = controller.store.state.controls

        controller.callbacks.transport.jumpStepBy(10)
        assertEquals(start.step + 10, controller.store.state.controls.step)

        val afterSteps = controller.store.state.controls.time
        controller.callbacks.transport.jumpTimeBy(100.0)
        assertEquals(afterSteps + 100.0, controller.store.state.controls.time)
    }

    @Test
    fun `demo controller rejects backward time jumps`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.edits.jumps.setTimeInput("1.5")
        controller.callbacks.transport.runToTime(assertNotNull(controller.edits.jumps.timeTarget()))

        val dialog = controller.store.state.controls.dialog
        assertNotNull(dialog)
        assertEquals("Invalid time", dialog.title)
    }

    @Test
    fun `demo controller updates fps on submit`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.edits.frameRate.setInput("120")
        controller.edits.frameRate.apply()

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

        controller.edits.jumps.setStepInput("99")
        controller.callbacks.transport.runToStep(assertNotNull(controller.edits.jumps.stepTarget()))

        assertEquals(SimulationStatus.PAUSED, controller.store.state.controls.status)
        assertEquals(99L, controller.store.state.controls.step)
    }

    @Test
    fun `selecting several nodes builds a group inspector`() {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)

        controller.edits.selection.select(listOf(1, 3))

        assertEquals(listOf(1, 3), controller.store.state.selectedNodeIds)
        val inspector = controller.store.state.inspector as NodeGroup
        assertEquals(listOf(1, 3), inspector.nodeIds)
        assertEquals("Mixed", inspector.concentrations.first { it.label == "signal" }.value)
    }
}
