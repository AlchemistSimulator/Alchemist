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
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks

/**
 * Something the viewport asks the simulation to do.
 */
internal typealias ViewportAction = suspend AlchemistUiCallbacks.() -> Unit

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
     * The secondary button pans; Ctrl on a selected node moves the selection; anything else selects.
     */
    fun press(position: Offset, secondaryPressed: Boolean, ctrlPressed: Boolean): ViewportDrag = when {
        secondaryPressed -> ViewportDrag.Pan(position)
        ctrlPressed && hitNode(position)?.id in selectedNodeIds ->
            ViewportDrag.MoveNodes(anchor = position, current = position, nodeIds = selectedNodeIds)
        else -> ViewportDrag.Select(anchor = position, current = position)
    }

    /** What to do when [drag] ends; `null` for nothing. */
    fun release(drag: ViewportDrag): ViewportAction? = when (drag) {
        is ViewportDrag.MoveNodes -> moveNodes(drag)
        is ViewportDrag.Select -> select(drag)
        ViewportDrag.None, is ViewportDrag.Pan -> null
    }

    private fun action(block: ViewportAction): ViewportAction = block

    private fun hitNode(position: Offset): ViewportNode? =
        sceneCache.findHitNode(viewportSize, camera, position, tapThresholdPx)

    private fun moveNodes(drag: ViewportDrag.MoveNodes): ViewportAction? = projection
        ?.takeIf { (drag.current - drag.anchor).getDistance() >= dragThresholdPx }
        ?.let { projection ->
            val delta = camera.toBase(drag.current, viewportSize) - camera.toBase(drag.anchor, viewportSize)
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
        ?.let { positions -> action { onNodesMoved(positions) } }

    private fun select(drag: ViewportDrag.Select): ViewportAction = when {
        drag.length >= dragThresholdPx -> {
            val nodeIds = sceneCache.selectNodes(viewportSize, camera, drag.rect)
            action { onNodesSelected(nodeIds) }
        }
        else -> when (val hit = hitNode(drag.current)) {
            null -> action { onInspectorDismiss() }
            else -> action { onNodeSelected(hit.id) }
        }
    }
}
