package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy

/**
 * Steering logic in which a filter is applied to the steering actions
 * in order to execute another steering logic with only some of these.
 *
 * @param steerStrategy
 *          the environment in which the pedestrian moves.
 * @param filter
 *          the lambda to apply on the list of steering actions.
 */
open class Filtered<T, P : Position<P>>(
    private val steerStrategy: SteeringStrategy<T, P>,
    private val filter: List<SteeringAction<T, P>>.() -> List<SteeringAction<T, P>>
) : SteeringStrategy<T, P> by steerStrategy {

    override fun computeNextPosition(actions: List<SteeringAction<T, P>>) = steerStrategy.computeNextPosition(actions.filter())
}
