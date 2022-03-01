/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.Pedestrian2DPhysicalCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianPhysicalCapability
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 * Base implementation of a pedestrian's capability to experience physical interactions in a 2D space.
 */
class BasePedestrian2DPhysicalCapability<T>(
    randomGenerator: RandomGenerator,
    /**
     * The environment in which the node is moving.
     */
    val environment: Physics2DEnvironment<T>,
    node: Node<T>,
) : PedestrianPhysicalCapability<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
by BasePedestrianPhysicalCapability(randomGenerator, environment, node),
    Pedestrian2DPhysicalCapability<T> {
    override val comfortArea: Euclidean2DShape get() = environment
        .shapeFactory.circle(node.asCapability<T, Spatial2DCapability<T>>().shape.radius + comfortRay)
}
