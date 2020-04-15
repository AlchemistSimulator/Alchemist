package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles

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
    private val env: Environment2DWithObstacles<W, T, Euclidean2DPosition>,
    override val reaction: SteeringBehavior<T>,
    pedestrian: Pedestrian<T>,
    private val proximityRange: Double
) : SteeringActionImpl<T, Euclidean2DPosition>(env, reaction, pedestrian) {

    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        ObstacleAvoidance(env, r as SteeringBehavior<T>, n as Pedestrian<T>, proximityRange)

    override fun nextPosition(): Euclidean2DPosition = target().let { target ->
        env.getObstaclesInRange(currentPosition, proximityRange)
            .asSequence()
            .map { obstacle: W ->
                obstacle.nearestIntersection(currentPosition, target) to obstacle.bounds2D
            }
            .minBy { (intersection, _) -> currentPosition.distanceTo(intersection) }
            ?.let { (intersection, bound) -> intersection to env.makePosition(bound.centerX, bound.centerY) }
            ?.let { (intersection, center) -> (intersection - center).resizeToMaxWalkIfGreater() }
            /*
             * Otherwise we just don't apply any repulsion force.
             */
            ?: env.makePosition(0.0, 0.0)
    }

    /*
     * This steering action needs to know the heading of the pedestrian in order to compute
     * a suitable repulsion force for the obstacles which are in such direction.
     * This is how the pedestrian's target is computed at present, consider using
     * [PhysicsEnvironment.getHeading] in the future.
     */
    private fun target(): Euclidean2DPosition = with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is ObstacleAvoidance<*, *> })
    }
}
