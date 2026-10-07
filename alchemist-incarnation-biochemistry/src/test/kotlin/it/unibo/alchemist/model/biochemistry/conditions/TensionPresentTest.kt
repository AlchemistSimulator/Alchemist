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
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.apache.commons.math3.random.MersenneTwister

class TensionPresentTest {
    private val randomGenerator = MersenneTwister(0)
    private val environment = BioRect2DEnvironmentNoOverlap(BiochemistryIncarnation())
        .apply { linkingRule = ConnectWithinDistance(1.0) }

    private fun deformableCellAt(x: Double, maximumDiameter: Double, rigidity: Double): Node<Double> {
        val cell = GenericNode(environment)
        cell.addProperty(CircularDeformableCell(environment, cell, maximumDiameter, rigidity))
        assertTrue(environment.addNode(cell, Euclidean2DPosition(x, 0.0)))
        return cell
    }

    @Test
    fun `a bigger cell added later widens the range where tension is observed`() {
        val cell = deformableCellAt(0.0, maximumDiameter = 1.0, rigidity = 1.0)
        val reaction =
            BiochemicalNodeReaction(cell, ExponentialTime(1.0, randomGenerator), environment, randomGenerator)
        val tension = TensionPresent(environment, reaction)
        val validity = mutableListOf<Boolean>()
        tension.isValid.subscribe { validity += it }
        assertEquals(listOf(false), validity)
        // Farther than the previous biggest diameter, but within the sum of the maximum radii (0.5 + 2).
        deformableCellAt(2.2, maximumDiameter = 4.0, rigidity = 0.5)
        assertEquals(4.0, environment.maxDiameterAmongCircularDeformableCells.current)
        assertEquals(listOf(false, true), validity)
    }
}
