/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import java.lang.IllegalArgumentException

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
     * A function mapping each position to a scalar value.
     */
    private val concentrationIn: (P) -> Double,
    /**
     * The position of either maximum or minimum concentration of the scalar field, can be null if such a position
     * doesn't exists or isn't known. Its use is explained in [nextPosition].
     */
    private val center: P? = null
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {


    companion object {
        /**
         * Creates an instance of [FollowScalarField] from a [Layer]. The client can specify the [molecule] contained
         * in the layer. If [invertValues] is true the layer will be inverted, which means the returned action will
         * move the pedestrian where the scalar field is lower.
         */
        inline fun <P, reified C : FollowScalarField<Double, P, *>> fromLayer(
            env: Environment<Double, P>,
            molecule: Molecule,
            invertValues: Boolean,
            builder: ((P) -> Double, P?) -> C
        ): C where P : Position2D<P>, P : Vector2D<P> {
            val layer = env.getLayer(molecule).orElseThrow { IllegalArgumentException("no layer containing $molecule") }
            val center = (layer as? BidimensionalGaussianLayer<P>)?.let { env.makePosition(it.centerX, it.centerY) }
            val concentrationIn: (P) -> Double = if (invertValues) {
                { position -> -layer.getValue(position) }
            } else {
                layer::getValue
            }
            return builder(concentrationIn, center)
        }
    }

    /**
     * @returns the next relative position reached by the pedestrian. The set of reachable positions is discretized
     * using [Vector2D.surrounding] from the current position (radius is [maxWalk]). If the scalar field has a
     * [center], two more positions are taken into account: one towards the center along the direction connecting the
     * latter to the current position, and another away from the center along the same direction. The position with
     * maximum concentration is then selected: if its concentration is higher than the concentration in the current
     * position, the pedestrian moves there. Otherwise, it doesn't move at all.
     */
    override fun nextPosition(): P = currentPosition.let { currentPosition ->
        val centerProjectedPositions = center?.let {
            val direction = (center - currentPosition).coerceAtMost(maxWalk)
            listOf(currentPosition + direction, currentPosition - direction)
        } ?: emptyList()
        (currentPosition.surrounding(maxWalk) + centerProjectedPositions)
            .asSequence()
            .enforceObstacles(currentPosition)
            .filter { canFit(it) }
            /*
             * Next relative position.
             */
            .maxOr(currentPosition) - currentPosition
    }

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, FollowScalarField<T, P, A>>(n) {
            FollowScalarField(env, reaction,  it, concentrationIn, center)
        }

    private fun Sequence<P>.enforceObstacles(currentPosition: P): Sequence<P> =
        if (env is EnvironmentWithObstacles<*, T, P>) map { env.next(currentPosition, it) } else this

    private fun canFit(position: P): Boolean =
        env !is PhysicsEnvironment<T, P, *, *> || env.canNodeFitPosition(pedestrian, position)

    private fun Sequence<P>.maxOr(currentPosition: P): P = this
        .maxBy { concentrationIn(it) }
        ?.takeIf { concentrationIn(it) > concentrationIn(currentPosition) }
        ?: currentPosition
}
