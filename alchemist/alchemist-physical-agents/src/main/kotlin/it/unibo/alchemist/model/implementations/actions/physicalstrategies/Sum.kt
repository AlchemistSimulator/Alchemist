/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.physicalstrategies

import it.unibo.alchemist.model.interfaces.PhysicalNode
import it.unibo.alchemist.model.interfaces.PhysicalSteeringStrategy
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [PhysicalSteeringStrategy] performing a simple sum of the overall intentional force and the physical ones.
 */
class Sum<T, P, A, F>(
    private val environment: PhysicsEnvironment<T, P, A, F>,
    override val physicalNode: PhysicalNode<T, P, A, F>,
    override val nonPhysicalStrategy: SteeringStrategy<T, P>
) : PhysicalSteeringStrategy<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    override fun computeNextPosition(overallIntentionalForce: P): P =
        (physicalNode.physicalForces(environment) + overallIntentionalForce).reduce { acc, p -> acc + p }
}
