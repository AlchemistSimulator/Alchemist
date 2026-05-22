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
import it.unibo.alchemist.boundary.composeui.model.GroupInspectorState
import it.unibo.alchemist.boundary.composeui.model.InfoField
import it.unibo.alchemist.boundary.composeui.model.NodeInspectorState
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class GroupInspectorStateTest {
    @Test
    fun `group inspector aggregates bounds and concentrations`() {
        val scene = ViewportScene(
            nodes = listOf(
                ViewportNode(
                    id = 10,
                    coordinates = persistentListOf(-2.0, 5.0),
                    concentrations = persistentListOf(
                        InfoField("shared", "1"),
                        InfoField("variant", "A"),
                    ),
                ),
                ViewportNode(
                    id = 20,
                    coordinates = persistentListOf(4.0, -1.0),
                    concentrations = persistentListOf(
                        InfoField("shared", "1"),
                        InfoField("variant", "B"),
                        InfoField("partial", "yes"),
                    ),
                ),
            ).toImmutableList(),
        )

        val inspector = assertIs<GroupInspectorState>(scene.toInspectorState(listOf(10, 20)))

        assertEquals(listOf(10, 20), inspector.nodeIds)
        assertEquals("-2.000", inspector.position.first { it.label == "Min X" }.value)
        assertEquals("4.000", inspector.position.first { it.label == "Max X" }.value)
        assertEquals("-1.000", inspector.position.first { it.label == "Min Y" }.value)
        assertEquals("5.000", inspector.position.first { it.label == "Max Y" }.value)
        assertEquals("1", inspector.concentrations.first { it.label == "shared" }.value)
        assertEquals(MIXED_CONCENTRATION_PLACEHOLDER, inspector.concentrations.first { it.label == "variant" }.value)
        assertEquals(MIXED_CONCENTRATION_PLACEHOLDER, inspector.concentrations.first { it.label == "partial" }.value)
    }

    @Test
    fun `selection is cleared when all selected nodes disappear from the scene`() {
        val state = AlchemistUiState(
            scene = ViewportScene(
                nodes = persistentListOf(ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0))),
            ),
            selectedNodeIds = persistentListOf(1),
            inspector = NodeInspectorState(
                nodeId = 1,
                subtitle = "Live node snapshot",
                position = persistentListOf(),
                concentrations = persistentListOf(),
                metadata = persistentListOf(),
            ),
        )

        val refreshed = state.copy(scene = ViewportScene()).withSelection(state.selectedNodeIds)

        assertEquals(emptyList(), refreshed.selectedNodeIds)
        assertNull(refreshed.inspector)
    }
}
