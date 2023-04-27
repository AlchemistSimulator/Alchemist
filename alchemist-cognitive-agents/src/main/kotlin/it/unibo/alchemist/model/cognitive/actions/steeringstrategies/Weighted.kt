/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions.steeringstrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.GroupSteeringAction
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [SteeringStrategy] performing a weighted sum of steering actions (see [computeNextPosition]).
 *
 * @param environment
 *          the environment in which the node moves.
 * @param node
 *          the owner of the steering actions combined by this strategy.
 * @param weight
 *          lambda used to assign a weight to each steering action: the higher the weight, the greater the
 *          importance of the action.
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
        environment.getPosition(node).let { currPos ->
            actions.filterIsInstance<SteeringActionWithTarget<T, out Euclidean2DPosition>>()
                .map { it.target() }
                .minByOrNull { it.distanceTo(currPos) }
                ?: currPos
        }

    private fun List<SteeringAction<T, Euclidean2DPosition>>.calculatePosition(): Euclidean2DPosition = when {
        size > 1 ->
            map { it.nextPosition() to it.weight() }.run {
                val totalWeight = map { it.second }.sum()
                map { it.first * (it.second / totalWeight) }.reduce { acc, pos -> acc + pos }
            }
        else -> firstOrNull()?.nextPosition() ?: environment.origin
    }
}
