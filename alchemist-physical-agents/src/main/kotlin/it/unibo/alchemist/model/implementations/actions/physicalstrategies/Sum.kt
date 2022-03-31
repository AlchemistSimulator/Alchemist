/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.physicalstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.PhysicalSteeringStrategy
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty

/**
 * A [PhysicalSteeringStrategy] performing a simple sum of the overall intentional force and the physical ones.
 */
class Sum<T>(
    private val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    override val nonPhysicalStrategy: SteeringStrategy<T, Euclidean2DPosition>
) : PhysicalSteeringStrategy<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory> {

    override fun computeNextPosition(overallIntentionalForce: Euclidean2DPosition): Euclidean2DPosition =
        (node.asProperty<T, PhysicalPedestrian2D<T>>().physicalForces(environment) + overallIntentionalForce)
            .reduce { acc, p -> acc + p }
}
