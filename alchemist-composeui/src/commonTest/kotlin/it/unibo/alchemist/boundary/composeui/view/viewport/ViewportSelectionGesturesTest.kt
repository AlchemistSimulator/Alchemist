/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.callbacks.NoOpUiCallbacks
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest

class ViewportSelectionGesturesTest {
    @Test
    fun `ctrl click adds a node to the selection`() = runTest {
        assertEquals(listOf(1, 2), selectionAfter(selected = setOf(1), at = SECOND, ctrl = true))
    }

    @Test
    fun `ctrl click on a selected node removes it`() = runTest {
        assertEquals(listOf(2), selectionAfter(selected = setOf(1, 2), at = FIRST, ctrl = true))
    }

    @Test
    fun `a plain click replaces the selection`() = runTest {
        assertEquals(listOf(2), selectionAfter(selected = setOf(1), at = SECOND))
    }

    @Test
    fun `shift drag adds the boxed nodes to the selection`() = runTest {
        assertEquals(listOf(1, 2, 3), selectionAfter(selected = setOf(1), at = BOX_START, to = BOX_END, shift = true))
    }

    @Test
    fun `a plain drag replaces the selection with the boxed nodes`() = runTest {
        assertEquals(listOf(2, 3), selectionAfter(selected = setOf(1), at = BOX_START, to = BOX_END))
    }

    @Test
    fun `a click with a key on empty space keeps the selection`() {
        val gestures = gestures(setOf(1))
        assertNull(gestures.release(gestures.press(EMPTY, secondaryPressed = false, shiftPressed = true)))
        assertNull(gestures.release(gestures.press(EMPTY, secondaryPressed = false, ctrlPressed = true)))
    }

    @Test
    fun `selection modes combine the selection with the new nodes`() {
        assertEquals(listOf(3), SelectionMode.REPLACE.apply(setOf(1, 2), listOf(3)))
        assertEquals(listOf(1, 2, 3), SelectionMode.ADD.apply(setOf(1, 2), listOf(2, 3)))
        assertEquals(listOf(1, 3), SelectionMode.TOGGLE.apply(setOf(1, 2), listOf(2, 3)))
    }

    private companion object {
        const val THRESHOLD_PX = 5f
        val FIRST = Offset(10f, 10f)
        val SECOND = Offset(50f, 10f)
        val THIRD = Offset(90f, 10f)
        val EMPTY = Offset(150f, 80f)
        val BOX_START = Offset(40f, 0f)
        val BOX_END = Offset(100f, 20f)
        val SIZE = IntSize(200, 100)

        val scene = ViewportScene(
            nodes = (1..3).map { ViewportNode(id = it, coordinates = persistentListOf(0.0, 0.0)) }.toImmutableList(),
        )

        /** Gestures over nodes 1, 2 and 3, drawn at [FIRST], [SECOND] and [THIRD]. */
        fun gestures(selected: Set<Int>) = ViewportGestures(
            sceneCache = ViewportSceneCache(scene = scene, baseCenters = persistentListOf(FIRST, SECOND, THIRD)),
            viewportSize = SIZE,
            camera = ViewportCameraState(),
            projection = null,
            selectedNodeIds = selected,
            tapThresholdPx = THRESHOLD_PX,
            dragThresholdPx = THRESHOLD_PX,
        )

        /** The selection after pressing at [at] and releasing at [to], starting from [selected]. */
        suspend fun selectionAfter(
            selected: Set<Int>,
            at: Offset,
            to: Offset = at,
            ctrl: Boolean = false,
            shift: Boolean = false,
        ): List<Int> {
            val gestures = gestures(selected)
            val drag = gestures
                .press(at, secondaryPressed = false, ctrlPressed = ctrl, shiftPressed = shift)
                .movedTo(to, secondaryPressed = false)
            val controller = ComposeUiController(
                AlchemistUiState(scene = scene).withSelection(selected.toList()),
            ) { NoOpUiCallbacks }
            gestures.release(drag)?.invoke(controller)
            return controller.store.state.selectedNodeIds
        }
    }
}
