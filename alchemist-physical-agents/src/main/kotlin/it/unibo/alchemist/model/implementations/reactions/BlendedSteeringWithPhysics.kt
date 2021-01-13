/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.physicalstrategies.Sum
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph

/**
 * [BlendedSteering] strategy for physical pedestrians, taking into account physical forces as well. [Sum] strategy
 * is used to combine steering actions and physical forces.
 */
class BlendedSteeringWithPhysics<T>(
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, *, *>,
    pedestrian: PhysicalPedestrian2D<T>,
    timeDistribution: TimeDistribution<T>
) : BlendedSteering<T>(environment, pedestrian, timeDistribution) {

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, pedestrian, super.steerStrategy)
}
