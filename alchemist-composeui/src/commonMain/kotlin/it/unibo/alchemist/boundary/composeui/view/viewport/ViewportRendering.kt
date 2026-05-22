/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:Suppress("ktlint:standard:property-naming", "ktlint:standard:function-naming")

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.theme.NodeHitRadius
import it.unibo.alchemist.boundary.composeui.view.theme.ZoomStep
import kotlin.math.floor
import kotlin.math.sqrt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal const val FullEdgeRenderLimit = 20_000
internal const val SampledEdgeRenderLimit = 200_000
internal const val MaxDrawnEdgesPerFrame = 5_000
private const val SpatialIndexThreshold = 2_000
private const val SpatialIndexCellSize = 64f

internal fun buildViewportSceneCache(
    scene: ViewportScene,
    viewportSize: IntSize,
    projection: ViewportProjection?,
): ViewportSceneCache {
    if (projection == null || scene.nodes.isEmpty() || viewportSize.width == 0 || viewportSize.height == 0) {
        return ViewportSceneCache(scene = scene)
    }
    val baseCenters = scene.nodes.map { node -> node.toViewportPosition(viewportSize, projection) }.toImmutableList()
    val nodeIndexById = scene.nodes.withIndex().associate { (index, node) -> node.id to index }
    val indexedEdges = scene.edges.mapNotNull { edge ->
        val fromIndex = nodeIndexById[edge.fromNodeId] ?: return@mapNotNull null
        val toIndex = nodeIndexById[edge.toNodeId] ?: return@mapNotNull null
        IndexedEdge(fromIndex = fromIndex, toIndex = toIndex)
    }.toImmutableList()
    val spatialIndex = if (scene.nodes.size >= SpatialIndexThreshold) {
        NodeSpatialIndex(baseCenters)
    } else {
        null
    }
    return ViewportSceneCache(
        scene = scene,
        baseCenters = baseCenters,
        indexedEdges = indexedEdges,
        spatialIndex = spatialIndex,
    )
}

internal fun buildViewportFrame(
    cache: ViewportSceneCache,
    viewportSize: IntSize,
    camera: ViewportCameraState,
): ViewportFrame {
    if (cache.baseCenters.isEmpty() || viewportSize.width == 0 || viewportSize.height == 0) {
        return ViewportFrame.empty(cache.baseCenters.size)
    }
    val visibleBaseRect = viewportSize
        .toBaseRect(camera)
        .inflate(NodeHitRadius / camera.zoom.coerceAtLeast(1e-6f))
    val visibleNodeIndices = cache.queryNodeIndices(visibleBaseRect)
    val mutableScreenPositions = MutableList<Offset?>(cache.baseCenters.size) { null }
    visibleNodeIndices.forEach { nodeIndex ->
        mutableScreenPositions[nodeIndex] = cache.baseCenters[nodeIndex].toScreenPosition(viewportSize, camera)
    }
    val screenPositions = mutableScreenPositions.toImmutableList()
    val visibleEdges = if (!cache.scene.showLinks || cache.scene.linkRenderMode == LinkRenderMode.HIDDEN) {
        persistentListOf()
    } else {
        val edgeBudget = when (cache.scene.linkRenderMode) {
            LinkRenderMode.FULL -> cache.indexedEdges.size
            LinkRenderMode.SAMPLED -> MaxDrawnEdgesPerFrame
            LinkRenderMode.HIDDEN -> 0
        }
        buildList(minOf(cache.indexedEdges.size, edgeBudget)) {
            for (edge in cache.indexedEdges) {
                if (size >= edgeBudget) {
                    break
                }
                if (screenPositions[edge.fromIndex] != null && screenPositions[edge.toIndex] != null) {
                    add(edge)
                }
            }
        }.toImmutableList()
    }
    return ViewportFrame(
        screenPositions = screenPositions,
        visibleNodeIndices = visibleNodeIndices,
        visibleEdges = visibleEdges,
    )
}

internal fun ViewportSceneCache.findHitNode(
    viewportSize: IntSize,
    camera: ViewportCameraState,
    tapOffset: Offset,
    tapThresholdPx: Float,
): ViewportNode? {
    if (baseCenters.isEmpty()) {
        return null
    }
    val tapInBaseSpace = tapOffset.toBasePosition(viewportSize, camera)
    val tapRadius = tapThresholdPx / camera.zoom.coerceAtLeast(1e-6f)
    val candidateIndices = queryNodeIndices(
        Rect(
            left = tapInBaseSpace.x - tapRadius,
            top = tapInBaseSpace.y - tapRadius,
            right = tapInBaseSpace.x + tapRadius,
            bottom = tapInBaseSpace.y + tapRadius,
        ),
    )
    val bestMatch = candidateIndices
        .minByOrNull { nodeIndex -> baseCenters[nodeIndex].distanceTo(tapInBaseSpace) }
        ?.takeIf { nodeIndex -> baseCenters[nodeIndex].distanceTo(tapInBaseSpace) <= tapRadius }
    return bestMatch?.let(scene.nodes::get)
}

