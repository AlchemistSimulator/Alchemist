/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.movestrategies.RandomTarget
import it.unibo.alchemist.model.implementations.movestrategies.speed.GloballyConstantSpeed
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Chooses random targets in a direction extracted from [rng] at a distance extracted from [distanceDistribution].
 * Moves the [node] towards the targets at the given constant [speed]. Changes target on collision.
 */
open class RandomWalker<T>(
    node: Node<T>,
    reaction: Reaction<T>,
    environment: Environment<T, Euclidean2DPosition>,
    private val rng: RandomGenerator,
    private val speed: Double,
    private val distanceDistribution: RealDistribution
) : AbstractConfigurableMoveNodeWithAccurateEuclideanDestination<T>(
    environment,
    node,
    RoutingStrategy { p1, p2 -> PolygonalChain<Euclidean2DPosition>(listOf(p1, p2)) },
    RandomTarget<T>(environment, node, rng, distanceDistribution),
    GloballyConstantSpeed(reaction, speed)
) {
    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        RandomWalker(n, r, environment, rng, speed, distanceDistribution)
}
