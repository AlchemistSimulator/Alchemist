/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("ktlint:standard:property-naming", "ktlint:standard:function-naming")

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

internal fun renderNodes(
    scene: ViewportScene,
    viewportSize: IntSize,
    projection: ViewportProjection?,
): List<RenderedNode> {
    if (projection == null || scene.nodes.isEmpty() || viewportSize.width == 0 || viewportSize.height == 0) {
        return emptyList()
    }
    return scene.nodes.map { node ->
        RenderedNode(node = node, center = node.toViewportPosition(viewportSize, projection))
    }
}

internal fun renderEdges(edges: List<ViewportEdge>, renderedNodes: List<RenderedNode>): List<RenderedEdge> {
    val nodesById = renderedNodes.associateBy { it.node.id }
    return edges.mapNotNull { edge ->
        val from = nodesById[edge.fromNodeId] ?: return@mapNotNull null
        val to = nodesById[edge.toNodeId] ?: return@mapNotNull null
        RenderedEdge(start = from.center, end = to.center)
    }
}

internal data class RenderedNode(val node: ViewportNode, val center: Offset)

internal data class RenderedEdge(val start: Offset, val end: Offset)

internal data class ViewportCameraState(val pan: Offset = Offset.Zero, val zoom: Float = 1f)

internal fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

internal fun Offset.toScreenPosition(viewportSize: IntSize, camera: ViewportCameraState): Offset {
    val center = viewportSize.center
    return center + ((this - center) * camera.zoom) + camera.pan
}

internal fun ViewportCameraState.panBy(delta: Offset): ViewportCameraState = copy(pan = pan + delta)

internal fun ViewportCameraState.zoomBy(
    viewportSize: IntSize,
    pivot: Offset,
    scrollDelta: Float,
): ViewportCameraState {
    val zoomFactor = when {
        scrollDelta < 0f -> ZoomStep
        scrollDelta > 0f -> 1f / ZoomStep
        else -> 1f
    }
    val targetZoom = applyInfiniteZoomFactor(zoom, zoomFactor)
    if (targetZoom == zoom) {
        return this
    }
    val center = viewportSize.center
    val worldPoint = pivot.toWorldPosition(viewportSize, this)
    val newPan = pivot - center - ((worldPoint - center) * targetZoom)
    return copy(zoom = targetZoom, pan = newPan)
}

internal fun applyInfiniteZoomFactor(currentZoom: Float, zoomFactor: Float): Float {
    if (currentZoom <= 0f || !currentZoom.isFinite() || zoomFactor <= 0f || !zoomFactor.isFinite()) {
        return currentZoom
    }
    val targetZoom = currentZoom * zoomFactor
    return when {
        targetZoom.isNaN() -> currentZoom
        targetZoom == 0f -> Float.MIN_VALUE
        targetZoom == Float.POSITIVE_INFINITY -> Float.MAX_VALUE
        else -> targetZoom
    }
}

internal fun Offset.toWorldPosition(viewportSize: IntSize, camera: ViewportCameraState): Offset {
    val center = viewportSize.center
    return center + ((this - center - camera.pan) / camera.zoom)
}

internal val IntSize.center: Offset
    get() = Offset(width / 2f, height / 2f)
