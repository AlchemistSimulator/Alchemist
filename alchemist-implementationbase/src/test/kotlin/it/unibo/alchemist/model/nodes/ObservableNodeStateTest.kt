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
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObservableNodeStateTest {
    private val environment =
        Continuous2DEnvironment(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow())
    private val molecule = SimpleMolecule("m")
    private val node = GenericNode(environment)
    private val other = SimpleMolecule("other")

    @Test
    fun `a molecule stored with a null concentration can be removed`() {
        val nullableNode = GenericNode(mockk<Environment<Any?, *>>(relaxed = true))
        nullableNode[molecule] = null
        nullableNode.removeAllConcentrationOf(molecule)
        assertTrue(nullableNode.contents.current.isEmpty())
    }

    @Test
    fun `concentrations follow setting, changing, and removing a molecule`() {
        val concentrations: List<Option<Any>> = node.concentrationOf(molecule).record()
        node[molecule] = 1
        node[other] = 5
        node[molecule] = 2
        node.removeAllConcentrationOf(molecule)
        assertEquals(listOf(None, 1.some(), 2.some(), None), concentrations)
    }

    @Test
    fun `molecule presence follows the node contents`() {
        val presence = node.containsMolecule(molecule).record()
        node[molecule] = 1
        node[molecule] = 2
        node.removeAllConcentrationOf(molecule)
        assertEquals(listOf(false, true, false), presence)
    }

    @Test
    fun `the molecule count follows the node contents`() {
        val counts = node.moleculeCount.record()
        node[molecule] = 1
        node[other] = 1
        node[molecule] = 2
        node.removeAllConcentrationOf(other)
        assertEquals(listOf(0, 1, 2, 1), counts)
    }

    private fun <T> Observable<T>.record(): List<T> = mutableListOf<T>().also { values -> onChange { values += it } }
}
