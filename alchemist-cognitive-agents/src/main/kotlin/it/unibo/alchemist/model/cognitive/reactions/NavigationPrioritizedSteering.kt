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
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.SimpleNetworkArrivals
import it.unibo.alchemist.model.timedistributions.WeibullTime

/**
 * A [SteeringBehavior] that prioritizes a single navigation action using the [SinglePrevalent] strategy.
 * The provided actions must contain a single [NavigationAction2D], used as the prevalent action.
 *
 * @param T the concentration type.
 * @param N the polygon type used by the environment's navigation graph.
 * @param environment the environment containing the navigation graph.
 * @property pedestrian the owner pedestrian's property.
 * @param timeDistribution the time distribution that schedules reaction execution.
 * @param toleranceAngle tolerance angle in degrees for the SinglePrevalent strategy.
 * @param alpha smoothing alpha for exponential smoothing used by SinglePrevalent.
 */
open class NavigationPrioritizedSteering<T, N : ConvexPolygon>
@JvmOverloads
constructor(
    environment: Euclidean2DEnvironmentWithGraph<*, T, N, *>,
    pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
    /** Tolerance angle in degrees (see [SinglePrevalent]). */
    toleranceAngle: Double = Math.toDegrees(SinglePrevalent.DEFAULT_TOLERANCE_ANGLE),
    /** Alpha value for exponential smoothing (see [SinglePrevalent]). */
    alpha: Double = SinglePrevalent.DEFAULT_ALPHA,
) : SteeringBehavior<T>(
    environment,
    pedestrian,
    timeDistribution,
    SinglePrevalent(
        environment,
        pedestrian.node,
        prevalent = { singleNavigationAction() },
        maxWalk = {
            pedestrian.speed() /
                (
                    timeDistribution.steeringRate.takeUnless(Double::isNaN) ?: error(
                        "Navigation steering requires a time generator with a defined execution rate",
                    )
                    )
        },
        toleranceAngle = Math.toRadians(toleranceAngle),
        alpha = alpha,
    ),
) {
    private companion object {
        /**
         * Returns the only navigation action contained in the list or throws an exception.
         */
        private fun <T, M : ConvexPolygon> ActionList<T>.singleNavigationAction(): NaviAction<T, M> = this
            .filterIsInstance<NaviAction<T, M>>()
            .let {
                check(it.size == 1) { "There should be exactly one navigation action" }
                it.first()
            }
    }
}

private val TimeDistribution<*>.steeringRate: Double
    get() = when (this) {
        is DiracComb<*> -> frequency
        is ExponentialTime<*> -> lambda
        is AnyRealDistribution<*> -> mean
        is WeibullTime<*> -> mean
        is SimpleNetworkArrivals<*> -> expectedRate
        else -> Double.NaN
    }

/*
 * Just for readability.
 */
private typealias ActionList<T> = List<SteeringAction<T, Euclidean2DPosition>>
private typealias NaviAction<T, M> = NavigationAction2D<T, *, *, M, *>
