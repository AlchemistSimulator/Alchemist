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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

/**
 * State for the right-side inspector panel.
 */
@Immutable
internal sealed interface InspectorState {

    /**
     * Inspector state for a single node.
     *
     * @property nodeId the node id.
     * @property title the title.
     * @property subtitle the subtitle.
     * @property position the node position.
     * @property concentrations the node contents, by molecule.
     * @property metadata extra details about the node.
     */
    @Immutable
    data class SingleNode(
        val nodeId: Int,
        val title: String = "Node $nodeId",
        val subtitle: String,
        val position: ImmutableList<Labeled<Double>>,
        val concentrations: ImmutableList<Labeled<Any?>>,
        val metadata: ImmutableList<Labeled<Any?>>,
    ) : InspectorState

    /**
     * Inspector state for a group of selected nodes.
     *
     * @property nodeIds the selected node ids.
     * @property title the title.
     * @property subtitle the subtitle.
     * @property position the box containing the nodes.
     * @property concentrations each molecule, with its value if all nodes share it.
     * @property valuesByNode for each molecule, its value in every node of the group.
     */
    @Immutable
    data class NodeGroup(
        val nodeIds: ImmutableList<Int>,
        val title: String = "Selected Nodes",
        val subtitle: String = "${nodeIds.size} nodes selected",
        val position: ImmutableList<Labeled<Double>>,
        val concentrations: ImmutableList<Labeled<Any?>>,
        val valuesByNode: ImmutableMap<String, ImmutableList<NodeValue>> = persistentMapOf(),
    ) : InspectorState {
        internal companion object {
            /** Shown for a molecule whose value differs across the group. */
            const val MIXED_CONCENTRATION_PLACEHOLDER = "Mixed"

            /** The inspector for [nodes]. */
            fun of(nodes: List<ViewportNode>): NodeGroup {
                val xs = nodes.map { it.coordinates[0] }
                val ys = nodes.map { it.coordinates[1] }
                val valuesByNode = nodes
                    .flatMap { node -> node.concentrations.map(Labeled<Any?>::label) }
                    .distinct()
                    .sorted()
                    .associateWith { molecule ->
                        nodes.map { NodeValue.of(it, molecule) }.toImmutableList()
                    }
                val concentrations = valuesByNode.map { (molecule, values) ->
                    val shared = values.map(NodeValue::value).distinct().singleOrNull()
                    Labeled(molecule, shared ?: MIXED_CONCENTRATION_PLACEHOLDER)
                }
                return NodeGroup(
                    nodeIds = nodes.map(ViewportNode::id).toImmutableList(),
                    position = persistentListOf(
                        Labeled("Min X", xs.min()),
                        Labeled("Max X", xs.max()),
                        Labeled("Min Y", ys.min()),
                        Labeled("Max Y", ys.max()),
                    ),
                    concentrations = concentrations.toImmutableList(),
                    valuesByNode = valuesByNode.toImmutableMap(),
                )
            }
        }
    }
}
