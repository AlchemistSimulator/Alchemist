/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.SinglePrevalent
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon

/**
 * A [SteeringBehavior] using [SinglePrevalent] steering strategy and accepting a collection of actions
 * containing a single [EuclideanNavigationAction], which is used as the prevalent one.
 *
 * @param T concentration type
 * @param M type of nodes of the environment's graph.
 */
class NavigationPrioritisedSteering<T, M : ConvexPolygon> @JvmOverloads constructor(
    env: Euclidean2DEnvironmentWithGraph<*, T, M, *>,
    pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>,
    /**
     * Tolerance angle in degrees (see [SinglePrevalent]).
     */
    toleranceAngle: Double = Math.toDegrees(SinglePrevalent.DEFAULT_TOLERANCE_ANGLE),
    /**
     * Alpha value for exponential smoothing (see [SinglePrevalent]).
     */
    alpha: Double = SinglePrevalent.DEFAULT_ALPHA
) : SteeringBehavior<T>(
    env,
    pedestrian,
    timeDistribution,
    SinglePrevalent(
        env,
        pedestrian,
        { this.getSingleNavigationAction() },
        maxWalk = { pedestrian.speed() / timeDistribution.rate },
        toleranceAngle = Math.toRadians(toleranceAngle),
        alpha = alpha
    )
) {

    companion object {
        private fun <T, M : ConvexPolygon> List<SteeringAction<T, Euclidean2DPosition>>.getSingleNavigationAction():
            EuclideanNavigationAction<T, *, *, M, *> = filterIsInstance<EuclideanNavigationAction<T, *, *, M, *>>()
            .let {
                require(it.size == 1) { "There should be only one navigation action" }
                it.first()
            }
    }
}
