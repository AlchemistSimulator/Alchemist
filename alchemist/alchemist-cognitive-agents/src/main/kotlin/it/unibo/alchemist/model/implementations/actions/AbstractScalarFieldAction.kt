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
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import java.lang.IllegalArgumentException

/**
 * A steering action influenced by a scalar field.
 */
abstract class AbstractScalarFieldAction<T, P, A>(
    /**
     * The environment the pedestrian is into.
     */
    protected val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T, P, A>,
    /**
     * A function mapping each position to a scalar value.
     */
    protected val concentrationIn: (P) -> Double,
    /**
     * The position of maximum concentration of the scalar field (can be null).
     */
    protected val center: P? = null
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position2D<P>, P : Vector2D<P>,
          A : GeometricTransformation<P> {


    companion object {
        /**
         * Creates an instance of a scalar field action from a layer. The client can specify the [molecule] contained
         * in the layer.
         */
        inline fun <P, reified C : AbstractScalarFieldAction<Number, P, *>> fromLayer(
            env: Environment<Number, P>,
            molecule: Molecule,
            builder: ((P) -> Number, P?) -> C
        ): C where P : Position2D<P>, P : Vector2D<P> = env.getLayer(molecule)
            .orElseThrow { IllegalArgumentException("no layer containing $molecule") }
            .let { layer ->
                val center = (layer as? BidimensionalGaussianLayer<P>)?.let {
                    env.makePosition(it.centerX, it.centerY)
                }
                builder(layer::getValue, center)
            }
    }

    /**
     * @returns the next relative position reached by the pedestrian. The set of reachable positions is discretized
     * using [Vector2D.surrounding] from the current position (radius is [maxWalk]). If the scalar field has a
     * [center], two more positions are taken into account: one towards the center along the direction connecting the
     * center to the current position, and another away from the center along the same direction. [selectPosition] is
     * used to pick the desired position among the reachable ones.
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
            .selectPosition(currentPosition) - currentPosition
    }

    /**
     * Selects the most suitable position from a sequence, this is used to compute [nextPosition].
     */
    abstract fun Sequence<P>.selectPosition(currentPosition: P): P

    private fun Sequence<P>.enforceObstacles(currentPosition: P): Sequence<P> =
        if (env is EnvironmentWithObstacles<*, T, P>) map { env.next(currentPosition, it) } else this

    private fun canFit(position: P): Boolean =
        env !is PhysicsEnvironment<T, P, *, *> || env.canNodeFitPosition(pedestrian, position)
}
