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
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController

/**
 * Something the viewport asks for: a change of selection, or a change to the simulation.
 */
internal typealias ViewportAction = suspend ComposeUiController.() -> Unit

/**
 * Turns pointer presses and releases into drags and actions, for the viewport as currently shown.
 */
internal class ViewportGestures(
    private val sceneCache: ViewportSceneCache,
    private val viewportSize: IntSize,
    private val camera: ViewportCameraState,
    private val projection: ViewportProjection?,
    private val selectedNodeIds: Set<Int>,
    private val tapThresholdPx: Float,
    private val dragThresholdPx: Float,
) {
    /**
     * The drag started by a press at [position].
     * The secondary button pans; Ctrl on a selected node moves the selection; anything else selects:
     * with Shift, adding to the selection; with Ctrl, adding or removing each node.
     */
    fun press(
        position: Offset,
        secondaryPressed: Boolean,
        ctrlPressed: Boolean = false,
        shiftPressed: Boolean = false,
    ): ViewportDrag = when {
        secondaryPressed -> ViewportDrag.Pan(position)
        ctrlPressed && !shiftPressed && hitNode(position)?.id in selectedNodeIds ->
            ViewportDrag.MoveNodes(
                anchor = position,
                anchorBase = camera.toBase(position, viewportSize),
                current = position,
                nodeIds = selectedNodeIds,
            )
        else -> ViewportDrag.Select(
            anchor = position,
            current = position,
            mode = SelectionMode.of(ctrlPressed = ctrlPressed, shiftPressed = shiftPressed),
        )
    }

    /** The node nearest to [position], however far; `null` if there are no nodes. */
    fun nearestNode(position: Offset): ViewportNode? = sceneCache.findNearestNode(viewportSize, camera, position)

    /**
     * What to do when [drag] ends; `null` for nothing.
     * A Ctrl click on a selected node, which did not move it, removes it from the selection.
     */
    fun release(drag: ViewportDrag): ViewportAction? = when (drag) {
        is ViewportDrag.MoveNodes -> when {
            drag.delta.getDistance() < dragThresholdPx ->
                changeSelection(SelectionMode.TOGGLE, listOfNotNull(hitNode(drag.anchor)?.id))
            else -> moveNodes(drag)
        }
        is ViewportDrag.Select -> select(drag)
        ViewportDrag.None, is ViewportDrag.Pan -> null
    }

    private fun action(block: ViewportAction): ViewportAction = block

    private fun changeSelection(mode: SelectionMode, nodeIds: List<Int>): ViewportAction {
        val selection = mode.apply(selectedNodeIds, nodeIds)
        return action { edits.selection.select(selection) }
    }

    private fun hitNode(position: Offset): ViewportNode? =
        sceneCache.findHitNode(viewportSize, camera, position, tapThresholdPx)

    private fun moveNodes(drag: ViewportDrag.MoveNodes): ViewportAction? = projection
        ?.let { projection ->
            val delta = drag.baseDelta(camera, viewportSize)
            sceneCache.scene
                .translateSelectedNodes(
                    nodeIds = drag.nodeIds,
                    deltaX = (delta.x / projection.pixelsPerUnit).toDouble(),
                    deltaY = (-delta.y / projection.pixelsPerUnit).toDouble(),
                )
                .nodes
                .filter { it.id in drag.nodeIds }
                .map(ViewportNode::toPositionUpdate)
                .takeIf { it.isNotEmpty() }
        }
        ?.let { positions -> action { callbacks.nodes.move(positions) } }

    private fun select(drag: ViewportDrag.Select): ViewportAction? = when {
        drag.length >= dragThresholdPx ->
            changeSelection(drag.mode, sceneCache.selectNodes(viewportSize, camera, drag.rect))
        else -> when (val hit = hitNode(drag.current)) {
            // A click on empty space clears the selection, unless a key asks to keep it.
            null -> action { edits.selection.clear() }.takeIf { drag.mode == SelectionMode.REPLACE }
            else -> changeSelection(drag.mode, listOf(hit.id))
        }
    }
}
