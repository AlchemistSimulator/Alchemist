/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.view.viewport.FullEdgeRenderLimit
import it.unibo.alchemist.boundary.composeui.view.viewport.MaxDrawnEdgesPerFrame
import it.unibo.alchemist.boundary.composeui.view.viewport.SampledEdgeRenderLimit
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import java.util.PriorityQueue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun <T, P : Position<P>> Node<T>.toViewport(environment: Environment<T, P>): ViewportNode = ViewportNode(
    id = id,
    coordinates = environment.getPosition(this).coordinates.toList().toImmutableList(),
    concentrations = this.contents.map { InfoField(it.key.toString(), it.value.toString()) }.toImmutableList(),
)

fun <T, P : Position<P>> Environment<T, P>.toViewport(renderLinks: Boolean = false): ViewportScene {
    val viewportNodes = nodes.map { it.toViewport(this) }.toImmutableList()
    val edgeSnapshot = extractEdgeSnapshot(renderLinks)
    return ViewportScene(
        nodes = viewportNodes,
        edges = edgeSnapshot.edges,
        edgeCount = edgeSnapshot.edgeCount,
        linkRenderMode = edgeSnapshot.renderMode,
        linkRenderNotice = edgeSnapshot.notice,
        dimensions = dimensions,
    )
}

fun <T, P : Position<P>> Simulation<T, P>.toSimulationStatus(): SimulationStatus = when (this.status) {
    Status.INIT -> SimulationStatus.INIT
    Status.READY -> SimulationStatus.READY
    Status.PAUSED -> SimulationStatus.PAUSED
    Status.RUNNING -> SimulationStatus.RUNNING
    Status.TERMINATED -> SimulationStatus.TERMINATED
}

private fun <T, P : Position<P>> Environment<T, P>.extractEdgeSnapshot(renderLinks: Boolean): EdgeSnapshot {
    if (!renderLinks) {
        return EdgeSnapshot()
    }
    return collectEdgeSnapshot(
        edgePairs = sequence {
            for (node in nodes) {
                for (neighbor in getNeighborhood(node)) {
                    yield(node.id to neighbor.id)
                }
            }
        },
    )
}

internal fun collectEdgeSnapshot(edgePairs: Sequence<Pair<Int, Int>>, renderLinks: Boolean = true): EdgeSnapshot {
    if (!renderLinks) {
        return EdgeSnapshot()
    }
    val seenEdges = HashSet<Long>()
    val fullEdges = ArrayList<ViewportEdge>(FullEdgeRenderLimit)
    val sampledEdges = PriorityQueue<SampledViewportEdge>(
        MaxDrawnEdgesPerFrame,
        compareByDescending<SampledViewportEdge> { it.score },
    )
    var uniqueEdges = 0
    for ((firstNodeId, secondNodeId) in edgePairs) {
        val edgeKey = canonicalEdgeKey(firstNodeId, secondNodeId) ?: continue
        if (!seenEdges.add(edgeKey)) {
            continue
        }
        uniqueEdges++
        if (uniqueEdges <= FullEdgeRenderLimit) {
            fullEdges += edgeKey.toViewportEdge()
        }
        sampledEdges.consider(edgeKey)
        if (uniqueEdges > SampledEdgeRenderLimit) {
            return EdgeSnapshot(
                renderMode = LinkRenderMode.HIDDEN,
                edgeCount = uniqueEdges,
                notice = "links hidden above ${SampledEdgeRenderLimit.toReadableCount()}",
            )
        }
    }
    return when {
        uniqueEdges <= FullEdgeRenderLimit -> EdgeSnapshot(
            edges = fullEdges.toImmutableList(),
            edgeCount = uniqueEdges,
            renderMode = LinkRenderMode.FULL,
        )
        else -> EdgeSnapshot(
            edges = sampledEdges
                .toList()
                .sortedBy(SampledViewportEdge::score)
                .map(SampledViewportEdge::edge)
                .toImmutableList(),
            edgeCount = uniqueEdges,
            renderMode = LinkRenderMode.SAMPLED,
            notice = "showing ${MaxDrawnEdgesPerFrame.toReadableCount()} sampled links",
        )
    }
}

private fun PriorityQueue<SampledViewportEdge>.consider(edgeKey: Long) {
    val candidate = SampledViewportEdge(score = edgeKey.sampleScore(), edge = edgeKey.toViewportEdge())
    if (size < MaxDrawnEdgesPerFrame) {
        add(candidate)
        return
    }
    val largestScore = peek() ?: return
    if (candidate.score < largestScore.score) {
        poll()
        add(candidate)
    }
}

internal data class EdgeSnapshot(
    val edges: ImmutableList<ViewportEdge> = persistentListOf(),
    val edgeCount: Int = 0,
    val renderMode: LinkRenderMode = LinkRenderMode.FULL,
    val notice: String? = null,
)

private data class SampledViewportEdge(val score: Long, val edge: ViewportEdge)

private fun Int.toReadableCount(): String = "%,d".format(this)

private fun Long.sampleScore(): Long {
    var value = this
    value = (value xor (value ushr 33)) * -0xae502812aa7333L
    value = (value xor (value ushr 33)) * -0x3b314601e57a13adL
    return value xor (value ushr 33)
}

private fun Long.toViewportEdge(): ViewportEdge = ViewportEdge(
    fromNodeId = (this ushr 32).toInt(),
    toNodeId = this.toInt(),
)

private fun canonicalEdgeKey(firstNodeId: Int, secondNodeId: Int): Long? = when {
    firstNodeId == secondNodeId -> null
    firstNodeId < secondNodeId -> edgeKey(firstNodeId, secondNodeId)
    else -> edgeKey(secondNodeId, firstNodeId)
}

private fun edgeKey(firstNodeId: Int, secondNodeId: Int): Long =
    (firstNodeId.toLong() shl 32) or (secondNodeId.toLong() and 0xffffffffL)

internal fun canonicalEdge(firstNodeId: Int, secondNodeId: Int): ViewportEdge? =
    canonicalEdgeKey(firstNodeId, secondNodeId)?.toViewportEdge()
