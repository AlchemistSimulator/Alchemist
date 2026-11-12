/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.toViewport
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.TestSimulations
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.runningInBackground
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.runBlocking

class DesktopAlchemistUiNodeMoveTest {
    @Test
    fun `node move commits translated positions through the simulator and refreshes the scene`() {
        val simulation = TestSimulations.ticking(0.0 to 0.0, 2.0 to 3.0)
        val environment = simulation.environment
        val (firstNode, secondNode) = environment.nodes.current.sortedBy { environment.currentPositionOf(it).x }
        val selection = listOf(firstNode.id, secondNode.id)
        val store = ComposeUiStateStore(AlchemistUiState(scene = environment.toViewport()).withSelection(selection))
        val callback = DesktopAlchemistUiCallback(simulation, store)
        simulation.runningInBackground {
            runBlocking {
                callback.onNodesMoved(
                    listOf(
                        NodePositionUpdate(firstNode.id, persistentListOf(1.5, -2.0)),
                        NodePositionUpdate(secondNode.id, persistentListOf(3.5, 1.0)),
                    ),
                )
            }
        }
        assertEquals(listOf(1.5, -2.0), environment.currentPositionOf(firstNode).coordinates.toList())
        assertEquals(listOf(3.5, 1.0), environment.currentPositionOf(secondNode).coordinates.toList())
        assertEquals(selection, store.state.selectedNodeIds)
        assertEquals(listOf(1.5, -2.0), store.state.scene.nodes.first { it.id == firstNode.id }.coordinates)
        assertEquals(listOf(3.5, 1.0), store.state.scene.nodes.first { it.id == secondNode.id }.coordinates)
    }
}
