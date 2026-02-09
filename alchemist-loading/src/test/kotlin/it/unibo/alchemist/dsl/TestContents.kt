/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.sapere.molecules.LsaMolecule
import org.junit.jupiter.api.Test

class TestContents {

    @Test
    fun testAll() {
        val loader = simulation2D(SAPEREIncarnation()) {
            environment {
                deployments {
                    deploy(point(0.0, 0.0)) {
                        contents {
                            - "test" to listOf(LsaMolecule("1"))
                        }
                    }
                }
            }
        }

        loader.launch(loader.launcher)
    }
}
