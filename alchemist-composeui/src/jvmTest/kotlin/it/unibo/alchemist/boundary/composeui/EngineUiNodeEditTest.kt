/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.AlchemistEnvironments.toViewport
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.util.TestSimulations
import it.unibo.alchemist.boundary.composeui.util.TestSimulations.runningInBackground
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore
import it.unibo.alchemist.model.molecules.SimpleMolecule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class EngineUiNodeEditTest {
    @Test
    fun `a set value is parsed by the incarnation and reaches every selected node`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0, 1.0 to 1.0)
        val nodes = simulation.environment.nodes.current
        val store = ComposeUiStateStore(AlchemistUiState(scene = simulation.environment.toViewport()))
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            // Protelis runs the value as a program.
            callback.nodes.setConcentration(nodes.map { it.id }, "x", "2 + 1")
        }
        nodes.forEach { assertEquals(3.0, it[MOLECULE].getOrNull()) }
        val shown = store.state.scene.nodes.flatMap { node -> node.concentrations.filter { it.label == "x" } }
        assertEquals(listOf(3.0, 3.0), shown.map { it.value })
    }

    @Test
    fun `removing a molecule skips the nodes without it`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0, 1.0 to 1.0)
        val (withMolecule, without) = simulation.environment.nodes.current.toList()
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            callback.nodes.setConcentration(listOf(withMolecule.id), "x", "1")
            callback.nodes.removeMolecule(listOf(withMolecule.id, without.id), "x")
        }
        assertFalse(MOLECULE in withMolecule)
        assertFalse(MOLECULE in without)
        assertEquals(null, store.state.controls.dialog)
    }

    @Test
    fun `an edit that fails returns why and leaves the simulation running`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0)
        val store = ComposeUiStateStore(AlchemistUiState())
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            assertTrue(callback.nodes.setConcentration(listOf(MISSING_NODE), "x", "1").isFailure)
            // Typing in the large editor sends a value at each key, so a failure must not open a dialog.
            assertNull(store.state.controls.dialog)
            callback.transport.step()
            assertTrue(simulation.step > 0)
        }
    }

    @Test
    fun `inspecting nodes sends their reactions, keeping the selection`() = runTest {
        val simulation = TestSimulations.ticking(0.0 to 0.0, 1.0 to 1.0)
        val (first, second) = simulation.environment.nodes.current.toList()
        val store = ComposeUiStateStore(
            AlchemistUiState(scene = simulation.environment.toViewport()).withSelection(listOf(first.id)),
        )
        val callback = EngineUiCallbacks(simulation, store)
        simulation.runningInBackground {
            callback.display.inspect(listOf(first.id))
        }
        val shown = store.state.scene.nodes.associate { it.id to it.reactions }
        assertEquals(first.reactions.current.size, shown[first.id]?.size)
        assertNull(shown[second.id])
        assertEquals(listOf(first.id), store.state.selectedNodeIds)
    }

    private companion object {
        const val MISSING_NODE = 42
        val MOLECULE = SimpleMolecule("x")
    }
}
