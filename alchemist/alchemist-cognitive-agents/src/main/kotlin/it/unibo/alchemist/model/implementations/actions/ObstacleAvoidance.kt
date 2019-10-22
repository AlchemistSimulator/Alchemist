package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.Environment2DWithObstacles
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the agent avoiding potential obstacles in its path.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param proximityRange
 *          the distance at which an obstacle is perceived by the pedestrian.
 */
class ObstacleAvoidance<W : Obstacle2D, T, P : Position2D<P>>(
    private val env: Environment2DWithObstacles<W, T, P>,
    reaction: SteeringBehavior<T, P>,
    pedestrian: Pedestrian<T>,
    private val proximityRange: Double
) : SteeringActionImpl<T, P>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is ObstacleAvoidance<*, *, *> })
    } }
) {

    override fun getDestination(current: P, target: P, maxWalk: Double): P =
        super.getDestination(
            current,
            env.getObstaclesInRange(current.x, current.y, proximityRange)
                .asSequence()
                .map { with(it.nearestIntersection(current.x, current.y, target.x, target.y)) {
                    env.makePosition(this[0], this[1]) to it.bounds2D
                } }
                .minBy { (intersection, _) -> current.getDistanceTo(intersection) }
                ?.let { (intersection, bound) -> intersection to env.makePosition(bound.centerX, bound.centerY) }
                ?.let { (intersection, center) -> current + intersection - center } ?: target,
            maxWalk
        )
}
