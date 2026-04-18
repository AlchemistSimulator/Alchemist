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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

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

@Immutable
internal data class RenderedNode(val node: ViewportNode, val center: Offset)

@Immutable
internal data class RenderedEdge(val start: Offset, val end: Offset)

@Immutable
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
