/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.nodes

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableNodeStateTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())
    private val node = GenericNode(environment)
    private val molecule = SimpleMolecule("m")
    private val other = SimpleMolecule("other")

    private fun <T> Observable<T>.record(): List<T> = mutableListOf<T>().also { values -> subscribe { values += it } }

    @Test
    fun `concentrations follow setting, changing, and removing a molecule`() {
        val concentrations: List<Option<Any>> = node.observeConcentration(molecule).record()
        node.setConcentration(molecule, 1)
        node.setConcentration(other, 5)
        node.setConcentration(molecule, 2)
        node.removeConcentration(molecule)
        assertEquals(listOf(None, 1.some(), 2.some(), None), concentrations)
    }

    @Test
    fun `molecule presence follows the node contents`() {
        val presence = node.observeContains(molecule).record()
        node.setConcentration(molecule, 1)
        node.setConcentration(molecule, 2)
        node.removeConcentration(molecule)
        assertEquals(listOf(false, true, false), presence)
    }

    @Test
    fun `the molecule count follows the node contents`() {
        val counts = node.observeMoleculeCount.record()
        node.setConcentration(molecule, 1)
        node.setConcentration(other, 1)
        node.setConcentration(molecule, 2)
        node.removeConcentration(other)
        assertEquals(listOf(0, 1, 2, 1), counts)
    }
}
