/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

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
        controller.store.update { it.copy(selectedNodeId = 3) }

        runSuspend { controller.callbacks.onToggleLinks() }

        assertTrue(controller.store.state.scene.showLinks)
        assertEquals(3, controller.store.state.selectedNodeId)
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
