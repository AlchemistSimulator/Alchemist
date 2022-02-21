/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.implementations.capabilities.BasePerceptionOfOthers2D
import it.unibo.alchemist.model.implementations.capabilities.BaseSpatial2DCapability
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability.Companion.defaultShapeRadius
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 * A pedestrian with heterogeneous characteristics. Requires a bidimensional [environment] with support for physics
 * ([Physics2DEnvironment]).
 */
class HeterogeneousPedestrian2D<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    override val environment: Physics2DEnvironment<T>,
    age: Any,
    gender: String,
    group: Group<T>? = null,
    nodeCreationParameter: String? = null,
) : AbstractHeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>(
    randomGenerator,
    incarnation.createNode(randomGenerator, environment, nodeCreationParameter),
    Age.fromAny(age),
    Gender.fromString(gender),
    group
),
    Pedestrian2D<T> {
    init {
        backingNode.addCapability(BasePerceptionOfOthers2D(environment, backingNode))
        backingNode.addCapability(
            BaseSpatial2DCapability(
                backingNode,
                environment.shapeFactory.circle(defaultShapeRadius)
            )
        )
    }
}
