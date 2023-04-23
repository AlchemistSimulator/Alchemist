/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Obstacle2D
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.environments.Environment2DWithObstacles
import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.reflect.jvm.jvmName

/**
 * Move the agent avoiding potential obstacles in its path.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param proximityRange
 *          the distance at which an obstacle is perceived by the node.
 */
class CognitiveAgentObstacleAvoidance<W : Obstacle2D<Euclidean2DPosition>, T>(
    private val environment: Environment2DWithObstacles<W, T>,
    override val reaction: SteeringBehavior<T>,
    override val pedestrian: PedestrianProperty<T>,
    private val proximityRange: Double,
) : AbstractSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, pedestrian) {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentObstacleAvoidance<W, T> {
        check(reaction is SteeringBehavior<T>) {
            "steering behavior needed but found ${this.reaction::class.run { simpleName ?: jvmName } }"
        }
        return CognitiveAgentObstacleAvoidance(environment, reaction, node.pedestrianProperty, proximityRange)
    }

    override fun nextPosition(): Euclidean2DPosition = target().let { target ->
        environment.getObstaclesInRange(currentPosition, proximityRange)
            .asSequence()
            .map { obstacle: W ->
                obstacle.nearestIntersection(currentPosition, target) to obstacle.bounds2D
            }
            .minByOrNull { (intersection, _) -> currentPosition.distanceTo(intersection) }
            ?.let { (intersection, bound) -> intersection to environment.makePosition(bound.centerX, bound.centerY) }
            ?.let { (intersection, center) -> (intersection - center).coerceAtMost(maxWalk) }
            /*
             * Otherwise we just don't apply any repulsion force.
             */
            ?: environment.origin
    }

    /**
     * Computes the target of the node, delegating to [reaction].steerStrategy.computeTarget.
     */
    private fun target(): Euclidean2DPosition = with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is CognitiveAgentObstacleAvoidance<*, *> })
    }
}
