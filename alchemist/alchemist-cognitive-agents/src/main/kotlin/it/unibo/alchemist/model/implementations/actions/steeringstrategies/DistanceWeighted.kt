package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Weighted steering logic where the weight of each steering action is
 * the inverse of its distance from the target.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
open class DistanceWeighted<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>
) : Weighted<T, P>(env, pedestrian, {
    target().getDistanceTo(env.getPosition(pedestrian))
            .let { if (it > 0) 1 / it else 0.0 }
})