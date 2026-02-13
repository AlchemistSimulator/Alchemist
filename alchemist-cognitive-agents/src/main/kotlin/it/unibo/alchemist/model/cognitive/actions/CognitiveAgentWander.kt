/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.geometry.Vector2D
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Iterables.randomElement
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * Produces a wandering steering action that simulates a random walk by targeting
 * points on a circle centered at a given offset from the node and with a given radius.
 *
 * The node's heading is set randomly and subsequent target points are chosen on a
 * circumference around the heading with the specified [offset] and [radius]. The
 * resulting target is constrained by the node's maximum walking distance.
 *
 * @param T the concentration type.
 * @param reaction the reaction executing this action.
 * @property environment the physics environment in which the node moves.
 * @param pedestrian the owner of this action.
 * @property randomGenerator the random number generator used to select directions.
 * @property offset the distance from the node position to the center of the circle.
 * @property radius the radius of the circle from which target points are sampled.
 */
open class CognitiveAgentWander<T>(
    private val environment: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    pedestrian: PedestrianProperty<T>,
    protected val randomGenerator: RandomGenerator,
    protected val offset: Double,
    protected val radius: Double,
) : AbstractSteeringActionWithTarget<T, Euclidean2DPosition, Euclidean2DTransformation>(
    environment,
    reaction,
    pedestrian,
    TargetSelectionStrategy {
        randomGenerator.position(
            environment,
        )
    },
) {
    private val heading by lazy {
        environment.setHeading(node, randomGenerator.random2DVersor(environment));
        { environment.getHeading(node) }
    }

    override fun nextPosition(): Euclidean2DPosition = heading()
        .resized(offset)
        .surrounding(radius)
        .randomElement(randomGenerator)
        .coerceAtMost(maxWalk)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        CognitiveAgentWander(environment, reaction, node.pedestrianProperty, randomGenerator, offset, radius)
}

/**
 * Generate a random Euclidean position.
 */
private fun RandomGenerator.position(environment: Environment<*, Euclidean2DPosition>) =
    random2DVersor(environment).let {
        val distance = nextInt()
        Euclidean2DPosition(it.x * distance, it.y * distance)
    }

/**
 * Generate a random Euclidean direction.
 */
private fun <V> RandomGenerator.random2DVersor(
    environment: Environment<*, V>,
): V
    where V : Vector2D<V>, V : Position2D<V> =
    environment.makePosition(nextDouble(-1.0, 1.0), nextDouble(-1.0, 1.0))
