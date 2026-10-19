/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.cognitive.environments

import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.test.loadYamlSimulation
import kotlin.test.Test
import kotlin.test.assertEquals

class EnvironmentWithDynamicsPositionTest {
    private val environment = loadYamlSimulation<Any, Euclidean2DPosition>("testSinglePedestrian.yml")
        .environment as EnvironmentWithDynamics<Any>
    private val node = environment.nodes.current.single()

    @Test
    fun `position observers read the new position when a node moves`() {
        val readPositions = mutableListOf<Euclidean2DPosition>()
        environment.positionOf(node).onChange(notifyOnRegistration = false) {
            readPositions += environment.currentPositionOf(node)
        }
        environment.moveNodeTo(node, Euclidean2DPosition(5.0, 5.0))
        assertEquals(listOf(Euclidean2DPosition(5.0, 5.0)), readPositions)
    }
}
