/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import org.junit.jupiter.api.Test

class TestSimulations {

    @Test
    fun testIncarnation() {
        val loader = simulation(SAPEREIncarnation()) { }
        loader.launch(loader.launcher)
    }

    @Test
    fun testLinkingRule() {
        val loader = simulation(SAPEREIncarnation()) {
            networkModel = ConnectWithinDistance(5.0)
        }
        loader.launch(loader.launcher)
    }
}
