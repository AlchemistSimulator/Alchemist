/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.cognitive.steering.SinglePrevalent
import it.unibo.alchemist.model.cognitive.steering.Sum
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.physics.environments.EuclideanPhysics2DEnvironmentWithGraph

/**
 * [NavigationPrioritizedSteering] strategy for physical pedestrians, taking into account physical forces as well.
 * [Sum] strategy is used to combine steering actions and physical forces.
 */
class NavigationPrioritizedSteeringWithPhysics<T, N : ConvexPolygon> @JvmOverloads constructor(
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
    alpha: Double = SinglePrevalent.DEFAULT_ALPHA,
) : NavigationPrioritizedSteering<T, N>(environment, pedestrian, timeDistribution, toleranceAngle, alpha) {

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, node, super.steerStrategy)
}
