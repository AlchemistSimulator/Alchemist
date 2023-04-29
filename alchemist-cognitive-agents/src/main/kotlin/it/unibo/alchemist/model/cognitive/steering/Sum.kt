/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.cognitive.PhysicalSteeringStrategy
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DShapeFactory
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment

/**
 * A [PhysicalSteeringStrategy] performing a simple sum of the overall intentional force and the physical ones.
 */
class Sum<T>(
    private val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    override val nonPhysicalStrategy: SteeringStrategy<T, Euclidean2DPosition>,
) : PhysicalSteeringStrategy<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory> {

    private val nodePhysics = node.asProperty<T, PhysicalPedestrian2D<T>>()

    override fun computeNextPosition(overallIntentionalForce: Euclidean2DPosition): Euclidean2DPosition =
        (nodePhysics.physicalForces(environment) + overallIntentionalForce)
            .reduce { acc, p -> acc + p }
}
