package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

/**
 * Weighted steering logic where the weight of each steering action is
 * the inverse of the pedestrian's distance from the action's target.
 *
 * @param environment
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
open class DistanceWeighted<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: Pedestrian<T>,
    /**
     * Default weight for steering actions without a defined target.
     */
    defaultWeight: Double = 1.0
) : Weighted<T>(environment, pedestrian, {
    if (this is SteeringActionWithTarget) {
        pedestrian.targetDistance(environment, this).let { if (it > 0.0) 1 / it else it }
    } else defaultWeight
})

/**
 * Calculate the distance between this pedestrian current position and the target of the specified steering action.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param action
 *          the steering action you want to the target position.
 */
fun <T, P : Position<P>> Pedestrian<T>.targetDistance(
    env: Environment<T, P>,
    action: SteeringActionWithTarget<T, P>
): Double = action.target().distanceTo(env.getPosition(this))
