/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.collections.immutable.persistentListOf

class ValueEditorStateTest {
    private val longText = "a".repeat(LONG_LENGTH)
    private val scene = ViewportScene(
        nodes = persistentListOf(
            node(1, Labeled("x", longText)),
            node(2, Labeled("x", 2.5)),
            node(3),
        ),
    )

    @Test
    fun `one node shows its whole value and its type`() {
        val editor = assertNotNull(stateWith(listOf(1)).valueEditor)
        assertEquals("Node 1", editor.subtitle)
        assertEquals(longText, editor.valueText)
        assertEquals(longText, editor.sharedText)
        assertEquals("String", editor.typeText)
    }

    @Test
    fun `different values are listed per node, with every type and the missing nodes`() {
        val editor = assertNotNull(stateWith(listOf(1, 2, 3)).valueEditor)
        assertEquals("3 nodes", editor.subtitle)
        assertEquals("Node 1: $longText\nNode 2: 2.5\nNode 3: absent", editor.valueText)
        assertEquals("", editor.sharedText)
        assertEquals("String, Double; absent in 1 of 3 nodes", editor.typeText)
    }

    @Test
    fun `the editor shows the new value and type after the scene changes`() {
        val state = stateWith(listOf(1))
        val changed = state.copy(scene = state.scene.withEditedNodes(listOf(1)) { it.withConcentration("x", 2.5) })
        val editor = assertNotNull(changed.withSelection(changed.selectedNodeIds).valueEditor)
        assertEquals("2.5", editor.valueText)
        assertEquals("Double", editor.typeText)
    }

    @Test
    fun `opening the editor of the same nodes again closes it, other nodes replace it`() {
        val state = stateWith(listOf(1, 2))
        assertNull(state.withValueEditorToggled(listOf(2, 1), "x").valueEditor)
        assertEquals(listOf(1), assertNotNull(state.withValueEditorToggled(listOf(1), "x").valueEditor).nodeIds)
    }

    @Test
    fun `the editor keeps only the selected nodes and closes without any`() {
        val state = stateWith(listOf(1, 2))
        assertEquals(listOf(2), assertNotNull(state.withSelection(listOf(2, 3)).valueEditor).nodeIds)
        assertNull(state.withSelection(listOf(3)).valueEditor)
    }

    private fun stateWith(nodeIds: List<Int>) =
        AlchemistUiState(scene = scene).withSelection(nodeIds).withValueEditorToggled(nodeIds, "x")

    private companion object {
        const val LONG_LENGTH = 500

        fun node(id: Int, vararg concentrations: Labeled<Any?>) = ViewportNode(
            id = id,
            coordinates = persistentListOf(0.0, 0.0),
            concentrations = persistentListOf(*concentrations),
        )
    }
}
