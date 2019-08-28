package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.div
import it.unibo.alchemist.model.implementations.utils.makePosition
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
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
class Separation<T>(
    private val env: EuclideanPhysics2DEnvironment<T>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian2D<T>
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { env.origin() }
), GroupSteeringAction<T, Euclidean2DPosition> {

    override fun group(): List<Pedestrian<T>> = pedestrian.fieldOfView(env)
            .influentialNodes()
            .filterIsInstance<Pedestrian<T>>()
            .plusElement(pedestrian)

    override fun getDestination(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double): Euclidean2DPosition =
        super.getDestination(
            target,
            centroid(),
            maxWalk
        )

    private fun centroid(): Euclidean2DPosition = with(group()) {
        env.makePosition(map { env.getPosition(it) - currentPosition }.reduce { acc, pos -> acc + pos } / (-size))
    }
}