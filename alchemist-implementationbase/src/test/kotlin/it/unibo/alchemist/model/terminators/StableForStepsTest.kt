/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.terminators

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.test.Test
import kotlin.test.assertEquals

class StableForStepsTest {
    @Test
    fun `nodes holding null concentrations are compared like any other content`() {
        // Protelis concentrations may be null.
        val incarnation: Incarnation<Any?, Euclidean2DPosition> =
            SupportedIncarnations.get<Any?, Euclidean2DPosition>("protelis").orElseThrow()
        val environment = Continuous2DEnvironment(incarnation)
        environment.simulation = mockk<Simulation<Any?, Euclidean2DPosition>> { every { step } returns 0 }
        val node = GenericNode(environment)
        node.setConcentration(SimpleMolecule("empty"), null)
        environment.addNode(node, environment.makePosition(0, 0))
        val terminator = StableForSteps<Any?, Euclidean2DPosition>(checkInterval = 1, equalIntervals = 2)
        assertEquals(listOf(false, false, true), List(3) { terminator(environment) })
    }
}
