/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.environments

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister

class TestBioRect2DEnvironment {
    private val randomGenerator = MersenneTwister(0)
    private val incarnation = BiochemistryIncarnation()
    private val environment = BioRect2DEnvironment(incarnation).apply { linkingRule = ConnectWithinDistance(2.0) }
    private val junction = Junction("A-B", emptyMap(), emptyMap())

    private fun cellAt(x: Double, y: Double): Node<Double> = incarnation.createNode(randomGenerator, environment, null)
        .also { assertTrue(environment.addNode(it, Euclidean2DPosition(x, y))) }

    private fun linkedCells(): Pair<Node<Double>, Node<Double>> {
        val first = cellAt(0.0, 0.0)
        val second = cellAt(1.0, 0.0)
        first.cell.addJunction(junction, second)
        second.cell.addJunction(junction.reverse(), first)
        return first to second
    }

    @Test
    fun `an absolute move away from a neighbor removes the junctions with it`() {
        val (first, second) = linkedCells()
        environment.moveNodeTo(first, Euclidean2DPosition(10.0, 10.0))
        assertEquals(0, first.cell.junctionsCount)
        assertEquals(0, second.cell.junctionsCount)
    }

    @Test
    fun `a relative move away from a neighbor removes the junctions with it`() {
        val (first, second) = linkedCells()
        environment.moveNodeBy(first, Euclidean2DPosition(10.0, 10.0))
        assertEquals(0, first.cell.junctionsCount)
        assertEquals(0, second.cell.junctionsCount)
    }

    private companion object {
        private val Node<Double>.cell get() = asProperty<Double, CellProperty<*>>()
    }
}
