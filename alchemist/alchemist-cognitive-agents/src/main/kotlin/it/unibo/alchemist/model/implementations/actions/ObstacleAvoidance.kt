package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithObstacles
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import kotlin.math.cos
import kotlin.math.sin

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
class ObstacleAvoidance<W : Obstacle2D, T>(
    private val env: EuclideanPhysics2DEnvironmentWithObstacles<W, T>,
    reaction: Reaction<T>,
    pedestrian: Pedestrian<T>,
    private val proximityRange: Double
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    reaction,
    pedestrian,
    TargetSelectionStrategy {
        val currentPosition = env.getPosition(pedestrian) ?: env.origin()
        val currentHeading = env.getHeading(pedestrian).asAngle()
        if (env.getObstaclesInRange(currentPosition.x, currentPosition.y, proximityRange).isEmpty()) {
            Euclidean2DPosition(Double.MAX_VALUE, Double.MAX_VALUE)
        } else {
            currentPosition + Euclidean2DPosition(cos(currentHeading) * proximityRange, sin(currentHeading) * proximityRange)
        }
    }
) {

    override fun getDestination(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double): Euclidean2DPosition =
        super.getDestination(
            current,
            nearestObstacle(target)?.let {
                it.second - it.first.bounds2D.let { rect -> env.makePosition(rect.centerX, rect.centerY) }
            } ?: target,
            maxWalk
        )

    private fun nearestObstacle(target: Euclidean2DPosition): Pair<W, Euclidean2DPosition>? = with(currentPosition) {
        env.getObstaclesInRange(x, y, proximityRange)
            .map { shape -> shape to this + shape.next(x, y, target.x, target.y).let { env.makePosition(it.first, it.second) } }
            .minBy { getDistanceTo(it.second) }
    }
}