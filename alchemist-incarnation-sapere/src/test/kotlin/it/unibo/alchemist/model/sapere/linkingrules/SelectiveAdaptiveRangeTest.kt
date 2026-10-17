/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.linkingrules

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectiveAdaptiveRangeTest {
    private val environment = Continuous2DEnvironment(SAPEREIncarnation())

    private val infrastructure = LsaMolecule(SelectiveAdaptiveRange.DEFAULT_MOLECULETYPE)

    init {
        environment.networkModel = with(environment) {
            selectiveAdaptiveRange(
                radius = 1.0,
                desiredNeighborsCount = 1,
                tolerance = 0,
                minRange = 1.0,
                maxRange = 10.0,
                adjustment = 1.0,
            )
        }
    }

    @Test
    fun `nodes carrying the filter molecule adapt their ranges until they are linked`() {
        val first = infrastructureNodeAt(0, 0)
        val second = infrastructureNodeAt(3, 0)
        assertEquals(listOf<Node<List<ILsaMolecule>>>(second), neighborsOf(first))
        assertEquals(listOf<Node<List<ILsaMolecule>>>(first), neighborsOf(second))
    }

    @Test
    fun `a node losing the filter molecule loses its links`() {
        val first = infrastructureNodeAt(0, 0)
        val second = infrastructureNodeAt(3, 0)
        second.removeOneMatchedLsa(infrastructure)
        assertEquals(emptyList(), neighborsOf(first))
    }

    private fun infrastructureNodeAt(x: Number, y: Number): LsaNode = LsaNode(environment).also {
        it.addLsa(infrastructure)
        environment.addNode(it, environment.makePosition(x, y))
    }

    private fun neighborsOf(node: Node<List<ILsaMolecule>>): List<Node<List<ILsaMolecule>>> =
        environment.neighborhoodOf(node).current.neighbors.toList()
}
