/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.displacements

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator

class CloseToAlreadyDisplaced<T, P : Position<P>> (
    randomGenerator: RandomGenerator,
    environment: Environment<T, P>,
    nodeCount: Int,
    variance: Double
) : AbstractCloseTo<T, P>(randomGenerator, environment, nodeCount, variance) {
    override val sources = environment.nodes.asSequence()
        .map { environment.getPosition(it) }
        .map { when (it) {
                is GeoPosition -> doubleArrayOf(it.latitude, it.longitude)
                else -> it.cartesianCoordinates
            }
        }
}