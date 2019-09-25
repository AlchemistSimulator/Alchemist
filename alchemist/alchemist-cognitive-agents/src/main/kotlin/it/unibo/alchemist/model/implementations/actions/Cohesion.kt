package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.utils.div
import it.unibo.alchemist.model.implementations.utils.makePosition
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteeringAction
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the agent towards the other members of his group.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class Cohesion<T, P : Position<P>>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian<T>
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { env.origin() }
), GroupSteeringAction<T, P> {

    override fun group(): List<Pedestrian<T>> = pedestrian.membershipGroup.members

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(
        target,
        centroid() - current,
        maxWalk
    )

    private fun centroid(): P = with(group()) {
        env.makePosition(map { env.getPosition(it) }.reduce { acc, pos -> acc + pos } / size)
    }
}