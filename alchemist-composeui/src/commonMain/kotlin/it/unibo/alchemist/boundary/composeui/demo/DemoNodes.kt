/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore

/**
 * Changes to the nodes of the demo scene, applied to the scene directly.
 * Numeric values are stored as numbers, anything else as text.
 */
internal class DemoNodes(private val store: ComposeUiStateStore) : AlchemistUiCallbacks.Nodes {
    override suspend fun move(nodePositions: List<NodePositionUpdate>) = store.update { state ->
        state.copy(scene = state.scene.withMovedNodes(nodePositions)).withSelection(state.selectedNodeIds)
    }

    override suspend fun clone(copies: List<NodePositionUpdate>) = store.update { state ->
        state.copy(scene = state.scene.withClonedNodes(copies)).withSelection(state.selectedNodeIds)
    }

    override suspend fun delete(nodeIds: List<Int>) = store.update { state ->
        state.copy(scene = state.scene.withoutNodes(nodeIds)).withSelection(state.selectedNodeIds)
    }

    override suspend fun setConcentration(nodeIds: List<Int>, molecule: String, value: String) = runCatching {
        edit(nodeIds) { it.withConcentration(molecule, value.toDoubleOrNull() ?: value) }
    }

    override suspend fun removeMolecule(nodeIds: List<Int>, molecule: String) =
        edit(nodeIds) { it.withoutMolecule(molecule) }

    private fun edit(nodeIds: List<Int>, change: (ViewportNode) -> ViewportNode) = store.update { state ->
        state.copy(scene = state.scene.withEditedNodes(nodeIds, change)).withSelection(state.selectedNodeIds)
    }
}
