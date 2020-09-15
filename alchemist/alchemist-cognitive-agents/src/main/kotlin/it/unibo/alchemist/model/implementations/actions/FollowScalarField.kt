/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

/**
 * Moves the pedestrian where the given scalar field is higher.
 */
class FollowScalarField<T, P, A>(
    /**
     * The environment the pedestrian is into.
     */
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    /**
     * The position of either maximum or minimum value of the scalar field, can be null if such a position doesn't
     * exist or isn't known. Its use is explained in [nextPosition].
     */
    private val center: P? = null,
    /**
     * A function mapping each position to a scalar value (= the scalar field).
     */
    private val valueIn: (P) -> Double
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {

    /**
     * @returns the next relative position reached by the pedestrian. The set of reachable positions is discretized
     * using [Vector2D.surrounding] from the current position (radius is [maxWalk]). If the scalar field has a
     * [center], two more positions are taken into account: one towards the center along the direction connecting the
     * latter to the current position, and another away from the center along the same direction. The position with
     * maximum value is then selected: if its value is higher than the current one, the pedestrian moves there.
     * Otherwise, it doesn't move at all.
     */
    override fun nextPosition(): P = currentPosition.let { currentPosition ->
        val centerProjectedPositions = center?.let {
            val direction = (center - currentPosition).coerceAtMost(maxWalk)
            listOf(currentPosition + direction, currentPosition - direction)
        }
        (currentPosition.surrounding(maxWalk) + (centerProjectedPositions ?: emptyList()))
            .asSequence()
            .enforceObstacles(currentPosition)
            .enforceOthers()
            /*
             * Next relative position.
             */
            .maxOr(currentPosition) - currentPosition
    }

    override fun cloneAction(n: Pedestrian<T, P, A>, r: Reaction<T>) = FollowScalarField(env, r, n, center, valueIn)

    private fun Sequence<P>.enforceObstacles(currentPosition: P): Sequence<P> =
        if (env is EnvironmentWithObstacles<*, T, P>) map { env.next(currentPosition, it) } else this

    private fun Sequence<P>.enforceOthers(): Sequence<P> =
        if (env is PhysicsEnvironment<T, P, *, *>) map { env.farthestPositionReachable(pedestrian, it) } else this

    private fun Sequence<P>.maxOr(position: P): P =
        maxBy { valueIn(it) }
        ?.takeIf { valueIn(it) > valueIn(position) }
        ?: position
}
