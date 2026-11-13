/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

import androidx.compose.runtime.Immutable
import it.unibo.alchemist.boundary.composeui.model.InspectorState.NodeGroup
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.ViewportNodes.toWorldBounds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * State of the central scene area.
 *
 * @property nodes the nodes to draw.
 * @property edges the links to draw.
 * @property edgeCount the total number of links, drawn or not.
 * @property showLinks whether links are shown.
 * @property linkRenderMode how links are drawn.
 * @property linkRenderNotice why links are sampled or hidden, if they are.
 * @property dimensions the number of dimensions of the environment.
 * @property worldBounds the box containing all nodes; `null` if there are none.
 * @property summary the values shown in the summary bar.
 * @property message the text shown when there is nothing to draw.
 */
@Immutable
internal data class ViewportScene(
    val nodes: ImmutableList<ViewportNode> = persistentListOf(),
    val edges: ImmutableList<ViewportEdge> = persistentListOf(),
    val edgeCount: Int = edges.size,
    val showLinks: Boolean = false,
    val linkRenderMode: LinkRenderMode = LinkRenderMode.FULL,
    val linkRenderNotice: String? = null,
    val dimensions: Int = 2,
    val worldBounds: ViewportWorldBounds? = nodes.toWorldBounds(),
    val summary: ImmutableList<Labeled<Any>> = persistentListOf(),
    val message: String = "Waiting for simulation data",
) {
    /** The inspector for [selectedNodeIds]; `null` if none of them is in the scene. */
    internal fun toInspectorState(selectedNodeIds: List<Int>): InspectorState? {
        val nodesById = nodes.associateBy(ViewportNode::id)
        val selectedNodes = selectedNodeIds.mapNotNull(nodesById::get)
        return when (selectedNodes.size) {
            0 -> null
            1 -> selectedNodes.single().toInspectorState()
            else -> NodeGroup.of(selectedNodes)
        }
    }

    /** [nodeIds] without duplicates and without ids missing from the scene. */
    internal fun sanitizeSelection(nodeIds: List<Int>): ImmutableList<Int> {
        val availableIds = nodes.mapTo(hashSetOf(), ViewportNode::id)
        return nodeIds.distinct().filter { it in availableIds }.toImmutableList()
    }

    /** Moves the nodes in [nodeIds] by [deltaX] and [deltaY] on the plane. */
    internal fun translateSelectedNodes(nodeIds: Collection<Int>, deltaX: Double, deltaY: Double): ViewportScene =
        when {
            nodeIds.isEmpty() || (deltaX == 0.0 && deltaY == 0.0) -> this
            else -> {
                val selectedIds = nodeIds.toHashSet()
                withNodes(nodes.map { if (it.id in selectedIds) it.translate(deltaX, deltaY) else it })
            }
        }

    /** Moves each node in [nodePositions] to its new coordinates. */
    internal fun withMovedNodes(nodePositions: List<NodePositionUpdate>): ViewportScene = when {
        nodePositions.isEmpty() -> this
        else -> {
            val coordinatesById = nodePositions.associate { it.nodeId to it.coordinates }
            withNodes(nodes.map { node -> coordinatesById[node.id]?.let(node::withCoordinates) ?: node })
        }
    }

    private fun withNodes(updatedNodes: List<ViewportNode>): ViewportScene = copy(
        nodes = updatedNodes.toImmutableList(),
        worldBounds = updatedNodes.toWorldBounds(),
    )
}
