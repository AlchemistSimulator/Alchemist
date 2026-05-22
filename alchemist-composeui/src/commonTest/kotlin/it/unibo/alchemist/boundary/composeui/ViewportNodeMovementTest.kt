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
import it.unibo.alchemist.boundary.composeui.model.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ViewportNodeMovementTest {
    @Test
    fun `translate selected nodes applies one delta and preserves trailing coordinates`() {
        val scene = ViewportScene(
            nodes = listOf(
                ViewportNode(id = 1, coordinates = persistentListOf(0.0, 1.0, 7.0)),
                ViewportNode(id = 2, coordinates = persistentListOf(3.0, -2.0, 9.0)),
                ViewportNode(id = 3, coordinates = persistentListOf(10.0, 10.0, 11.0)),
            ).toImmutableList(),
        )

        val moved = scene.translateSelectedNodes(listOf(1, 2), deltaX = 1.5, deltaY = -0.5)

        assertEquals(listOf(1.5, 0.5, 7.0), moved.nodes.first { it.id == 1 }.coordinates)
        assertEquals(listOf(4.5, -2.5, 9.0), moved.nodes.first { it.id == 2 }.coordinates)
        assertEquals(listOf(10.0, 10.0, 11.0), moved.nodes.first { it.id == 3 }.coordinates)
    }

    @Test
    fun `with moved nodes replaces only targeted coordinates`() {
        val scene = ViewportScene(
            nodes = listOf(
                ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0)),
                ViewportNode(id = 2, coordinates = persistentListOf(1.0, 1.0)),
            ).toImmutableList(),
        )

        val moved = scene.withMovedNodes(
            listOf(NodePositionUpdate(nodeId = 2, coordinates = persistentListOf(8.0, -3.0))),
        )

        assertEquals(listOf(0.0, 0.0), moved.nodes.first { it.id == 1 }.coordinates)
        assertEquals(listOf(8.0, -3.0), moved.nodes.first { it.id == 2 }.coordinates)
    }

    @Test
    fun `selection inspector reflects moved group bounds after commit`() {
        val state = AlchemistUiState(
            scene = ViewportScene(
                nodes = listOf(
                    ViewportNode(id = 1, coordinates = persistentListOf(0.0, 1.0)),
                    ViewportNode(id = 2, coordinates = persistentListOf(4.0, 3.0)),
                ).toImmutableList(),
            ),
            selectedNodeIds = persistentListOf(1, 2),
        )

        val moved = state
            .copy(scene = state.scene.translateSelectedNodes(state.selectedNodeIds, deltaX = 2.0, deltaY = -1.5))
            .withSelection(state.selectedNodeIds)

        val inspector = assertIs<GroupInspectorState>(moved.inspector)
        assertEquals(listOf(1, 2), moved.selectedNodeIds)
        assertEquals("2.000", inspector.position.first { it.label == "Min X" }.value)
        assertEquals("6.000", inspector.position.first { it.label == "Max X" }.value)
        assertEquals("-0.500", inspector.position.first { it.label == "Min Y" }.value)
        assertEquals("1.500", inspector.position.first { it.label == "Max Y" }.value)
    }

    @Test
    fun `mutable coordinate inputs are copied before entering viewport state`() {
        val mutableCoordinates = mutableListOf(1.0, 2.0)
        val node = ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0))
            .withCoordinates(mutableCoordinates)

        mutableCoordinates[0] = 99.0

        assertEquals(persistentListOf(1.0, 2.0), node.coordinates)
    }
}
