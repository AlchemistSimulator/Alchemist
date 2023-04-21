/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.NavigationAction2D
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon

private typealias SteeringActions<T> = List<SteeringAction<T, Euclidean2DPosition>>

/**
 * A [SteeringStrategy] in which one action is prevalent. Only [NavigationAction]s can be prevalent, because
 * they guarantee to navigate the environment consciously (e.g. without getting stuck in obstacles). The
 * purpose of this strategy is to linearly combine the potentially contrasting forces to which the node
 * is subject, while maintaining that warranty. Such forces are combined as follows:
 * let f be the prevalent force,
 * - if f leads the node outside the room (= environment's area) he/she is into, no combination is performed
 * and f is used as it is. This because crossing doors can be a thorny issue and we don't want to introduce
 * disturbing forces.
 * - Otherwise, a linear combination is performed: f is assigned unitary weight, all other forces are assigned
 * weight w equal to the maximum value in [0,1] so that the resulting force:
 * - forms with f an angle smaller than or equal to the specified [toleranceAngle],
 * - doesn't lead the node outside the current room.
 * The idea is to decrease the intensity of non-prevalent forces until the resulting one enters in some tolerance
 * sector defined by both the tolerance angle and the current room's boundary. With a suitable tolerance angle
 * this allows to steer the node towards the target defined by the prevalent force, while using a trajectory
 * which takes into account other urges as well.
 * Finally, an exponential smoothing with the given [alpha] is applied to the resulting force in order to decrease
 * oscillatory movements (this also known as shaking behavior).
 *
 * @param T concentration type
 * @param N type of nodes of the environment's graph.
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

    override fun computeNextPosition(actions: SteeringActions<T>): Euclidean2DPosition =
        with(actions.prevalent()) {
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
            val isInToleranceSector: Euclidean2DPosition.() -> Boolean = {
                magnitude > 0.0 && angleBetween(prevalentForce) <= toleranceAngle && !leadsOutsideCurrentRoom()
            }
            var othersWeight = 1.0
            var resulting = combine(prevalentForce, otherForces, othersWeight)
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
    private class ExponentialSmoothing<V : Vector<V>>(
        private val alpha: Double,
    ) {

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
