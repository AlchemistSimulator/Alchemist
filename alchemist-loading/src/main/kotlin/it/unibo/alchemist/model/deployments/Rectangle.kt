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
import org.apache.commons.math3.random.RandomGenerator

/**
 * Randomly deploys nodes inside a rectangle.
 *
 * @param P position type
 * @param environment the [Environment]
 * @param randomGenerator the [RandomGenerator]
 * @param nodes the number of nodes
 * @param sizeX x size
 * @param sizeY y size
 * @property x x start point
 * @property y y start point
 */
open class Rectangle<P : Position<out P>>(
    environment: Environment<*, P>,
    randomGenerator: RandomGenerator,
    nodes: Int,
    protected val x: Double,
    protected val y: Double,
    sizeX: Double,
    sizeY: Double,
) : AbstractRandomDeployment<P>(environment, randomGenerator, nodes) {
    /**
     * The rectangle height.
     */
    protected val height: Double = sizeY

    /**
     * The rectangle width.
     */
    protected val width: Double = sizeX

    override fun indexToPosition(i: Int): P = makePosition(randomDouble(x, x + width), randomDouble(y, y + height))
}
