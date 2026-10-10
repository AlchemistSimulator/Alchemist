/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Walks a fixed [distance] in a uniformly random direction at the given [speed],
 * then repeats the move by selecting a new random direction.
 *
 * The walker automatically changes direction on impact with obstacles when the
 * provided [environment] supports obstacle handling.
 *
 * @param T the concentration type.
 * @param reaction the reaction that contains this action, whose node moves.
 * @param environment the environment containing the node.
 * @param randomGenerator the random generator used to pick directions.
 * @param distance the distance to travel before choosing a new direction.
 * @param speed the walking speed.
 */
class ConstantDistanceRandomWalk<T>(
    reaction: NodeReaction<T>,
    environment: Environment<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    private val distance: Double,
    speed: Double,
) : GenericRandomWalker<T>(
    reaction,
    environment,
    randomGenerator,
    speed,
    DiracDeltaDistribution(distance),
) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>) = ConstantDistanceRandomWalk(
        newReaction,
        environment,
        randomGenerator,
        distance,
        speed,
    )
}
