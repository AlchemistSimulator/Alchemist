package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteeringAction
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget

/**
 * Steering logic where only the "simple" steering action and the group steering action (if present) whose targets
 * are the nearest to the pedestrian's current position are considered.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
class Nearest<T>(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian<T>
) : Filtered<T, Euclidean2DPosition>(DistanceWeighted(env, pedestrian), {
    partition { it is GroupSteeringAction<T, Euclidean2DPosition> }.let { (groupActions, steerActions) ->
        mutableListOf<SteeringAction<T, Euclidean2DPosition>>().apply {
            groupActions.pickNearestOrFirst(env, pedestrian)?.let { add(it) }
            steerActions.pickNearestOrFirst(env, pedestrian)?.let { add(it) }
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
    pedestrian: Pedestrian<T>
): SteeringAction<T, Euclidean2DPosition>? =
    filterIsInstance<SteeringActionWithTarget<T, Euclidean2DPosition>>()
    .minBy { pedestrian.targetDistance(env, it) } ?: firstOrNull()
