/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.CircularCellProperty
import it.unibo.alchemist.model.biochemistry.environments.BioRect2DEnvironmentNoOverlap
import it.unibo.alchemist.model.biochemistry.molecules.Biomolecule
import it.unibo.alchemist.model.biochemistry.nodes.EnvironmentNodeImpl
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.MersenneTwister

class ChemotacticPolarizationTest {
    private val randomGenerator = MersenneTwister(0)
    private val incarnation = BiochemistryIncarnation()
    private val environment = BioRect2DEnvironmentNoOverlap(incarnation)
        .apply { linkingRule = ConnectWithinDistance(2.0) }
    private val cell = incarnation.createNode(randomGenerator, environment, null)
        .also { environment.addNode(it, Euclidean2DPosition(0.0, 0.0)) }
    private val source = EnvironmentNodeImpl(environment)
        .also { environment.addNode(it, Euclidean2DPosition(1.0, 0.0)) }
    private val polarization = incarnation.createReaction(
        randomGenerator,
        environment,
        cell,
        ExponentialTime(1.0, randomGenerator),
        "[] --> [ChemotacticPolarization(A, up)]",
    )

    @Test
    fun `a neighbor gaining the molecule after construction attracts the cell`() {
        source.setConcentration(Biomolecule("A"), 1.0)
        polarization.execute()
        assertEquals(
            Euclidean2DPosition(1.0, 0.0),
            cell.asProperty<Double, CircularCellProperty>().polarizationVersor,
        )
    }

    @Test
    fun `the cell can be removed from the environment`() {
        environment.removeNode(cell)
        assertEquals(listOf(source), environment.nodes.current)
    }
}
