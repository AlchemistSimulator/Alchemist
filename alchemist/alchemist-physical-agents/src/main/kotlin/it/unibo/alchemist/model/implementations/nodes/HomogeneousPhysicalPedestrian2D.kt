/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * A homogeneous pedestrian capable of physical interactions, modeled as a [PhysicalPedestrian2D]. [comfortRay] is
 * statically defined to be equal to its [shape] radius.
 */
open class HomogeneousPhysicalPedestrian2D<T> @JvmOverloads constructor(
    environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    group: PedestrianGroup2D<T>? = null
) : HomogeneousPedestrian2D<T>(environment, randomGenerator, group), PhysicalPedestrian2D<T> {

    override val comfortRay: Double = super<HomogeneousPedestrian2D>.shape.radius
}
