package it.unibo.alchemist.model.actions.steeringstrategies

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.cognitiveagents.SteeringAction
import it.unibo.alchemist.model.cognitiveagents.SteeringStrategy
import it.unibo.alchemist.model.geometry.Vector

/**
 * [SteeringStrategy] decorator applying a [filter] to the list of steering actions (see [computeNextPosition]).
 *
 * @param steerStrategy
 *          computeNextPosition is delegated to this strategy.
 * @param filter
 *          the filter to apply on the list of steering actions.
 */
open class Filtered<T, P>(
    private val steerStrategy: SteeringStrategy<T, P>,
    private val filter: List<SteeringAction<T, P>>.() -> List<SteeringAction<T, P>>,
) : SteeringStrategy<T, P> by steerStrategy
    where P : Position<P>, P : Vector<P> {

    /**
     * Delegated to [steerStrategy] after [filter]ing the given [actions].
     */
    override fun computeNextPosition(actions: List<SteeringAction<T, P>>) =
        steerStrategy.computeNextPosition(actions.filter())
}
