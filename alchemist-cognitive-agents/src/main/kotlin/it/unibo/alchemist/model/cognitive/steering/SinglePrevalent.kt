/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.NavigationAction
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.positions.Euclidean2DPosition

private typealias SteeringActions<T> = List<SteeringAction<T, Euclidean2DPosition>>

/**
 * A [SteeringStrategy] in which one navigation action is prevalent and others are combined
 * with a reduced weight so that the resulting force stays within a tolerance sector around
 * the prevalent force while remaining inside the current room.
 *
 * The steps are:
 * 1. Identify the prevalent navigation action (must be a [NavigationAction]).
 * 2. If the prevalent action leads outside the current room, it is used unchanged.
 * 3. Otherwise, linearly combine the prevalent force (weight 1) and the other forces
 *    (weight w ∈ [0,1]) and reduce w until the combined force is within the tolerance
 *    angle and keeps the node inside the room.
 * 4. Apply exponential smoothing to reduce oscillations.
 *
 * @param T the concentration type.
 * @param N the polygon type used by the environment's navigation graph.
 * @param environment the environment with navigation graph.
 * @param node the node owning the steering strategy.
 * @param prevalent function selecting the prevalent [NavigationAction].
 * @param toleranceAngle tolerance angle in radians.
 * @param alpha smoothing alpha for exponential smoothing.
 * @param maxWalk function computing the maximum allowed walk distance.
 * @param maxWalkRatio minimum magnitude ratio for the resulting force.
 * @param delta decrement step used when searching for a suitable weight w.
 */
class SinglePrevalent<T, N : ConvexPolygon>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, N, *>,
    node: Node<T>,
    private val prevalent: SteeringActions<T>.() -> NavigationAction2D<T, *, *, N, *>,
    /**
     * Tolerance angle in radians.
     */
    private val toleranceAngle: Double = DEFAULT_TOLERANCE_ANGLE,
    /**
     * Alpha value for the [ExponentialSmoothing].
     */
    private val alpha: Double = DEFAULT_ALPHA,
    /**
     * Function computing the maximum distance the node can walk.
     */
    private val maxWalk: () -> Double,
    /**
     * When the node is subject to contrasting forces the resulting one may be small in magnitude.
     * This parameter allows to specify a minimum magnitude for the resulting force computed as
     * [maxWalk] * [maxWalkRatio]
     */
    private val maxWalkRatio: Double = DEFAULT_MAX_WALK_RATIO,
    /**
     * To determine weight w so that the resulting force satisfies the conditions described above, such
     * quantity is initially set to 1.0 and then iteratively decreased by delta until a suitable weight
     * has been found. In other words, the time complexity for computing w is O(1 / delta). This can be
     * reduced to O(1) in the future.
     */
    private val delta: Double = DEFAULT_DELTA,
) : Weighted<T>(environment, node, { 0.0 }) {
    /**
     * Default values for the parameters.
     */
    companion object {
        /**
         * On average, it was observed that this value allows the pedestrian not to get stuck in obstacles.
         */
        const val DEFAULT_TOLERANCE_ANGLE = Math.PI / 4

        /**
         * Empirically found to produce a good smoothing while leaving enough freedom of movement to the pedestrian
         * (e.g. to perform sudden changes of direction).
         */
        const val DEFAULT_ALPHA = 0.5

        /**
         * Empirically found to produce natural movements.
         */
        const val DEFAULT_MAX_WALK_RATIO = 0.3

        /**
         * Good trade-off between efficiency and accuracy.
         */
        const val DEFAULT_DELTA = 0.05
    }

    private val expSmoothing = ExponentialSmoothing<Euclidean2DPosition>(alpha)

    override fun computeNextPosition(actions: SteeringActions<T>): Euclidean2DPosition = with(actions.prevalent()) {
        val prevalentForce = this.nextPosition()
        val leadsOutsideCurrentRoom: Euclidean2DPosition.() -> Boolean = {
            checkNotNull(currentRoom) { "currentRoom should be defined" }
                .let { !it.containsBoundaryIncluded(pedestrianPosition + this) }
        }
        if (prevalentForce == environment.origin ||
            currentRoom == null ||
            prevalentForce.leadsOutsideCurrentRoom()
        ) {
            return prevalentForce
        }
        val otherForces = (actions - this).map { it.nextPosition() }
        var othersWeight = 1.0
        var resulting = combine(prevalentForce, otherForces, othersWeight)
        fun Euclidean2DPosition.isInToleranceSector(): Boolean =
            magnitude > 0.0 && angleBetween(prevalentForce) <= toleranceAngle && !leadsOutsideCurrentRoom()
        while (!resulting.isInToleranceSector() && othersWeight >= 0) {
            othersWeight -= delta
            resulting = combine(prevalentForce, otherForces, othersWeight)
        }
        resulting = resulting.takeIf { othersWeight > 0 } ?: prevalentForce
        (expSmoothing.apply(resulting).takeIf { !it.leadsOutsideCurrentRoom() } ?: resulting)
            .coerceIn(maxWalk() * maxWalkRatio, maxWalk())
    }

    /**
     * Linearly combines the forces assigning [othersWeight] to [others] and unitary weight to [prevalent].
     */
    private fun <V : Vector<V>> combine(prevalent: V, others: List<V>, othersWeight: Double): V =
        (others.map { it * othersWeight } + prevalent).reduce { acc, force -> acc + force }

    /**
     * Exponential smoothing is a trivial way of smoothing signals.
     * Let s(t) be the smoothed signal at time t, given a discrete signal g:
     * s(t) = alpha * g(t) + (1 - alpha) * s(t-1)
     * s(0) = g(0)
     */
    private class ExponentialSmoothing<V : Vector<V>>(private val alpha: Double) {
        init {
            require(alpha in 0.0..1.0) { "alpha should be in [0,1]" }
        }

        private var previous: V? = null

        /**
         * Applies the smoothing to the given force.
         */
        fun apply(current: V): V {
            val new = previous?.let { current.times(alpha) + it.times(1 - alpha) } ?: current
            previous = new
            return new
        }
    }
}
