package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.actions.utils.makePosition
import it.unibo.alchemist.model.implementations.actions.utils.origin
import it.unibo.alchemist.model.implementations.actions.utils.times
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy

/**
 * Steering logic where each steering action is associated to a weight
 * and the final computed position is their weighted sum.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 * @param weight
 *          lambda to associate each steering action a numerical value representing
 *          its relevance in the position computation.
 */
open class Weighted<T, P : Position<P>>(
    private val env: Environment<T, P>,
    private val pedestrian: Pedestrian<T>,
    private val weight: SteeringAction<T, P>.() -> Double
) : SteeringStrategy<T, P> {

    override fun computePosition(actions: List<SteeringAction<T, P>>) = with(env.getPosition(pedestrian)) {
        this + if (actions.size > 1) {
            actions.map { it.nextPosition() to it.weight() }.run {
                val totalWeight = map { it.second }.sum()
                map { env.makePosition(it.first * (it.second / totalWeight)) }.reduce { acc, pos -> acc + pos }
            }
        } else actions.firstOrNull()?.nextPosition() ?: env.origin()
    }
}