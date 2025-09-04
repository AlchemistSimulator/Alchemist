/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.dsl.SimulationContext
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.deployments.Grid
import it.unibo.alchemist.model.deployments.Point
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

class DeploymentsContext(private val ctx: SimulationContext) {
    var deployments: MutableList<Deployment<*>> = mutableListOf()
    var generator: RandomGenerator = MersenneTwister(0)

    fun deploy(deployment: Deployment<*>) {
        deployments.add(deployment)
    }

    // Convenience methods to avoid casting issues
    fun point(x: Double, y: Double) {
        val point = Point(ctx.environment, x, y)
        deploy(point)
    }

    fun grid(
        xStart: Double,
        yStart: Double,
        xEnd: Double,
        yEnd: Double,
        xStep: Double,
        yStep: Double,
        xRand: Double = 0.0,
        yRand: Double = 0.0,
    ) {
        val grid = Grid(
            ctx.environment, generator, xStart,
            yStart, xEnd, yEnd, xStep, yStep, xRand, yRand,
        )
        deploy(grid)
    }
}
