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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.distribution.DiracDeltaDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Moves for [distance] toward a uniformly random chosen direction at a constant [speed],
 * then changes direction and walks another [distance], and so on.
 *
 * Automatically changes direction on impact with obstacles
 * if the [environment] supports them.
 *
 * @param <T> concentration type
 * @param environment environment containing the node
 * @param node the node to move
 * @param reaction the reaction containing this action
 * @param randomGenerator random number generator to use for the decisions
 * @param distance the distance to travel before picking another one
 * @param speed the speed
 */
class ConstantDistanceRandomWalk<T>(
    node: Node<T>,
    reaction: Reaction<T>,
    environment: Environment<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    private val distance: Double,
    speed: Double,
) : GenericRandomWalker<T>(
    node,
    reaction,
    environment,
    randomGenerator,
    speed,
    DiracDeltaDistribution(distance),
) {
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        ConstantDistanceRandomWalk(
            node,
            reaction,
            environment,
            randomGenerator,
            distance,
            speed,
        )
}
