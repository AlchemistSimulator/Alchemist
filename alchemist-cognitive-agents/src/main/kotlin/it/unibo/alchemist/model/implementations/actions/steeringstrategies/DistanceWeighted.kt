package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * [Weighted] strategy where the weight of each steering action is the inverse of the pedestrian's distance from the
 * action's target (the closer the target, the more important the action). [defaultWeight] is used for actions without
 * a target.
 *
 * @param environment
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 */
class DistanceWeighted<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: Node<T>,
    /**
     * Default weight for steering actions without a defined target.
     */
    private val defaultWeight: Double = 1.0
) : Weighted<T>(
    environment,
    pedestrian,
    {
        if (this is SteeringActionWithTarget) {
            this.targetDistanceTo(pedestrian, environment).let { if (it > 0.0) 1 / it else it }
        } else {
            defaultWeight
        }
    }
)

/**
 * Computes the distance between this action's target and the given [node].
 */
fun <T, P> SteeringActionWithTarget<T, P>.targetDistanceTo(
    node: Node<T>,
    env: Environment<T, P>
): Double where P : Position<P>, P : Vector<P> = target().distanceTo(env.getPosition(node))
