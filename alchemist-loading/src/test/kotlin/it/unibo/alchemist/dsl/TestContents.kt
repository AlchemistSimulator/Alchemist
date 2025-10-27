/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.boundary.dsl.model.Incarnation.SAPERE
import it.unibo.alchemist.model.deployments.Point
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.junit.jupiter.api.Test

class TestContents {

    @Test
    fun testAll() {
        val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
        val loader = simulation(incarnation) {
            deployments {
                deploy(Point(environment, 0.0, 0.0)) {
                    all {
                        molecule = "test"
                        concentration = 1.0
                    }
                }
            }
        }

        loader.launch(loader.launcher)
    }
}
