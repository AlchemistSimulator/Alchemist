/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import org.junit.jupiter.api.Test

class TestDeployments {

    @Test
    fun testDeployments() {
        val loader = simulation2D(SAPEREIncarnation()) {
            environment {
                deployments {
                    val p = point(0.0, 0.0)
                    deploy(p)
                }
            }
        }

        loader.launch(loader.launcher)
    }

    @Test
    fun testMultipleDeployments() {
        val loader = simulation2D(SAPEREIncarnation()) {
            environment {
                deployments {
                val point = point(0.0, 0.0)
                deploy(point)
                deploy(point(1.0, 1.0))
            }}
        }

        loader.launch(loader.launcher)
    }

    @Test
    fun testGridDeployment() {
        val loader = simulation2D(SAPEREIncarnation()) {
            environment {
                deployments {
                val grid = grid(
                    1.0,
                    1.0,
                    5.0,
                    5.0,
                    1.0,
                    1.0,
                )
                deploy(grid)
            }
        }}
        loader.launch(loader.launcher)
    }
}
