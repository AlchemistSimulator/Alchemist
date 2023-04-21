/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.SinglePrevalent
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.NavigationAction2D
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * A [SteeringBehavior] using [SinglePrevalent] steering strategy and accepting a collection of actions
 * containing a single [NavigationAction2D], which is used as the prevalent one.
 *
 * @param T concentration type
 * @param N type of nodes of the environment's graph.
 */
open class NavigationPrioritisedSteering<T, N : ConvexPolygon> @JvmOverloads constructor(
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
        private fun <T, M : ConvexPolygon> Actions<T>.singleNavigationAction(): Prevalent<T, M> = this
            .filterIsInstance<NavigationAction2D<T, *, *, M, *>>()
            .let {
                require(it.size == 1) { "There should be exactly one navigation action" }
                it.first()
            }
    }
}

/*
 * Just for readability.
 */
private typealias Actions<T> = List<SteeringAction<T, Euclidean2DPosition>>
private typealias Prevalent<T, M> = NavigationAction2D<T, *, *, M, *>
