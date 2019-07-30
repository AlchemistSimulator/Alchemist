package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.Nearest
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
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
 * @param strategy
 *          the logic according to the steering actions are combined.
 */
open class Blended<T, P : Position<P>> @JvmOverloads constructor(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val strategy: SteeringStrategy<T, P> = Nearest(env, pedestrian)
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy { strategy.computeTarget(actions) }
) {
    override fun getDestination(current: P, target: P, maxWalk: Double): P = strategy.computeNextPosition(actions)
}