internal fun ViewportSceneCache.selectNodes(
    viewportSize: IntSize,
    camera: ViewportCameraState,
    selectionRect: Rect,
): List<Int> {
    if (baseCenters.isEmpty()) {
        return emptyList()
    }
    val selectionInBaseSpace = selectionRect.toBaseRect(viewportSize, camera)
    return queryNodeIndices(selectionInBaseSpace)
        .filter { nodeIndex -> selectionInBaseSpace.contains(baseCenters[nodeIndex]) }
        .map { nodeIndex -> scene.nodes[nodeIndex].id }
}

internal data class ViewportSceneCache(
    val scene: ViewportScene,
    val baseCenters: ImmutableList<Offset> = persistentListOf(),
    val indexedEdges: ImmutableList<IndexedEdge> = persistentListOf(),
    val spatialIndex: NodeSpatialIndex? = null,
) {
    fun queryNodeIndices(rect: Rect): ImmutableList<Int> =
        spatialIndex?.query(rect) ?: baseCenters.indices.filter { index ->
            rect.contains(baseCenters[index])
        }.toImmutableList()
}

@Immutable
internal data class ViewportFrame(
    val screenPositions: ImmutableList<Offset?>,
    val visibleNodeIndices: ImmutableList<Int>,
    val visibleEdges: ImmutableList<IndexedEdge>,
) {
    companion object {
        fun empty(nodeCount: Int): ViewportFrame = ViewportFrame(
            screenPositions = List(nodeCount) { null }.toImmutableList(),
            visibleNodeIndices = persistentListOf(),
            visibleEdges = persistentListOf(),
        )
    }
}

@Immutable
internal data class IndexedEdge(val fromIndex: Int, val toIndex: Int)

internal class NodeSpatialIndex(positions: List<Offset>) {
    private val cells = mutableMapOf<Long, MutableList<Int>>()

    init {
        positions.forEachIndexed { index, position ->
            cells.getOrPut(cellKey(position)) { mutableListOf() }.add(index)
        }
    }

    fun query(rect: Rect): ImmutableList<Int> {
        if (rect.isEmpty) {
            return persistentListOf()
        }
        val minCellX = floor(rect.left / SpatialIndexCellSize).toInt()
        val maxCellX = floor(rect.right / SpatialIndexCellSize).toInt()
        val minCellY = floor(rect.top / SpatialIndexCellSize).toInt()
        val maxCellY = floor(rect.bottom / SpatialIndexCellSize).toInt()
        val matches = mutableListOf<Int>()
        for (cellX in minCellX..maxCellX) {
            for (cellY in minCellY..maxCellY) {
                cells[cellKey(cellX, cellY)]?.let(matches::addAll)
            }
        }
        return matches.toImmutableList()
    }

    private fun cellKey(position: Offset): Long =
        cellKey(
            floor(position.x / SpatialIndexCellSize).toInt(),
            floor(position.y / SpatialIndexCellSize).toInt(),
        )

    private fun cellKey(cellX: Int, cellY: Int): Long =
        (cellX.toLong() shl 32) xor (cellY.toLong() and 0xffffffffL)
}

@Immutable
internal data class ViewportCameraState(val pan: Offset = Offset.Zero, val zoom: Float = 1f)

internal fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
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

internal fun Offset.toBasePosition(viewportSize: IntSize, camera: ViewportCameraState): Offset =
    toWorldPosition(viewportSize, camera).toScreenPosition(viewportSize, ViewportCameraState())

internal fun Rect.toBaseRect(viewportSize: IntSize, camera: ViewportCameraState): Rect {
    val topLeft = topLeft.toBasePosition(viewportSize, camera)
    val bottomRight = bottomRight.toBasePosition(viewportSize, camera)
    return Rect(
        left = minOf(topLeft.x, bottomRight.x),
        top = minOf(topLeft.y, bottomRight.y),
        right = maxOf(topLeft.x, bottomRight.x),
        bottom = maxOf(topLeft.y, bottomRight.y),
    )
}

private fun IntSize.toBaseRect(camera: ViewportCameraState): Rect = Rect(
    topLeft = Offset.Zero.toBasePosition(this, camera),
    bottomRight = Offset(width.toFloat(), height.toFloat()).toBasePosition(this, camera),
)

private fun Rect.inflate(amount: Float): Rect = Rect(
    left = left - amount,
    top = top - amount,
    right = right + amount,
    bottom = bottom + amount,
)

internal val IntSize.center: Offset
    get() = Offset(width / 2f, height / 2f)
