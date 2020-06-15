package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteeringAction
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

/**
 * [Filtered] strategy considering only the group steering action and the non-group steering action whose targets are
 * nearest to the pedestrian's position. The two actions are combined using [DistanceWeighted] strategy.
 *
 * @param environment
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
class Nearest<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: Pedestrian2D<T>
) : Filtered<T, Euclidean2DPosition>(DistanceWeighted(environment, pedestrian), {
    partition { it is GroupSteeringAction<T, Euclidean2DPosition> }.let { (groupActions, otherActions) ->
        listOfNotNull(
            groupActions.pickNearestOrFirst(environment, pedestrian),
            otherActions.pickNearestOrFirst(environment, pedestrian)
        )
    }
})

/**
 * Picks the [SteeringActionWithTarget] whose target is nearest to the [pedestrian]'s current position, or the first
 * action of the list if none of them has a defined target. If the list is empty, null is returned.
 */
fun <T> List<SteeringAction<T, Euclidean2DPosition>>.pickNearestOrFirst(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian2D<T>
): SteeringAction<T, Euclidean2DPosition>? = this
    .filterIsInstance<SteeringActionWithTarget<T, Euclidean2DPosition>>()
    .minBy { it.targetDistanceTo(pedestrian, env) }
    ?: firstOrNull()
