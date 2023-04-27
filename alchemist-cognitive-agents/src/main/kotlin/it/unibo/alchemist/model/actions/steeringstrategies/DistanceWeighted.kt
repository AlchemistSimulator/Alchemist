package it.unibo.alchemist.model.actions.steeringstrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.SteeringActionWithTarget
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment

/**
 * [Weighted] strategy where the weight of each steering action is the inverse of the node's distance from the
 * action's target (the closer the target, the more important the action). [defaultWeight] is used for actions without
 * a target.
 *
 * @param environment
 *          the environment in which the node moves.
 * @param node
 *          the owner of the steering action this strategy belongs to.
 */
class DistanceWeighted<T>(
    environment: Euclidean2DEnvironment<T>,
    node: Node<T>,
    /**
     * Default weight for steering actions without a defined target.
     */
    private val defaultWeight: Double = 1.0,
) : Weighted<T>(
    environment,
    node,
    {
        if (this is SteeringActionWithTarget) {
            targetDistanceTo(node, environment).let { if (it > 0.0) 1 / it else it }
        } else {
            defaultWeight
        }
    },
)
