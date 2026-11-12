/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.util.DoubleInt
import it.unibo.alchemist.boundary.composeui.view.viewport.FULL_EDGE_RENDER_LIMIT
import it.unibo.alchemist.boundary.composeui.view.viewport.MAX_DRAWN_EDGES_PER_FRAME
import it.unibo.alchemist.boundary.composeui.view.viewport.SAMPLED_EDGE_RENDER_LIMIT
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import java.util.Locale
import java.util.PriorityQueue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Projects this node, as currently placed in [environment], into its viewport representation.
 */
fun <T, P : Position<P>> Node<T>.toViewport(environment: Environment<T, P>): ViewportNode = ViewportNode(
    id = id,
    coordinates = environment.currentPositionOf(this).coordinates.toList().toImmutableList(),
    concentrations = contents.current
        .map { (molecule, concentration) -> Labeled(molecule.toString(), concentration) }
        .toImmutableList(),
)

/**
 * Projects the current state of this environment into a viewport scene,
 * including the links between neighbors if [renderLinks] is true.
 */
fun <T, P : Position<P>> Environment<T, P>.toViewport(renderLinks: Boolean = false): ViewportScene {
    val viewportNodes = nodes.current.map { it.toViewport(this) }.toImmutableList()
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

/**
 * Maps the state of this simulation to the status displayed by the UI.
 */
fun <T, P : Position<P>> Simulation<T, P>.toSimulationStatus(): SimulationStatus = when (state) {
    SimulationState.INIT -> SimulationStatus.INIT
    SimulationState.READY -> SimulationStatus.READY
    SimulationState.PAUSED -> SimulationStatus.PAUSED
    SimulationState.RUNNING -> SimulationStatus.RUNNING
    SimulationState.TERMINATED -> SimulationStatus.TERMINATED
}

private fun <T, P : Position<P>> Environment<T, P>.extractEdgeSnapshot(renderLinks: Boolean): EdgeSnapshot {
    if (!renderLinks) {
        return EdgeSnapshot()
    }
    return collectEdgeSnapshot(
        edgePairs = sequence {
            for (node in nodes.current) {
                for (neighbor in neighborhoodOf(node).current.neighbors) {
                    yield(node.id to neighbor.id)
                }
            }
        },
    )
}

internal fun collectEdgeSnapshot(edgePairs: Sequence<Pair<Int, Int>>, renderLinks: Boolean = true): EdgeSnapshot {
    val uniqueEdges = edgePairs
        .takeIf { renderLinks }
        .orEmpty()
        .mapNotNull { (firstNodeId, secondNodeId) -> canonicalEdgeKey(firstNodeId, secondNodeId) }
        .distinct()
        .take(SAMPLED_EDGE_RENDER_LIMIT + 1)
        .toList()
    return when {
        uniqueEdges.size > SAMPLED_EDGE_RENDER_LIMIT -> EdgeSnapshot(
            renderMode = LinkRenderMode.HIDDEN,
            edgeCount = uniqueEdges.size,
            notice = "links hidden above ${SAMPLED_EDGE_RENDER_LIMIT.toReadableCount()}",
        )
        uniqueEdges.size <= FULL_EDGE_RENDER_LIMIT -> EdgeSnapshot(
            edges = uniqueEdges.map { it.toViewportEdge() }.toImmutableList(),
            edgeCount = uniqueEdges.size,
            renderMode = LinkRenderMode.FULL,
        )
        else -> EdgeSnapshot(
            edges = uniqueEdges
                .fold(
                    PriorityQueue<SampledViewportEdge>(
                        MAX_DRAWN_EDGES_PER_FRAME,
                        compareByDescending<SampledViewportEdge> { it.score },
                    ),
                ) { sample, edgeKey -> sample.apply { consider(edgeKey) } }
                .sortedBy(SampledViewportEdge::score)
                .map(SampledViewportEdge::edge)
                .toImmutableList(),
            edgeCount = uniqueEdges.size,
            renderMode = LinkRenderMode.SAMPLED,
            notice = "showing ${MAX_DRAWN_EDGES_PER_FRAME.toReadableCount()} sampled links",
        )
    }
}

private fun PriorityQueue<SampledViewportEdge>.consider(edgeKey: DoubleInt) {
    val candidate = SampledViewportEdge(score = edgeKey.packed.sampleScore(), edge = edgeKey.toViewportEdge())
    if (size < MAX_DRAWN_EDGES_PER_FRAME) {
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

private fun Int.toReadableCount(): String = "%,d".format(Locale.ROOT, this)

/**
 * Scrambles this edge key with the MurmurHash3 64-bit finalizer,
 * so that sampling by lowest score picks a stable, well-spread subset of the edges.
 */
private fun Long.sampleScore(): Long {
    fun Long.mix(): Long = this xor (this ushr FMIX_SHIFT)
    return ((mix() * FMIX_FIRST_MULTIPLIER).mix() * FMIX_SECOND_MULTIPLIER).mix()
}

private fun DoubleInt.toViewportEdge(): ViewportEdge = ViewportEdge(fromNodeId = high, toNodeId = low)

private fun canonicalEdgeKey(firstNodeId: Int, secondNodeId: Int): DoubleInt? = when {
    firstNodeId == secondNodeId -> null
    firstNodeId < secondNodeId -> DoubleInt(firstNodeId, secondNodeId)
    else -> DoubleInt(secondNodeId, firstNodeId)
}

internal fun canonicalEdge(firstNodeId: Int, secondNodeId: Int): ViewportEdge? =
    canonicalEdgeKey(firstNodeId, secondNodeId)?.toViewportEdge()

private const val FMIX_SHIFT = 33
private const val FMIX_FIRST_MULTIPLIER = -0xae502812aa7333L
private const val FMIX_SECOND_MULTIPLIER = -0x3b314601e57a13adL
