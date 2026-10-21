/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import org.apache.commons.math3.random.RandomGenerator

/**
 * Randomly deploys nodes inside a circle.
 *
 * @param P [Position] type
 * @param environment the [Environment]
 * @param randomGenerator the [RandomGenerator]
 * @param nodes the number of nodes
 * @param centerX the center x of the circle
 * @param centerY the center y of the circle
 * @param radius the radius of the circle
 */
class Circle<P : Position<out P>>(
    environment: Environment<*, P>,
    randomGenerator: RandomGenerator,
    nodes: Int,
    private val centerX: Double,
    private val centerY: Double,
    private val radius: Double,
) : AbstractRandomDeployment<P>(environment, randomGenerator, nodes) {
    override fun indexToPosition(i: Int): P {
        val angle = randomDouble(0.0, 2 * PI)
        val distance = radius * sqrt(randomDouble())
        return makePosition(centerX + distance * cos(angle), centerY + distance * sin(angle))
    }
}
