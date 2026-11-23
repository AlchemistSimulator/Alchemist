/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.demo.DemoUiCallbacks
import it.unibo.alchemist.boundary.composeui.demo.DemoUiState
import it.unibo.alchemist.boundary.composeui.model.InspectorState.SingleNode
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest

class NodeContentsEditTest {
    private val node = ViewportNode(
        id = 0,
        coordinates = persistentListOf(0.0, 0.0),
        concentrations = persistentListOf(Labeled("a", 1.0), Labeled("b", 2.0)),
    )

    @Test
    fun `setting a molecule already there keeps its place`() {
        assertEquals<List<Labeled<Any?>>>(
            listOf(Labeled("a", 5.0), Labeled("b", 2.0)),
            node.withConcentration("a", 5.0).concentrations,
        )
    }

    @Test
    fun `setting a new molecule adds it last`() {
        assertEquals(listOf("a", "b", "c"), node.withConcentration("c", 3.0).concentrations.map { it.label })
    }

    @Test
    fun `removing a molecule drops only that one`() {
        assertEquals<List<Labeled<Any?>>>(listOf(Labeled("b", 2.0)), node.withoutMolecule("a").concentrations)
    }

    @Test
    fun `the demo edits the selected node and refreshes the inspector`() = runTest {
        val controller = ComposeUiController(DemoUiState.initial, ::DemoUiCallbacks)
        val nodeId = controller.store.state.scene.nodes.first().id
        controller.store.update { it.withSelection(listOf(nodeId)) }

        controller.callbacks.nodes.setConcentration(listOf(nodeId), "extra", "4")

        val inspector = assertIs<SingleNode>(controller.store.state.inspector)
        assertEquals(Labeled<Any?>("extra", 4.0), inspector.concentrations.last())
    }
}
