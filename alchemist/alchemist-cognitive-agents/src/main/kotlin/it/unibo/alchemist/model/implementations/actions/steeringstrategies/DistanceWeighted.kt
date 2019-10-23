package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction

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
    pedestrian.targetDistance(env, this).let { if (it > 0.0) 1 / it else it }
})

/**
 * Calculate the distance between this pedestrian current position and the specified steering action target.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param action
 *          the steering action you want to the target position.
 */
fun <T, P : Position<P>> Pedestrian<T>.targetDistance(
    env: Environment<T, P>,
    action: SteeringAction<T, P>
): Double = action.target().getDistanceTo(env.getPosition(this))
