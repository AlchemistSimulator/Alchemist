package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.utils.div
import it.unibo.alchemist.model.implementations.utils.makePosition
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the agent away from the pedestrians near to him.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class Separation<T, P : Position<P>>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { env.origin() }
), GroupSteering<T, P> {

    override fun group(): List<Pedestrian<T>> = pedestrian.influencialPeople().plusElement(pedestrian)

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(
        target,
        centroid(),
        maxWalk
    )

    private fun centroid(): P = with(group()) {
        val currentPosition = env.getPosition(pedestrian)
        env.makePosition(map { env.getPosition(it) - currentPosition }.reduce { acc, pos -> acc + pos } / (-size))
    }
}