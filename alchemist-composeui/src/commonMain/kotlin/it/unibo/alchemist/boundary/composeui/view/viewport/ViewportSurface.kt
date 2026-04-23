/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import it.unibo.alchemist.boundary.composeui.formatFixed
import it.unibo.alchemist.boundary.composeui.toPositionUpdate
import it.unibo.alchemist.boundary.composeui.translateSelectedNodes
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.theme.Background
import it.unibo.alchemist.boundary.composeui.view.theme.BackgroundVariant
import it.unibo.alchemist.boundary.composeui.view.theme.GridHorizontalDivisions
import it.unibo.alchemist.boundary.composeui.view.theme.GridVerticalDivisions
import it.unibo.alchemist.boundary.composeui.view.theme.LinkStrokeWidth
import it.unibo.alchemist.boundary.composeui.view.theme.NodeHitRadius
import it.unibo.alchemist.boundary.composeui.view.theme.NodeRadius
import it.unibo.alchemist.boundary.composeui.view.theme.Outline
import it.unibo.alchemist.boundary.composeui.view.theme.PrimaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SecondaryAccent
import it.unibo.alchemist.boundary.composeui.view.theme.SelectedNodeInnerRadius
import it.unibo.alchemist.boundary.composeui.view.theme.SelectedNodeRadius
import it.unibo.alchemist.boundary.composeui.view.theme.Surface
import it.unibo.alchemist.boundary.composeui.view.theme.SurfaceStrong
import it.unibo.alchemist.boundary.composeui.view.theme.TextPrimary
import it.unibo.alchemist.boundary.composeui.view.theme.lerp
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ViewportSurface(
    scene: ViewportScene,
    selectedNodeIds: List<Int>,
    callbacks: AlchemistUiCallbacks,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf(ViewportCameraState()) }
    var fixedProjection by remember { mutableStateOf<ViewportProjection?>(null) }
    var rightDragAnchor by remember { mutableStateOf<Offset?>(null) }
    var selectionDragAnchor by remember { mutableStateOf<Offset?>(null) }
    var selectionDragCurrent by remember { mutableStateOf<Offset?>(null) }
    var nodeDragAnchor by remember { mutableStateOf<Offset?>(null) }
    var nodeDragCurrent by remember { mutableStateOf<Offset?>(null) }
    var draggedNodeIds by remember { mutableStateOf<List<Int>>(emptyList()) }
    val candidateProjection = remember(scene.nodes, viewportSize) { scene.createViewportProjection(viewportSize) }
    val projection = fixedProjection ?: candidateProjection
    LaunchedEffect(candidateProjection) {
        if (fixedProjection == null && candidateProjection != null) {
            fixedProjection = candidateProjection
        }
    }
    val previewScene = remember(scene, draggedNodeIds, nodeDragAnchor, nodeDragCurrent, viewportSize, camera, projection) {
        val anchor = nodeDragAnchor
        val current = nodeDragCurrent
        if (anchor == null || current == null || projection == null) {
            scene
        } else {
            val (deltaX, deltaY) = screenDeltaToWorldDelta(anchor, current, viewportSize, camera, projection)
            scene.translateSelectedNodes(draggedNodeIds, deltaX, deltaY)
        }
    }
    val baseNodes = remember(previewScene.nodes, viewportSize, projection) { renderNodes(previewScene, viewportSize, projection) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tapThresholdPx = with(density) { NodeHitRadius.dp.toPx() }
    val dragThresholdPx = with(density) { 6.dp.toPx() }
    val selectionRect = selectionDragAnchor?.let { anchor ->
        val current = selectionDragCurrent ?: anchor
        createSelectionRect(anchor, current).takeIf { anchor.distanceTo(current) >= dragThresholdPx }
    }
    Surface(
        modifier = modifier,
        color = Surface,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(12.dp),
        elevation = 0.dp,
    ) {
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Outline.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(BackgroundVariant.copy(alpha = 0.55f), Background),
                        radius = 1600f,
                    ),
                ),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates -> viewportSize = coordinates.size }
                    .onPointerEvent(PointerEventType.Press) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        if (event.buttons.isSecondaryPressed) {
                            rightDragAnchor = change.position
                            selectionDragAnchor = null
                            selectionDragCurrent = null
                            nodeDragAnchor = null
                            nodeDragCurrent = null
                            draggedNodeIds = emptyList()
                        } else {
                            rightDragAnchor = null
                            val hit = findHitNode(
                                baseNodes = baseNodes,
                                viewportSize = viewportSize,
                                camera = camera,
                                tapOffset = change.position,
                                tapThresholdPx = tapThresholdPx,
                            )
                            val shouldDragSelection =
                                event.keyboardModifiers.isCtrlPressed &&
                                    hit != null &&
                                    hit.node.id in selectedNodeIds
                            if (shouldDragSelection) {
                                selectionDragAnchor = null
                                selectionDragCurrent = null
                                nodeDragAnchor = change.position
                                nodeDragCurrent = change.position
                                draggedNodeIds = selectedNodeIds
                            } else {
                                nodeDragAnchor = null
                                nodeDragCurrent = null
                                draggedNodeIds = emptyList()
                                selectionDragAnchor = change.position
                                selectionDragCurrent = change.position
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        if (event.buttons.isSecondaryPressed) {
                            val previous = rightDragAnchor ?: change.position
                            val delta = change.position - previous
                            if (delta != Offset.Zero) {
                                camera = camera.panBy(delta)
                            }
                            rightDragAnchor = change.position
                        } else {
                            rightDragAnchor = null
                            if (nodeDragAnchor != null) {
                                nodeDragCurrent = change.position
                            } else if (selectionDragAnchor != null) {
                                selectionDragCurrent = change.position
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Release) { event ->
                        val releasePosition = event.changes.firstOrNull()?.position
                        val moveAnchor = nodeDragAnchor
                        val moveCurrent = nodeDragCurrent ?: releasePosition
                        if (moveAnchor != null && moveCurrent != null) {
                            if (moveAnchor.distanceTo(moveCurrent) >= dragThresholdPx && projection != null) {
                                val (deltaX, deltaY) = screenDeltaToWorldDelta(
                                    moveAnchor,
                                    moveCurrent,
                                    viewportSize,
                                    camera,
                                    projection,
                                )
                                val movedNodes = scene
                                    .translateSelectedNodes(draggedNodeIds, deltaX, deltaY)
                                    .nodes
                                    .filter { it.id in draggedNodeIds }
                                    .map { it.toPositionUpdate() }
                                if (movedNodes.isNotEmpty()) {
                                    coroutineScope.launch { callbacks.onNodesMoved(movedNodes) }
                                }
                            }
                        } else {
                            val anchor = selectionDragAnchor
                            val current = selectionDragCurrent ?: releasePosition
                            if (anchor != null && current != null) {
                                if (anchor.distanceTo(current) >= dragThresholdPx) {
                                    val mappedNodes = baseNodes.map { node ->
                                        node.copy(center = node.center.toScreenPosition(viewportSize, camera))
                                    }
                                    val selectedIds = mappedNodes
                                        .filter { selectionNode ->
                                            createSelectionRect(anchor, current).contains(selectionNode.center)
                                        }
                                        .map { selectionNode -> selectionNode.node.id }
                                    coroutineScope.launch { callbacks.onNodesSelected(selectedIds) }
                                } else {
                                    val hit = findHitNode(
                                        baseNodes = baseNodes,
                                        viewportSize = viewportSize,
                                        camera = camera,
                                        tapOffset = current,
                                        tapThresholdPx = tapThresholdPx,
                                    )
                                    coroutineScope.launch {
                                        if (hit != null) {
                                            callbacks.onNodeSelected(hit.node.id)
                                        } else {
                                            callbacks.onInspectorDismiss()
                                        }
                                    }
                                }
                            }
                        }
                        selectionDragAnchor = null
                        selectionDragCurrent = null
                        nodeDragAnchor = null
                        nodeDragCurrent = null
                        draggedNodeIds = emptyList()
                        rightDragAnchor = null
                    }
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        val pointerChange = event.changes.firstOrNull() ?: return@onPointerEvent
                        val scroll = pointerChange.scrollDelta
                        if (scroll != Offset.Zero && viewportSize.width > 0 && viewportSize.height > 0) {
                            camera = camera.zoomBy(
                                viewportSize = viewportSize,
                                pivot = pointerChange.position,
                                scrollDelta = scroll.y,
                            )
                        }
                    },
            ) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundVariant.copy(alpha = 0.55f), Background),
                    ),
                )
                val currentCamera = camera
                drawGrid(size, currentCamera)

                // Map the base nodes and edges down here in the Draw phase
                val mappedNodes = baseNodes.map { node ->
                    node.copy(center = node.center.toScreenPosition(viewportSize, currentCamera))
                }
                val mappedEdges = renderEdges(previewScene.edges, mappedNodes)

                if (previewScene.showLinks) {
                    mappedEdges.forEach { edge ->
                        drawLine(
                            color = Outline.copy(alpha = 0.42f),
                            start = edge.start,
                            end = edge.end,
                            strokeWidth = LinkStrokeWidth.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                mappedNodes.forEach { rendered ->
                    val isSelected = rendered.node.id in selectedNodeIds
                    val nodeColor = lerp(SecondaryAccent, PrimaryAccent, rendered.node.accent)
                    val screenRadius = NodeRadius.dp.toPx() * currentCamera.zoom
                    val screenSelectedRadius = SelectedNodeRadius.dp.toPx() * currentCamera.zoom
                    val screenSelectedInnerRadius = SelectedNodeInnerRadius.dp.toPx() * currentCamera.zoom

                    if (isSelected) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.20f),
                            radius = screenSelectedRadius,
                            center = rendered.center,
                        )
                        drawCircle(
                            color = PrimaryAccent,
                            radius = screenSelectedInnerRadius,
                            center = rendered.center,
                            style = Stroke(width = 2.dp.toPx() * currentCamera.zoom),
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(nodeColor, nodeColor.copy(alpha = 0.45f)),
                            center = rendered.center,
                            radius = max(1f, screenSelectedRadius),
                        ),
                        radius = screenRadius,
                        center = rendered.center,
                    )
                }
                selectionRect?.let { selection ->
                    drawRect(
                        color = PrimaryAccent.copy(alpha = 0.14f),
                        topLeft = selection.topLeft,
                        size = selection.size,
                    )
                    drawRect(
                        color = PrimaryAccent.copy(alpha = 0.75f),
                        topLeft = selection.topLeft,
                        size = selection.size,
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Alchemist Simulator",
                    style = MaterialTheme.typography.h4,
                )
                Text(
                    text = scene.message,
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                SummaryRail(
                    summary = scene.summary,
                    showLinks = scene.showLinks,
                    onToggleLinks = { coroutineScope.launch { callbacks.onToggleLinks() } },
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                color = SurfaceStrong.copy(alpha = 0.88f),
                shape = RoundedCornerShape(8.dp),
                elevation = 0.dp,
            ) {
                Text(
                    text = if (scene.nodes.isEmpty()) {
                        "No nodes to display"
                    } else {
                        "Click to inspect · drag to select · Ctrl-drag selected nodes · right-drag to pan · wheel to zoom"
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.caption,
                )
            }
            val gridLegend = remember(viewportSize, camera.zoom, projection) {
                if (viewportSize.width == 0 || viewportSize.height == 0 || projection == null) return@remember null
                val baseStep = min(
                    viewportSize.width / GridVerticalDivisions.toFloat(),
                    viewportSize.height / GridHorizontalDivisions.toFloat(),
                )
                var step = baseStep * camera.zoom
                var s = 1.0
                while (step < 10f) {
                    step *= 2f
                    s *= 2.0
                }
                while (step > 100f) {
                    step /= 2f
                    s /= 2.0
                }
                val worldStep = (baseStep * s) / projection.pixelsPerUnit
                GridLegendData(worldStep, worldStep, step, step)
            }
            if (gridLegend != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    color = SurfaceStrong.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    elevation = 0.dp,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp),
                    ) {
                        Text(
                            text = gridLegend.worldX.formatFixed(2),
                            style = MaterialTheme.typography.caption,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        val density = androidx.compose.ui.platform.LocalDensity.current
                        val canvasWidth = with(density) { gridLegend.stepX.toDp().coerceIn(20.dp, 120.dp) }
                        Canvas(modifier = Modifier.size(canvasWidth, 10.dp)) {
                            val strokeWidth = 1.5.dp.toPx()
                            val color = TextPrimary.copy(alpha = 0.7f)

                            val startX = 0f
                            val endX = size.width
                            val centerY = size.height / 2f
                            val tickHeight = 4.dp.toPx()

                            // Draw horizontal line
                            drawLine(
                                color = color,
                                start = Offset(startX, centerY),
                                end = Offset(endX, centerY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                            // Draw left tick
                            drawLine(
                                color = color,
                                start = Offset(startX + strokeWidth / 2, centerY - tickHeight),
                                end = Offset(startX + strokeWidth / 2, centerY + tickHeight),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                            // Draw right tick
                            drawLine(
                                color = color,
                                start = Offset(endX - strokeWidth / 2, centerY - tickHeight),
                                end = Offset(endX - strokeWidth / 2, centerY + tickHeight),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }
        }
    }
}
internal fun DrawScope.drawGrid(canvasSize: Size, camera: ViewportCameraState) {
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    val origin = center + camera.pan

    val baseStep = min(
        canvasSize.width / GridVerticalDivisions.toFloat(),
        canvasSize.height / GridHorizontalDivisions.toFloat(),
    )
    var step = baseStep * camera.zoom

    // Prevent the grid from becoming too dense when zoomed out
    while (step < 10f) {
        step *= 2f
    }
    // Prevent the grid from becoming too sparse when zoomed in
    while (step > 100f) {
        step /= 2f
    }

    val startX = (origin.x % step) - step
    var currentX = startX
    while (currentX < canvasSize.width) {
        drawLine(
            color = Outline.copy(alpha = 0.32f),
            start = Offset(currentX, 0f),
            end = Offset(currentX, canvasSize.height),
            strokeWidth = 1f,
        )
        currentX += step
    }

    val startY = (origin.y % step) - step
    var currentY = startY
    while (currentY < canvasSize.height) {
        drawLine(
            color = Outline.copy(alpha = 0.28f),
            start = Offset(0f, currentY),
            end = Offset(canvasSize.width, currentY),
            strokeWidth = 1f,
        )
        currentY += step
    }

    // Draw origin axes if they are visible
    if (origin.x in 0f..canvasSize.width) {
        drawLine(
            color = Outline.copy(alpha = 0.7f),
            start = Offset(origin.x, 0f),
            end = Offset(origin.x, canvasSize.height),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round,
        )
    }
    if (origin.y in 0f..canvasSize.height) {
        drawLine(
            color = Outline.copy(alpha = 0.7f),
            start = Offset(0f, origin.y),
            end = Offset(canvasSize.width, origin.y),
            strokeWidth = 1.6f,
            cap = StrokeCap.Round,
        )
    }
}

private data class GridLegendData(val worldX: Double, val worldY: Double, val stepX: Float, val stepY: Float)

private fun createSelectionRect(anchor: Offset, current: Offset): Rect = Rect(
    left = min(anchor.x, current.x),
    top = min(anchor.y, current.y),
    right = max(anchor.x, current.x),
    bottom = max(anchor.y, current.y),
)

private fun findHitNode(
    baseNodes: List<RenderedNode>,
    viewportSize: IntSize,
    camera: ViewportCameraState,
    tapOffset: Offset,
    tapThresholdPx: Float,
): RenderedNode? {
    val tapInBaseSpace = tapOffset
        .toWorldPosition(viewportSize, camera)
        .toScreenPosition(viewportSize, ViewportCameraState())
    return baseNodes
        .minByOrNull { node -> node.center.distanceTo(tapInBaseSpace) }
        ?.takeIf { node -> node.center.distanceTo(tapInBaseSpace) <= tapThresholdPx / camera.zoom }
}

private fun screenDeltaToWorldDelta(
    anchor: Offset,
    current: Offset,
    viewportSize: IntSize,
    camera: ViewportCameraState,
    projection: ViewportProjection,
): Pair<Double, Double> {
    val anchorInBaseSpace = anchor
        .toWorldPosition(viewportSize, camera)
        .toScreenPosition(viewportSize, ViewportCameraState())
    val currentInBaseSpace = current
        .toWorldPosition(viewportSize, camera)
        .toScreenPosition(viewportSize, ViewportCameraState())
    val baseDelta = currentInBaseSpace - anchorInBaseSpace
    return (
        baseDelta.x / projection.pixelsPerUnit
    ).toDouble() to (
        -baseDelta.y / projection.pixelsPerUnit
    ).toDouble()
}
