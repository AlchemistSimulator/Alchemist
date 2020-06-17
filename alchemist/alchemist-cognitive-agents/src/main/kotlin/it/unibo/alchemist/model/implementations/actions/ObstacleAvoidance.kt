package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * Move the agent avoiding potential obstacles in its path.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param proximityRange
 *          the distance at which an obstacle is perceived by the pedestrian.
 */
class ObstacleAvoidance<W : Obstacle2D<Euclidean2DPosition>, T>(
    private val env: Environment2DWithObstacles<W, T>,
    override val reaction: SteeringBehavior<T>,
    pedestrian: Pedestrian2D<T>,
    private val proximityRange: Double
) : AbstractSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(env, reaction, pedestrian) {

    override fun cloneAction(n: Pedestrian<T, Euclidean2DPosition, Euclidean2DTransformation>, r: Reaction<T>) =
        requireNodeTypeAndProduce<Pedestrian2D<T>, ObstacleAvoidance<W, T>>(n) {
            require(r is SteeringBehavior<T>) { "steering behavior needed but found $reaction" }
            ObstacleAvoidance(env, r, it, proximityRange)
        }

    override fun nextPosition(): Euclidean2DPosition = target().let { target ->
        env.getObstaclesInRange(currentPosition, proximityRange)
            .asSequence()
            .map { obstacle: W ->
                obstacle.nearestIntersection(currentPosition, target) to obstacle.bounds2D
            }
            .minBy { (intersection, _) -> currentPosition.distanceTo(intersection) }
            ?.let { (intersection, bound) -> intersection to env.makePosition(bound.centerX, bound.centerY) }
            ?.let { (intersection, center) -> (intersection - center).coerceAtMost(maxWalk) }
            /*
             * Otherwise we just don't apply any repulsion force.
             */
            ?: env.origin
    }

    /**
     * Computes the target of the pedestrian, delegating to [reaction].steerStrategy.computeTarget.
     */
    private fun target(): Euclidean2DPosition = with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is ObstacleAvoidance<*, *> })
    }
}
