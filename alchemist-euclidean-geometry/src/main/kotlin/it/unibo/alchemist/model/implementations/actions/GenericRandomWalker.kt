/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.implementations.movestrategies.RandomTarget
import it.unibo.alchemist.model.implementations.movestrategies.speed.GloballyConstantSpeed
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Chooses random targets in a direction extracted from [randomGenerator]
 * at a distance extracted from [distanceDistribution].
 * Moves the node towards the targets at the given constant [speed]. Changes target on collision.
 */
open class GenericRandomWalker<T>(
    node: Node<T>,
    reaction: Reaction<T>,
    environment: Environment<T, Euclidean2DPosition>,
    protected val randomGenerator: RandomGenerator,
    protected val speed: Double,
    protected val distanceDistribution: RealDistribution,
) : AbstractEuclideanConfigurableMoveNode<T, Euclidean2DPosition>(
    environment,
    node,
    RoutingStrategy { p1, p2 ->
        PolygonalChain(
            listOf(
                p1,
                p2,
            ),
        )
    },
    RandomTarget(environment, node, randomGenerator, distanceDistribution),
    GloballyConstantSpeed(reaction, speed),
) {
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        GenericRandomWalker(node, reaction, environment, randomGenerator, speed, distanceDistribution)
}
