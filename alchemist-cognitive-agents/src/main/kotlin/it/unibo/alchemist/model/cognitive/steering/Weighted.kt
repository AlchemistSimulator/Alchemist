/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.GroupSteeringAction
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * SteeringStrategy that computes the agent's next position as a weighted sum of steering actions.
 *
 * Actions are partitioned into group steering actions (instances of [GroupSteeringAction]) and
 * non-group steering actions. For each partition the strategy computes a weighted average of
 * the actions' next positions using the provided [weight] lambda; the two resulting vectors are
 * then summed (with unitary weight) to obtain the final displacement.
 *
 * The [weight] parameter is an extension lambda on [SteeringAction] invoked as `action.weight()`.
 * It must return a numeric weight (a non-negative [Double]) used to compute the weighted average.
 * If the total weight of a partition is zero, the implementation falls back to the first action's
 * next position; if there are no actions at all, the environment origin is returned.
 *
 * The strategy also computes a target position by selecting the closest target among
 * [SteeringActionWithTarget] actions; if none are present, the current node position is returned.
 *
 * @param T the concentration type of the simulation.
 * @param environment the environment in which the node moves.
 * @param node the owner of the steering actions combined by this strategy.
 * @param weight an extension lambda that assigns a numeric weight to each steering action.
 */
open class Weighted<T>(
    private val environment: Euclidean2DEnvironment<T>,
    private val node: Node<T>,
    private val weight: SteeringAction<T, Euclidean2DPosition>.() -> Double,
) : SteeringStrategy<T, Euclidean2DPosition> {
    /**
     * [actions] are partitioned in group steering actions and non-group steering actions. The overall next position
     * for each of these two sets of actions is computed via weighted sum. The resulting vectors are then summed
     * together (with unitary weight).
     */
    override fun computeNextPosition(actions: List<SteeringAction<T, Euclidean2DPosition>>): Euclidean2DPosition =
        actions.partition { it is GroupSteeringAction<T, Euclidean2DPosition> }.let { (groupActions, steerActions) ->
            groupActions.calculatePosition() + steerActions.calculatePosition()
        }

    /**
     * If there's no [SteeringActionWithTarget] among the provided [actions], a zero vector is returned. Otherwise,
     * the closest target is picked.
     */
    override fun computeTarget(actions: List<SteeringAction<T, Euclidean2DPosition>>): Euclidean2DPosition =
        environment.getCurrentPosition(node).let { currPos ->
            actions
                .filterIsInstance<SteeringActionWithTarget<T, out Euclidean2DPosition>>()
                .map { it.target() }
                .minByOrNull { it.distanceTo(currPos) }
                ?: currPos
        }

    private fun List<SteeringAction<T, Euclidean2DPosition>>.calculatePosition(): Euclidean2DPosition = when {
        size > 1 ->
            map { it.nextPosition() to it.weight() }.run {
                val totalWeight = sumOf { it.second }
                map { it.first * (it.second / totalWeight) }.reduce { acc, pos -> acc + pos }
            }
        else -> firstOrNull()?.nextPosition() ?: environment.origin
    }
}
