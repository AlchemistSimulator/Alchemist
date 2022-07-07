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
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.SinglePrevalent
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * [NavigationPrioritisedSteering] strategy for physical pedestrians, taking into account physical forces as well.
 * [Sum] strategy is used to combine steering actions and physical forces.
 */
class NavigationPrioritisedSteeringWithPhysics<T, N : ConvexPolygon> @JvmOverloads constructor(
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, *>,
    override val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
    /**
     * Tolerance angle in degrees (see [SinglePrevalent]).
     */
    toleranceAngle: Double = Math.toDegrees(SinglePrevalent.DEFAULT_TOLERANCE_ANGLE),
    /**
     * Alpha value for exponential smoothing (see [SinglePrevalent]).
     */
    alpha: Double = SinglePrevalent.DEFAULT_ALPHA
) : NavigationPrioritisedSteering<T, N>(environment, pedestrian, timeDistribution, toleranceAngle, alpha) {

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, node, super.steerStrategy)
}
