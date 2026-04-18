/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    val renderedNodes = remember(baseNodes, viewportSize, camera) {
        baseNodes.map { node ->
            node.copy(center = node.center.toScreenPosition(viewportSize, camera))
        }
    }
    val renderedEdges = remember(scene.edges, renderedNodes) { renderEdges(scene.edges, renderedNodes) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    val tapThresholdPx = with(density) { NodeHitRadius.dp.toPx() }
    Surface(
        modifier = modifier,
        color = Panel,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(28.dp),
        elevation = 0.dp,
    ) {
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    color = Outline.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(28.dp),
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(DeepSea.copy(alpha = 0.55f), Midnight),
                        radius = 1600f,
                    ),
                ),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates -> viewportSize = coordinates.size }
                    .pointerInput(renderedNodes, selectedNodeId) {
                        detectTapGestures { tapOffset ->
                            val hit = renderedNodes
                                .minByOrNull { node -> node.center.distanceTo(tapOffset) }
                                ?.takeIf { node -> node.center.distanceTo(tapOffset) <= tapThresholdPx }
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
                        colors = listOf(DeepSea.copy(alpha = 0.55f), Midnight),
                    ),
                )
                drawGrid(size)
                if (scene.showLinks) {
                    renderedEdges.forEach { edge ->
                        drawLine(
                            color = Outline.copy(alpha = 0.42f),
                            start = edge.start,
                            end = edge.end,
                            strokeWidth = LinkStrokeWidth.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
                renderedNodes.forEach { rendered ->
                    val isSelected = rendered.node.id == selectedNodeId
                    val nodeColor = lerp(AccentCool, Accent, rendered.node.accent)
                    if (isSelected) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.20f),
                            radius = SelectedNodeRadius.dp.toPx(),
                            center = rendered.center,
                        )
                        drawCircle(
                            color = Accent,
                            radius = SelectedNodeInnerRadius.dp.toPx(),
                            center = rendered.center,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(nodeColor, nodeColor.copy(alpha = 0.45f)),
                            center = rendered.center,
                            radius = SelectedNodeRadius.dp.toPx(),
                        ),
                        radius = NodeRadius.dp.toPx(),
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
                color = PanelStrong.copy(alpha = 0.88f),
                shape = RoundedCornerShape(18.dp),
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
        }
    }
}
internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGrid(canvasSize: Size) {
    val stepX = canvasSize.width / GridVerticalDivisions.toFloat()
    val stepY = canvasSize.height / GridHorizontalDivisions.toFloat()
    for (column in 1 until GridVerticalDivisions) {
        drawLine(
            color = Outline.copy(alpha = 0.32f),
            start = Offset(stepX * column, 0f),
            end = Offset(stepX * column, canvasSize.height),
            strokeWidth = 1f,
        )
    }
    for (row in 1 until GridHorizontalDivisions) {
        drawLine(
            color = Outline.copy(alpha = 0.28f),
            start = Offset(0f, stepY * row),
            end = Offset(canvasSize.width, stepY * row),
            strokeWidth = 1f,
        )
    }
    drawLine(
        color = Outline.copy(alpha = 0.7f),
        start = Offset(canvasSize.width / 2f, 0f),
        end = Offset(canvasSize.width / 2f, canvasSize.height),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Outline.copy(alpha = 0.7f),
        start = Offset(0f, canvasSize.height / 2f),
        end = Offset(canvasSize.width, canvasSize.height / 2f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round,
    )
}
