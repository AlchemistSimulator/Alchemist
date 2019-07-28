package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Steering logic where only the steering action whose target is
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
    sortedBy { it.target().getDistanceTo(env.getPosition(pedestrian)) }.take(1)
})