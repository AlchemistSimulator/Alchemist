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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.callbacks.NoOpUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest

class SelectionActionsTest {
    @Test
    fun `the buttons sit in a row above the top right corner of the selection`() {
        val actions = assertNotNull(SelectionActions.of(listOf(FIRST, SECOND), MARGIN, BUTTON, GAP))
        val delete = assertNotNull(actions.buttons[SelectionAction.DELETE])
        assertEquals(actions.bounds.right, delete.right)
        assertTrue(actions.buttons.values.all { it.bottom <= actions.bounds.top })
        assertEquals(SelectionAction.CLONE, actions.hit(assertNotNull(actions.buttons[SelectionAction.CLONE]).center))
        assertTrue(actions.hoverArea.contains(delete.center) && actions.hoverArea.contains(FIRST))
    }

    @Test
    fun `without room above, the buttons go below the selection`() {
        val actions = assertNotNull(SelectionActions.of(listOf(Offset(150f, 5f)), MARGIN, BUTTON, GAP))
        assertTrue(actions.buttons.values.all { it.top >= actions.bounds.bottom })
    }

    @Test
    fun `no selected node on screen means no buttons`() {
        assertNull(SelectionActions.of(emptyList(), MARGIN, BUTTON, GAP))
    }

    @Test
    fun `dragging from move moves the selection`() = runTest {
        val recorded = dragFrom(SelectionAction.MOVE, by = Offset(20f, 0f))
        assertEquals(listOf("move" to listOf(position(1, 2.0, 0.0), position(2, 3.0, 0.0))), recorded)
    }

    @Test
    fun `dragging from clone adds copies where they are dropped`() = runTest {
        val recorded = dragFrom(SelectionAction.CLONE, by = Offset(0f, 10f))
        assertEquals(listOf("clone" to listOf(position(1, 0.0, -1.0), position(2, 1.0, -1.0))), recorded)
    }

    @Test
    fun `a click on move or clone does nothing`() = runTest {
        assertEquals(NOTHING, dragFrom(SelectionAction.MOVE, by = Offset.Zero))
        assertEquals(NOTHING, dragFrom(SelectionAction.CLONE, by = Offset.Zero))
    }

    @Test
    fun `delete deletes the selection only if released on the button`() = runTest {
        assertEquals(listOf("delete" to listOf(1, 2)), dragFrom(SelectionAction.DELETE, by = Offset.Zero))
        assertEquals(NOTHING, dragFrom(SelectionAction.DELETE, by = Offset(0f, 200f)))
    }

    @Test
    fun `ctrl d clones the selection a little away`() = runTest {
        val recorded: List<Pair<String, Any>> = Recorder().also {
            it.run(gestures().cloneSelection(Offset(10f, 10f)))
        }.calls
        assertEquals(listOf("clone" to listOf(position(1, 1.0, -1.0), position(2, 2.0, -1.0))), recorded)
    }

    private class Recorder {
        val calls = mutableListOf<Pair<String, List<Any>>>()

        private val callbacks = object : AlchemistUiCallbacks by NoOpUiCallbacks {
            override val nodes: AlchemistUiCallbacks.Nodes = object :
                AlchemistUiCallbacks.Nodes by NoOpUiCallbacks.nodes {
                override suspend fun move(nodePositions: List<NodePositionUpdate>) {
                    calls += "move" to nodePositions
                }

                override suspend fun clone(copies: List<NodePositionUpdate>) {
                    calls += "clone" to copies
                }

                override suspend fun delete(nodeIds: List<Int>) {
                    calls += "delete" to nodeIds
                }
            }
        }

        suspend fun run(action: ViewportAction?) {
            action?.invoke(ComposeUiController(ComposeUiStateStore(AlchemistUiState(scene = SCENE)), callbacks))
        }
    }

    private companion object {
        const val MARGIN = 10f
        const val GAP = 5f
        const val PIXELS_PER_UNIT = 10f
        val BUTTON = Size(50f, 20f)
        val NOTHING = emptyList<Pair<String, List<Any>>>()
        val FIRST = Offset(100f, 100f)
        val SECOND = Offset(110f, 100f)
        val SIZE = IntSize(400, 300)
        val SCENE = ViewportScene(
            nodes = persistentListOf(
                ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0)),
                ViewportNode(id = 2, coordinates = persistentListOf(1.0, 0.0)),
            ),
        )

        fun position(nodeId: Int, x: Double, y: Double) = NodePositionUpdate(nodeId, persistentListOf(x, y))

        /** Gestures over nodes 1 and 2, both selected, drawn at [FIRST] and [SECOND], 10 pixels a unit apart. */
        fun gestures() = ViewportGestures(
            sceneCache = ViewportSceneCache(scene = SCENE, baseCenters = persistentListOf(FIRST, SECOND)),
            viewportSize = SIZE,
            camera = ViewportCameraState(),
            projection = ViewportProjection(worldCenterX = 0.0, worldCenterY = 0.0, pixelsPerUnit = PIXELS_PER_UNIT),
            selectedNodeIds = setOf(1, 2),
            tapThresholdPx = MARGIN,
            dragThresholdPx = GAP,
            selectionActions = SelectionActions.of(listOf(FIRST, SECOND), MARGIN, BUTTON, GAP),
        )

        /** What the callbacks get after pressing the [action] button and releasing [by] away from it. */
        suspend fun dragFrom(action: SelectionAction, by: Offset): List<Pair<String, List<Any>>> {
            val gestures = gestures()
            val start = assertNotNull(SelectionActions.of(listOf(FIRST, SECOND), MARGIN, BUTTON, GAP))
                .buttons
                .getValue(action)
                .center
            val drag = gestures.press(start, secondaryPressed = false).movedTo(start + by, secondaryPressed = false)
            return Recorder().also { it.run(gestures.release(drag)) }.calls
        }
    }
}
