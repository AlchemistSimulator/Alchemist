package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles
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
class ObstacleAvoidance<W : Obstacle2D<Euclidean2DPosition>, T>(
    private val env: Environment2DWithObstacles<W, T, Euclidean2DPosition>,
    reaction: SteeringBehavior<T>,
    pedestrian: Pedestrian<T>,
    private val proximityRange: Double
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy { with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is ObstacleAvoidance<*, *> })
    } }
) {

    override fun interpolatePositions(
        current: Euclidean2DPosition,
        target: Euclidean2DPosition,
        maxWalk: Double
    ): Euclidean2DPosition =
        super.interpolatePositions(
            current,
            env.getObstaclesInRange(current.x, current.y, proximityRange)
                .asSequence()
                .map { obstacle: W ->
                    obstacle.nearestIntersection(current, target) to obstacle.bounds2D
                }
                .minBy { (intersection, _) -> current.distanceTo(intersection) }
                ?.let { (intersection, bound) -> intersection to env.makePosition(bound.centerX, bound.centerY) }
                ?.let { (intersection, center) -> current + intersection - center } ?: target,
            maxWalk
        )
}
