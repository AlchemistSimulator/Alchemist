/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironmentNoOverlap
import it.unibo.alchemist.model.biochemistry.properties.CircularDeformableCell
import it.unibo.alchemist.model.biochemistry.reactions.BiochemicalNodeReaction
import it.unibo.alchemist.model.networks.connectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister

class TensionPresentTest {
    private val randomGenerator = MersenneTwister(0)
    private val environment = BioRect2DEnvironmentNoOverlap(BiochemistryIncarnation())
        .apply { networkModel = connectWithinDistance(1.0) }

    private fun deformableCell(maximumDiameter: Double, rigidity: Double): Node<Double> = GenericNode(environment)
        .also { node -> node.addProperty(CircularDeformableCell(environment, node, maximumDiameter, rigidity)) }

    private fun deformableCellAt(x: Double, maximumDiameter: Double, rigidity: Double): Node<Double> =
        deformableCell(maximumDiameter, rigidity).also {
            assertTrue(environment.addNode(it, Euclidean2DPosition(x, 0.0)))
        }

    private fun tensionOn(cell: Node<Double>) = TensionPresent(
        environment,
        BiochemicalNodeReaction(cell, ExponentialTime(1.0, randomGenerator), environment, randomGenerator),
    )

    @Test
    fun `a bigger cell added later widens the range where tension is observed`() {
        val cell = deformableCellAt(0.0, maximumDiameter = 1.0, rigidity = 1.0)
        val tension = tensionOn(cell)
        val validity = mutableListOf<Boolean>()
        tension.isValid.onChange { validity += it }
        assertEquals(listOf(false), validity)
        // Farther than the previous biggest diameter, but within the sum of the maximum radii (0.5 + 2).
        deformableCellAt(2.2, maximumDiameter = 4.0, rigidity = 0.5)
        assertEquals(4.0, environment.maxDiameterAmongCircularDeformableCells.current)
        assertEquals(listOf(false, true), validity)
    }

    @Test
    fun `tension is absent while there are no deformable cells in the environment`() {
        val tension = tensionOn(deformableCell(maximumDiameter = 1.0, rigidity = 1.0))
        assertEquals(0.0, environment.maxDiameterAmongCircularDeformableCells.current)
        assertFalse(tension.isValid.current)
        assertEquals(0.0, tension.getTension())
    }
}
