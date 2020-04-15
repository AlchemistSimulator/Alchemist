package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.AbstractEuclideanPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy

/**
 * Combination of multiple steering actions.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param actions
 *          the list of actions to combine to determine the pedestrian movement.
 * @param steerStrategy
 *          the logic according to the steering actions are combined.
 */
class Combine<T, P : AbstractEuclideanPosition<P>>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian
) {
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        Combine(env, r, n as Pedestrian<T>, actions, steerStrategy)

    override fun nextPosition(): P = steerStrategy.computeNextPosition(actions).resizeToMaxWalkIfGreater()
}
