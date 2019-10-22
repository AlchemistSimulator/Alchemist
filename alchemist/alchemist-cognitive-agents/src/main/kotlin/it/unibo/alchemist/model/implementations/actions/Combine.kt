package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

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
class Combine<T, P : Position<P>>(
    env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { steerStrategy.computeTarget(actions) }
) {
    override fun getDestination(current: P, target: P, maxWalk: Double): P = steerStrategy.computeNextPosition(actions)
}
