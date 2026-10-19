/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.movestrategies

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Selects a target for [node] based on a random direction extracted from [directionRng],
 * and a random distance extracted from [distanceDistribution], stopping where [environment] stops [node].
 * [T] is the type of the concentration of the node.
 */
class RandomTarget<T>(
    private val environment: Environment<T, Euclidean2DPosition>,
    private val node: Node<T>,
    private val directionRng: RandomGenerator,
    private val distanceDistribution: RealDistribution,
) : ChangeTargetOnCollision<T, Euclidean2DPosition>({ environment.currentPositionOf(node) }) {

    override fun chooseTarget() = with(directionRng.nextDouble(0.0, 2 * PI)) {
        val distance = distanceDistribution.sample()
        val delta = environment.makePosition(distance * cos(this), distance * sin(this))
        environment.farthestReachablePosition(node, getCurrentPosition() + delta)
    }

    override fun cloneIfNeeded(destination: Node<T>, reaction: NodeReaction<T>): RandomTarget<T> =
        RandomTarget(environment, destination, directionRng, distanceDistribution)
}
