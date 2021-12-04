/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.movestrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.nextDouble
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.PI

/**
 * Selects a target based on a random direction extracted from [directionRng],
 * and a random distance extracted from [distanceDistribution].
 * [getCurrentPosition] should return the current position of the object to move.
 * [T] is the type of the concentration of the node.
 */
class RandomTarget<T>(
    private val environment: Environment<T, Euclidean2DPosition>,
    getCurrentPosition: () -> Euclidean2DPosition,
    private val makePosition: (Double, Double) -> Euclidean2DPosition,
    private val directionRng: RandomGenerator,
    private val distanceDistribution: RealDistribution
) : ChangeTargetOnCollision<T, Euclidean2DPosition>(getCurrentPosition) {

    /**
     * Handy constructor for Alchemist where the object to move is a [node] in the [env].
     */
    constructor(
        env: Environment<T, Euclidean2DPosition>,
        node: Node<T>,
        directionRng: RandomGenerator,
        distanceDistribution: RealDistribution
    ) : this(env, { env.getPosition(node) }, { x, y -> env.makePosition(x, y) }, directionRng, distanceDistribution)

    override fun chooseTarget() = with(directionRng.nextDouble(0.0, 2 * PI)) {
        val distance = distanceDistribution.sample()
        val current = getCurrentPosition()
        val delta = makePosition(distance * cos(this), distance * sin(this))
        val desired = current + delta
        when (environment) {
            is Environment2DWithObstacles<*, T> -> environment.next(current, desired)
            else -> desired
        }
    }

    override fun cloneIfNeeded(destination: Node<T>?, reaction: Reaction<T>?): RandomTarget<T> =
        RandomTarget(environment, getCurrentPosition, makePosition, directionRng, distanceDistribution)
}
