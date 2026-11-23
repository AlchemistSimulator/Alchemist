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
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
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
    private val selectionActions: SelectionActions? = null,
) {
    /**
     * The drag started by a press at [position].
     * The secondary button pans; the buttons over the selection move, clone or delete it;
     * Ctrl on a selected node moves the selection; anything else selects:
     * with Shift, adding to the selection; with Ctrl, adding or removing each node.
     */
    fun press(
        position: Offset,
        secondaryPressed: Boolean,
        ctrlPressed: Boolean = false,
        shiftPressed: Boolean = false,
    ): ViewportDrag = when {
        secondaryPressed -> ViewportDrag.Pan(position)
        else -> when (selectionActions?.hit(position)) {
            SelectionAction.MOVE -> dragSelection(position, ViewportDrag.MoveNodes.Kind.MOVE)
            SelectionAction.CLONE -> dragSelection(position, ViewportDrag.MoveNodes.Kind.CLONE)
            SelectionAction.DELETE -> ViewportDrag.DeleteNodes(current = position, nodeIds = selectedNodeIds)
            null -> when {
                ctrlPressed && !shiftPressed && hitNode(position)?.id in selectedNodeIds ->
                    dragSelection(position, ViewportDrag.MoveNodes.Kind.CTRL_DRAG)
                else -> ViewportDrag.Select(
                    anchor = position,
                    current = position,
                    mode = SelectionMode.of(ctrlPressed = ctrlPressed, shiftPressed = shiftPressed),
                )
            }
        }
    }

    /** Adds copies of the selection, [offset] away on screen; `null` without a selection. */
    fun cloneSelection(offset: Offset): ViewportAction? =
        placements(selectedNodeIds, offset / camera.zoom)?.let { copies -> action { callbacks.nodes.clone(copies) } }

    /** Deletes the selection; `null` without a selection. */
    fun deleteSelection(): ViewportAction? = selectedNodeIds
        .takeIf { it.isNotEmpty() }
        ?.let { nodeIds -> action { callbacks.nodes.delete(nodeIds.toList()) } }

    /** The node nearest to [position], however far; `null` if there are no nodes. */
    fun nearestNode(position: Offset): ViewportNode? = sceneCache.findNearestNode(viewportSize, camera, position)

    /**
     * What to do when [drag] ends; `null` for nothing.
     * A Ctrl click on a selected node, which did not move it, removes it from the selection;
     * the Delete button deletes the selection if released on it.
     */
    fun release(drag: ViewportDrag): ViewportAction? = when (drag) {
        is ViewportDrag.MoveNodes -> when {
            drag.delta.getDistance() >= dragThresholdPx -> moveOrClone(drag)
            drag.kind == ViewportDrag.MoveNodes.Kind.CTRL_DRAG ->
                changeSelection(SelectionMode.TOGGLE, listOfNotNull(hitNode(drag.anchor)?.id))
            else -> null
        }
        is ViewportDrag.DeleteNodes -> deleteSelection().takeIf {
            selectionActions?.hit(drag.current) == SelectionAction.DELETE
        }
        is ViewportDrag.Select -> select(drag)
        ViewportDrag.None, is ViewportDrag.Pan -> null
    }

    private fun action(block: ViewportAction): ViewportAction = block

    private fun changeSelection(mode: SelectionMode, nodeIds: List<Int>): ViewportAction {
        val selection = mode.apply(selectedNodeIds, nodeIds)
        return action { edits.selection.select(selection) }
    }

    private fun dragSelection(position: Offset, kind: ViewportDrag.MoveNodes.Kind) = ViewportDrag.MoveNodes(
        anchor = position,
        anchorBase = camera.toBase(position, viewportSize),
        current = position,
        nodeIds = selectedNodeIds,
        kind = kind,
    )

    private fun hitNode(position: Offset): ViewportNode? =
        sceneCache.findHitNode(viewportSize, camera, position, tapThresholdPx)

    private fun moveOrClone(drag: ViewportDrag.MoveNodes): ViewportAction? =
        placements(drag.nodeIds, drag.baseDelta(camera, viewportSize))?.let { positions ->
            when (drag.kind) {
                ViewportDrag.MoveNodes.Kind.CLONE -> action { callbacks.nodes.clone(positions) }
                else -> action { callbacks.nodes.move(positions) }
            }
        }

    /** Where the nodes [nodeIds] go if moved by [baseDelta] in base coordinates; `null` if nowhere. */
    private fun placements(nodeIds: Set<Int>, baseDelta: Offset): List<NodePositionUpdate>? = projection
        ?.let { projection ->
            sceneCache.scene
                .translateSelectedNodes(
                    nodeIds = nodeIds,
                    deltaX = (baseDelta.x / projection.pixelsPerUnit).toDouble(),
                    deltaY = (-baseDelta.y / projection.pixelsPerUnit).toDouble(),
                )
                .nodes
                .filter { it.id in nodeIds }
                .map(ViewportNode::toPositionUpdate)
                .takeIf { it.isNotEmpty() }
        }

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
