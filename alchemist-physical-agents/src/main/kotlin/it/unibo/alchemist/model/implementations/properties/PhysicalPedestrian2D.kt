/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2DProperty
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian
import it.unibo.alchemist.model.interfaces.properties.Topological2DProperty
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 * Base implementation of a pedestrian's capability to experience physical interactions in a 2D space.
 */
class PhysicalPedestrian2D<T>(
    randomGenerator: RandomGenerator,
    /**
     * The environment in which the node is moving.
     */
    val environment: Physics2DEnvironment<T>,
    node: Node<T>,
) : PhysicalPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
by PhysicalPedestrian(randomGenerator, environment, node),
    PhysicalPedestrian2DProperty<T> {
    override val comfortArea: Euclidean2DShape get() = environment
        .shapeFactory.circle(node.asCapability<T, Topological2DProperty<T>>().shape.radius + comfortRay)
}
