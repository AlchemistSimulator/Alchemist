/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.GroupSteeringAction
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * [Filtered] strategy considering only the group steering action and the non-group steering action whose targets are
 * nearest to the node's position. The two actions are combined using [DistanceWeighted] strategy.
 *
 * @param environment
 *          the environment in which the node moves.
 * @param node
 *          the owner of the steering action this strategy belongs to.
 */
class Nearest<T>(
    environment: Euclidean2DEnvironment<T>,
    node: Node<T>,
) : Filtered<T, Euclidean2DPosition>(
    DistanceWeighted(environment, node),
    {
        partition { it is GroupSteeringAction<T, Euclidean2DPosition> }.let { (groupActions, otherActions) ->
            listOfNotNull(
                groupActions.pickNearestOrFirst(environment, node),
                otherActions.pickNearestOrFirst(environment, node),
            )
        }
    },
) {
    companion object {
        /**
         * Picks the [SteeringActionWithTarget] whose target is nearest to the [node]'s current position, or the first
         * action of the list if none of them has a defined target. If the list is empty, null is returned.
         */
        private fun <T> List<SteeringAction<T, Euclidean2DPosition>>.pickNearestOrFirst(
            environment: Environment<T, Euclidean2DPosition>,
            node: Node<T>,
        ): SteeringAction<T, Euclidean2DPosition>? =
            filterIsInstance<SteeringActionWithTarget<T, Euclidean2DPosition>>()
                .minByOrNull { it.targetDistanceTo(node, environment) }
                ?: firstOrNull()
    }
}
