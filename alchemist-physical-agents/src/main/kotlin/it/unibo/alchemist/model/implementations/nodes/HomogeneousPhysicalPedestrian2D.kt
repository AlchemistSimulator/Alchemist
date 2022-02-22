/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.capabilities.BasePedestrian2DPhysicalCapability
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

/**
 * A homogeneous pedestrian capable of physical interactions, modeled as a [PhysicalPedestrian2D]. [comfortRay] is
 * statically defined to be equal to its [shape] radius.
 */
open class HomogeneousPhysicalPedestrian2D<T, S : Vector<S>, A : GeometricTransformation<S>> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    environment: Physics2DEnvironment<T>,
    nodeCreationParameter: String? = null,
    group: Group<T>? = null
) :
    PhysicalPedestrian2D<T>,
    HomogeneousPedestrian2D<T>(
        incarnation,
        randomGenerator,
        environment,
        nodeCreationParameter,
        group
    ) {

    init {
        backingNode.addCapability(
            BasePedestrian2DPhysicalCapability(
                randomGenerator,
                backingNode,
                backingNode.asCapability<T, Spatial2DCapability<T, S, A>>().shape
            )
        )
    }

    override val comfortRay: Double = backingNode.asCapability<T, Spatial2DCapability<T, S, A>>().shape.radius
}
