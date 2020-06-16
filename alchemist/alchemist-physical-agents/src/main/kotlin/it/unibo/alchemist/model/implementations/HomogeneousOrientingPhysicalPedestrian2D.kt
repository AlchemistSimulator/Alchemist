/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations

import it.unibo.alchemist.model.implementations.nodes.HomogeneousOrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import org.apache.commons.math3.random.RandomGenerator

class HomogeneousOrientingPhysicalPedestrian2D<T, M : ConvexPolygon, E>(
    override val environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, E>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double
) : HomogeneousOrientingPedestrian2D<T, M, E>(environment, randomGenerator, knowledgeDegree) {
}