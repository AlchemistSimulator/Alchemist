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
import androidx.compose.ui.unit.toSize
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.util.DoubleInt
import it.unibo.alchemist.boundary.composeui.view.theme.NODE_HIT_RADIUS
import kotlin.math.floor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal const val FULL_EDGE_RENDER_LIMIT = 20_000
internal const val SAMPLED_EDGE_RENDER_LIMIT = 200_000
internal const val MAX_DRAWN_EDGES_PER_FRAME = 5_000
private const val SPATIAL_INDEX_THRESHOLD = 2_000
private const val SPATIAL_INDEX_CELL_SIZE = 64f

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
    val spatialIndex = if (scene.nodes.size >= SPATIAL_INDEX_THRESHOLD) {
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
    val visibleBaseRect = camera
        .toBase(Rect(Offset.Zero, viewportSize.toSize()), viewportSize)
        .inflate(NODE_HIT_RADIUS / camera.zoom.coerceAtLeast(1e-6f))
    val visibleNodeIndices = cache.queryNodeIndices(visibleBaseRect)
    val mutableScreenPositions = MutableList<Offset?>(cache.baseCenters.size) { null }
    visibleNodeIndices.forEach { nodeIndex ->
        mutableScreenPositions[nodeIndex] = camera.toScreen(cache.baseCenters[nodeIndex], viewportSize)
    }
    val screenPositions = mutableScreenPositions.toImmutableList()
    val visibleEdges = if (!cache.scene.showLinks || cache.scene.linkRenderMode == LinkRenderMode.HIDDEN) {
        persistentListOf()
    } else {
        val edgeBudget = when (cache.scene.linkRenderMode) {
            LinkRenderMode.FULL -> cache.indexedEdges.size
            LinkRenderMode.SAMPLED -> MAX_DRAWN_EDGES_PER_FRAME
            LinkRenderMode.HIDDEN -> 0
        }
        cache.indexedEdges
            .asSequence()
            .filter { edge -> screenPositions[edge.fromIndex] != null && screenPositions[edge.toIndex] != null }
            .take(edgeBudget)
            .toImmutableList()
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
    val tapInBaseSpace = camera.toBase(tapOffset, viewportSize)
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
        .minByOrNull { nodeIndex -> (baseCenters[nodeIndex] - tapInBaseSpace).getDistance() }
        ?.takeIf { nodeIndex -> (baseCenters[nodeIndex] - tapInBaseSpace).getDistance() <= tapRadius }
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
    val selectionInBaseSpace = camera.toBase(selectionRect, viewportSize)
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
    private val cells = mutableMapOf<DoubleInt, MutableList<Int>>()

    init {
        positions.forEachIndexed { index, position ->
            cells.getOrPut(cellKey(position)) { mutableListOf() }.add(index)
        }
    }

    fun query(rect: Rect): ImmutableList<Int> {
        if (rect.isEmpty) {
            return persistentListOf()
        }
        val minCellX = floor(rect.left / SPATIAL_INDEX_CELL_SIZE).toInt()
        val maxCellX = floor(rect.right / SPATIAL_INDEX_CELL_SIZE).toInt()
        val minCellY = floor(rect.top / SPATIAL_INDEX_CELL_SIZE).toInt()
        val maxCellY = floor(rect.bottom / SPATIAL_INDEX_CELL_SIZE).toInt()
        val matches = mutableListOf<Int>()
        for (cellX in minCellX..maxCellX) {
            for (cellY in minCellY..maxCellY) {
                cells[DoubleInt(cellX, cellY)]?.let(matches::addAll)
            }
        }
        return matches.toImmutableList()
    }

    private fun cellKey(position: Offset): DoubleInt = DoubleInt(
        floor(position.x / SPATIAL_INDEX_CELL_SIZE).toInt(),
        floor(position.y / SPATIAL_INDEX_CELL_SIZE).toInt(),
    )
}

private fun Rect.inflate(amount: Float): Rect = Rect(
    left = left - amount,
    top = top - amount,
    right = right + amount,
    bottom = bottom + amount,
)
