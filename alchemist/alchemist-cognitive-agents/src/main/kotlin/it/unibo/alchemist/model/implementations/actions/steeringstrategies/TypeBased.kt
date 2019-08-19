package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.*

/**
 * Steering strategy which combines for each type of steering action
 * only the ones with the target nearest to the current pedestrian position.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
class TypeBased<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>
) : Filtered<T, P>(DistanceWeighted<T, P>(env, pedestrian), {
    groupBy { it::class }.entries.map { it.value.minBy { action -> pedestrian.targetDistance(env, action) }!! }
})