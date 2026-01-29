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
import it.unibo.alchemist.model.deployments.Point
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.junit.jupiter.api.Test

class TestContents {

    @Test
    fun testAll() {
        val loader = simulation(SAPEREIncarnation()) {
            deployments {
                deploy(point(0.0, 0.0)) {
                    all {
                        molecule = "test"
                        concentration = listOf(LsaMolecule("1"))
                    }
                }
            }
        }

        loader.launch(loader.launcher)
    }
}
