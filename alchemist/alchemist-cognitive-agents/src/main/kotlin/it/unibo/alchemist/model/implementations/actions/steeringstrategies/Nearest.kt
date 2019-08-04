package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.*

/**
 * Steering logic where only the steering action and the group steering action (if present) whose target is
 * the nearest to the current pedestrian position is taken into consideration.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
class Nearest<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>
) : Filtered<T, P>(DistanceWeighted<T, P>(env, pedestrian), {
    val targetDistance = { action: SteeringAction<T, P> -> action.target().getDistanceTo(env.getPosition(pedestrian)) }
    partition { it is GroupSteering<T, P> }.let { (groupActions, steerActions) ->
        mutableListOf<SteeringAction<T, P>>().apply {
            groupActions.minBy { targetDistance(it) }?.let { add(it) }
            steerActions.minBy { targetDistance(it) }?.let { add(it) }
        }
    }
})