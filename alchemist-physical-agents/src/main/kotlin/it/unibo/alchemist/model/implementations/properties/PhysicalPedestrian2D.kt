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
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 * Base implementation of a pedestrian's capability to experience physical interactions in a 2D space.
 */
class PhysicalPedestrian2D<T>(
    private val randomGenerator: RandomGenerator,
    /**
     * The environment in which the node is moving.
     */
    override val environment: Physics2DEnvironment<T>,
    node: Node<T>,
) : PhysicalPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>(
    randomGenerator,
    environment,
    node,
),
    PhysicalPedestrian2D<T> {

    override val comfortArea: Euclidean2DShape get() = environment
        .shapeFactory
        .circle(node.asProperty<T, AreaProperty<T>>().shape.radius + comfortRay)
        .transformed { origin(environment.getPosition(node)) }

    override fun physicalForces(
        environment: PhysicsEnvironment<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>,
    ) = environment.getNodesWithin(comfortArea)
        .minusElement(node)
        .filter { it.asPropertyOrNull<T, AreaProperty<T>>() != null }
        .map { repulsionForce(it) }
        /*
         * Discard infinitesimal forces.
         */
        .filter { it.magnitude > Double.MIN_VALUE }

    override fun cloneOnNewNode(node: Node<T>) = PhysicalPedestrian2D(randomGenerator, environment, node)
}
