package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.utils.div
import it.unibo.alchemist.model.implementations.actions.utils.makePosition
import it.unibo.alchemist.model.implementations.actions.utils.origin
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GroupSteering
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the agent towards the other members of his group.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 */
class Cohesion<T, P : Position<P>>(
    private val env: Environment<T, P>,
    private val pedestrian: Pedestrian<T>
) : SteeringActionImpl<T, P>(
    env,
    pedestrian,
    TargetSelectionStrategy { env.origin() }
), GroupSteering<T, P> {

    override fun getDestination(current: P, target: P, maxWalk: Double): P = super.getDestination(
        target,
        centroid() - current,
        maxWalk
    )

    private fun centroid(): P = with(pedestrian.membershipGroup.members) {
        env.makePosition(map { env.getPosition(it) }.reduce { acc, pos -> acc + pos } / size.toDouble())
    }
}