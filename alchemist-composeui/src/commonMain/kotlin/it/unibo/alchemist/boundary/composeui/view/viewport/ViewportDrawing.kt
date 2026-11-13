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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.theme.Background
import it.unibo.alchemist.boundary.composeui.view.theme.BackgroundVariant
import it.unibo.alchemist.boundary.composeui.view.theme.LINK_STROKE_WIDTH
import it.unibo.alchemist.boundary.composeui.view.theme.NODE_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SELECTED_NODE_INNER_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.SELECTED_NODE_RADIUS
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.lerp
import kotlin.math.max

/** Fills the viewport background. */
internal fun DrawScope.drawBackdrop() {
    drawRect(brush = Brush.verticalGradient(colors = listOf(BackgroundVariant.copy(alpha = 0.55f), Background)))
}

/** Draws the background grid and, when visible, the world axes. */
internal fun DrawScope.drawGrid(camera: ViewportCameraState) {
    val origin = size.center + camera.pan
    val step = GridSpacing.of(size.width, size.height, camera.zoom).pixels
    if (step > 0f && step.isFinite()) {
        gridLines(origin.x % step - step, step, size.width).forEach { x ->
            drawLine(
                color = Outline.copy(alpha = 0.32f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f,
            )
        }
        gridLines(origin.y % step - step, step, size.height).forEach { y ->
            drawLine(
                color = Outline.copy(alpha = 0.28f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }
    }
    if (origin.x in 0f..size.width) {
        drawAxis(start = Offset(origin.x, 0f), end = Offset(origin.x, size.height))
    }
    if (origin.y in 0f..size.height) {
        drawAxis(start = Offset(0f, origin.y), end = Offset(size.width, origin.y))
    }
}

/** Draws the links and the nodes, moving the nodes being dragged along with the pointer. */
internal fun DrawScope.drawScene(
    scene: ViewportScene,
    frame: ViewportFrame,
    camera: ViewportCameraState,
    selectedNodeIds: Set<Int>,
    drag: ViewportDrag,
) {
    val moving = drag as? ViewportDrag.MoveNodes
    fun centerOf(nodeIndex: Int): Offset? = frame.screenPositions[nodeIndex]?.let { position ->
        val isMoving = moving != null && scene.nodes.getOrNull(nodeIndex)?.id in moving.nodeIds
        if (isMoving) position + moving.delta else position
    }
    if (scene.showLinks) {
        frame.visibleEdges.forEach { edge ->
            val start = centerOf(edge.fromIndex)
            val end = centerOf(edge.toIndex)
            if (start != null && end != null) {
                drawLine(
                    color = Outline.copy(alpha = 0.42f),
                    start = start,
                    end = end,
                    strokeWidth = LINK_STROKE_WIDTH.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
    frame.visibleNodeIndices.forEach { nodeIndex ->
        val node = scene.nodes[nodeIndex]
        centerOf(nodeIndex)?.let { center -> drawNode(node, center, camera.zoom, node.id in selectedNodeIds) }
    }
}

/** Draws the selection box. */
internal fun DrawScope.drawSelection(rect: Rect) {
    drawRect(color = PrimaryAccent.copy(alpha = 0.14f), topLeft = rect.topLeft, size = rect.size)
    drawRect(
        color = PrimaryAccent.copy(alpha = 0.75f),
        topLeft = rect.topLeft,
        size = rect.size,
        style = Stroke(width = 1.5.dp.toPx()),
    )
}

private fun DrawScope.drawAxis(start: Offset, end: Offset) {
    drawLine(color = Outline.copy(alpha = 0.7f), start = start, end = end, strokeWidth = 1.6f, cap = StrokeCap.Round)
}

private fun DrawScope.drawNode(node: ViewportNode, center: Offset, zoom: Float, isSelected: Boolean) {
    val nodeColor = lerp(SecondaryAccent, PrimaryAccent, node.accent)
    val selectedRadius = SELECTED_NODE_RADIUS.dp.toPx() * zoom
    if (isSelected) {
        drawCircle(color = nodeColor.copy(alpha = 0.20f), radius = selectedRadius, center = center)
        drawCircle(
            color = PrimaryAccent,
            radius = SELECTED_NODE_INNER_RADIUS.dp.toPx() * zoom,
            center = center,
            style = Stroke(width = 2.dp.toPx() * zoom),
        )
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(nodeColor, nodeColor.copy(alpha = 0.45f)),
            center = center,
            radius = max(1f, selectedRadius),
        ),
        radius = NODE_RADIUS.dp.toPx() * zoom,
        center = center,
    )
}

private fun gridLines(start: Float, step: Float, end: Float): Sequence<Float> =
    generateSequence(start) { it + step }.takeWhile { it < end }
