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
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import it.unibo.alchemist.boundary.composeui.util.TestSimulations
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.runningInBackground
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.model.molecules.SimpleMolecule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest

class EngineUiNodeCloneDeleteTest {
    @Test
    fun `a clone has the contents of its original, at the given position`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val environment = simulation.environment
        val original = environment.nodes.current.single()
        val callback = EngineUiCallbacks(simulation, ComposeUiStateStore(AlchemistUiState()))
        simulation.runningInBackground {
            callback.nodes.setConcentration(listOf(original.id), "x", "1")
            callback.nodes.clone(listOf(NodePositionUpdate(original.id, persistentListOf(4.0, 5.0))))
        }
        val clone = environment.nodes.current.single { it.id != original.id }
        assertEquals(listOf(4.0, 5.0), environment.currentPositionOf(clone).coordinates.toList())
        assertEquals(original[MOLECULE].getOrNull(), clone[MOLECULE].getOrNull())
    }

    @Test
    fun `deleted nodes leave the simulation and the selection`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0, 1.0 to 1.0)
        val environment = simulation.environment
        val (deleted, kept) = environment.nodes.current.toList()
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            store.update { it.withSelection(listOf(deleted.id, kept.id)) }
            callback.nodes.delete(listOf(deleted.id))
        }
        assertEquals(listOf(kept), environment.nodes.current.toList())
        assertFalse(deleted.id in store.state.selectedNodeIds)
    }

    private companion object {
        val MOLECULE = SimpleMolecule("x")
    }
}
