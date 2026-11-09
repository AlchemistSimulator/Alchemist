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

/**
 * State for the right-side inspector panel.
 */
@Immutable
sealed interface InspectorState {

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
     */
    @Immutable
    data class NodeGroup(
        val nodeIds: ImmutableList<Int>,
        val title: String = "Selected Nodes",
        val subtitle: String = "${nodeIds.size} nodes selected",
        val position: ImmutableList<Labeled<Double>>,
        val concentrations: ImmutableList<Labeled<Any?>>,
    ) : InspectorState
}
