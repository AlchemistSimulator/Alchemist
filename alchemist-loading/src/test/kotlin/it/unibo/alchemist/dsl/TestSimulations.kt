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
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.junit.jupiter.api.Test

class TestSimulations {

    @Test
    fun testIncarnation() {
        val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
        val loader = simulation(incarnation) {
        }
        loader.launch(loader.launcher)
    }

    @Test
    fun testLinkingRule() {
        val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
        val loader = simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
        }
        loader.launch(loader.launcher)
    }
}
