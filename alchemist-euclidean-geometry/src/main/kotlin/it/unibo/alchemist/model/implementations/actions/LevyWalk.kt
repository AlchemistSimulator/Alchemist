/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.distribution.ParetoDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Selects a target based on a random direction extracted from [rng],
 * and a random distance extracted from a [ParetoDistribution] of parameters [scale] and [shape].
 * Moves toward the targets at a constant [speed] and changes targets on collision.
 */
class LevyWalk<T> @JvmOverloads constructor(
    node: Node<T>,
    reaction: Reaction<T>,
    environment: Environment<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    speed: Double,
    private val scale: Double = 1.0, // default parameters for the Pareto distribution
    private val shape: Double = 1.0,
) : GenericRandomWalker<T>(
    node,
    reaction,
    environment,
    randomGenerator,
    speed,
    ParetoDistribution(randomGenerator, scale, shape),
) {
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        LevyWalk(node, reaction, environment, randomGenerator, speed, scale, shape)
}
