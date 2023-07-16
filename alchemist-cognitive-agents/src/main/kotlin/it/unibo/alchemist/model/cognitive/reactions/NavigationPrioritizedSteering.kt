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
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.cognitive.steering.SinglePrevalent
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [SteeringBehavior] using [SinglePrevalent] steering strategy and accepting a collection of actions
 * containing a single [NavigationAction2D], which is used as the prevalent one.
 *
 * @param T concentration type
 * @param N type of nodes of the environment's graph.
 */
open class NavigationPrioritizedSteering<T, N : ConvexPolygon> @JvmOverloads constructor(
    environment: Euclidean2DEnvironmentWithGraph<*, T, N, *>,
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
) : SteeringBehavior<T>(
    environment,
    pedestrian,
    timeDistribution,
    SinglePrevalent(
        environment,
        pedestrian.node,
        prevalent = { singleNavigationAction() },
        maxWalk = { pedestrian.speed() / timeDistribution.rate },
        toleranceAngle = Math.toRadians(toleranceAngle),
        alpha = alpha,
    ),
) {

    companion object {
        /**
         * @returns the only navigation action contained in the list or throws an exception.
         */
        private fun <T, M : ConvexPolygon> ActionList<T>.singleNavigationAction(): NaviAction<T, M> = this
            .filterIsInstance<NaviAction<T, M>>()
            .let {
                check(it.size == 1) { "There should be exactly one navigation action" }
                it.first()
            }
    }
}

/*
 * Just for readability.
 */
private typealias ActionList<T> = List<SteeringAction<T, Euclidean2DPosition>>
private typealias NaviAction<T, M> = NavigationAction2D<T, *, *, M, *>
