package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteeringAction
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.SteeringAction

/**
 * Steering logic where only the steering action and the group steering action (if present) whose target is
 * the nearest to the current pedestrian position is taken into consideration.
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
            groupActions.minBy { pedestrian.targetDistance(env, it) }?.let { add(it) }
            steerActions.minBy { pedestrian.targetDistance(env, it) }?.let { add(it) }
        }
    }
})
