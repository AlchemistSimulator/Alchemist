/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironment
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.biochemistry.reactions.BiochemicalNodeReaction
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.MersenneTwister

class JunctionActionsTest {
    private val randomGenerator = MersenneTwister(0)
    private val incarnation = BiochemistryIncarnation()
    private val environment = BioRect2DEnvironment(incarnation)
    private val cell = incarnation.createNode(randomGenerator, environment, null)
    private val neighbor = incarnation.createNode(randomGenerator, environment, null)
    private val junction = Junction("A-B", emptyMap(), emptyMap())
    private val reaction =
        BiochemicalNodeReaction(cell, ExponentialTime(1.0, randomGenerator), environment, randomGenerator)

    private val Node<Double>.linkedNodes get() = asProperty<Double, CellProperty<*>>().getNeighborLinkWithJunction(
        junction,
    )

    @Test
    fun `adding a junction in the cell links the cell to the neighbor`() {
        AddJunctionInCell(environment, reaction, junction, randomGenerator).execute(neighbor)
        assertEquals(setOf(neighbor), cell.linkedNodes)
        assertEquals(emptySet(), neighbor.linkedNodes)
    }

    @Test
    fun `adding a junction in the neighbor links the neighbor to the cell`() {
        AddJunctionInNeighbor(environment, reaction, junction, randomGenerator).execute(neighbor)
        assertEquals(setOf(cell), neighbor.linkedNodes)
        assertEquals(emptySet(), cell.linkedNodes)
    }
}
