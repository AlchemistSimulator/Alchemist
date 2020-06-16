package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteeringAction
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

/**
 * Steering logic where only the "simple" steering action and the group steering action (if present) whose targets
 * are the nearest to the pedestrian's current position are considered.
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
    partition { it is GroupSteeringAction<T, Euclidean2DPosition> }.let { (groupActions, steerActions) ->
        mutableListOf<SteeringAction<T, Euclidean2DPosition>>().apply {
            groupActions.pickNearestOrFirst(environment, pedestrian)?.let { add(it) }
            steerActions.pickNearestOrFirst(environment, pedestrian)?.let { add(it) }
        }
    }
})

/**
 * Picks the [SteeringActionWithTarget] whose target is nearest to the pedestrian's current position,
 * or the first simple [SteeringAction] if none of them has a target. If the list is empty, null is
 * returned.
 */
fun <T> List<SteeringAction<T, Euclidean2DPosition>>.pickNearestOrFirst(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian2D<T>
): SteeringAction<T, Euclidean2DPosition>? =
    filterIsInstance<SteeringActionWithTarget<T, Euclidean2DPosition>>()
    .minBy { pedestrian.targetDistance(env, it) } ?: firstOrNull()
