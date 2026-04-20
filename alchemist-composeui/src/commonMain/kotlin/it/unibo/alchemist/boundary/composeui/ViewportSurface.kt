/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ViewportSurface(
    scene: ViewportScene,
    selectedNodeId: Int?,
    callbacks: AlchemistUiCallbacks,
    modifier: Modifier = Modifier,
) {
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var camera by remember { mutableStateOf(ViewportCameraState()) }
    var fixedProjection by remember { mutableStateOf<ViewportProjection?>(null) }
    var middleDragAnchor by remember { mutableStateOf<Offset?>(null) }
    val candidateProjection = remember(scene.nodes, viewportSize) { scene.createViewportProjection(viewportSize) }
    val projection = fixedProjection ?: candidateProjection
    LaunchedEffect(candidateProjection) {
        if (fixedProjection == null && candidateProjection != null) {
            fixedProjection = candidateProjection
        }
    }
    val baseNodes = remember(scene.nodes, viewportSize, projection) { renderNodes(scene, viewportSize, projection) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tapThresholdPx = with(density) { NodeHitRadius.dp.toPx() }
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
                    .pointerInput(baseNodes, selectedNodeId) {
                        detectTapGestures { tapOffset ->
                            // We need to map base nodes to screen space to check hits accurately against radius
                            // Alternatively we can map the tap back to base node space
                            // (which is screen space with camera pan=0, zoom=1)
                            val tapInBaseSpace = tapOffset.toWorldPosition(
                                viewportSize,
                                camera,
                            ).toScreenPosition(viewportSize, ViewportCameraState())
                            // Wait, baseNodes are already in the "camera at 0, zoom 1" screen space.
                            // So if we take the tapOffset, we just need to convert it
                            // to that same space to measure distance!
                            val hit = baseNodes
                                .minByOrNull { node -> node.center.distanceTo(tapInBaseSpace) }
                                ?.takeIf { node ->
                                    node.center.distanceTo(tapInBaseSpace) <=
                                        tapThresholdPx / camera.zoom
                                }
                            if (hit != null) {
                                coroutineScope.launch { callbacks.onNodeSelected(hit.node.id) }
                            } else {
                                coroutineScope.launch { callbacks.onInspectorDismiss() }
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Press) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        middleDragAnchor = if (event.buttons.isTertiaryPressed) {
                            change.position
                        } else {
                            null
                        }
                    }
                    .onPointerEvent(PointerEventType.Move) { event ->
                        val change = event.changes.firstOrNull() ?: return@onPointerEvent
                        if (event.buttons.isTertiaryPressed) {
                            val previous = middleDragAnchor ?: change.position
                            val delta = change.position - previous
                            if (delta != Offset.Zero) {
                                camera = camera.panBy(delta)
                            }
                            middleDragAnchor = change.position
                        } else {
                            middleDragAnchor = null
                        }
                    }
                    .onPointerEvent(PointerEventType.Release) {
                        middleDragAnchor = null
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
                val mappedEdges = renderEdges(scene.edges, mappedNodes)

                if (scene.showLinks) {
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
                    val isSelected = rendered.node.id == selectedNodeId
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
                        "Click to inspect · middle-drag to pan · wheel to zoom"
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
