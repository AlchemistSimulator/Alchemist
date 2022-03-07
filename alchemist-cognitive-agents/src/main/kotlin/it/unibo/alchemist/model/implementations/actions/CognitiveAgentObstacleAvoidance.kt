package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.reactions.SteeringBehavior
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithObstacles
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * Move the agent avoiding potential obstacles in its path.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param node
 *          the owner of this action.
 * @param proximityRange
 *          the distance at which an obstacle is perceived by the node.
 */
class CognitiveAgentObstacleAvoidance<W : Obstacle2D<Euclidean2DPosition>, T>(
    private val environment: Environment2DWithObstacles<W, T>,
    override val reaction: SteeringBehavior<T>,
    node: Node<T>,
    private val proximityRange: Double
) : AbstractSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, node) {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentObstacleAvoidance<W, T> {
        require(reaction is SteeringBehavior<T>) { "steering behavior needed but found ${this.reaction}" }
        return CognitiveAgentObstacleAvoidance(environment, reaction, node, proximityRange)
    }

    override fun nextPosition(): Euclidean2DPosition = target().let { target ->
        environment.getObstaclesInRange(currentPosition, proximityRange)
            .asSequence()
            .map { obstacle: W ->
                obstacle.nearestIntersection(currentPosition, target) to obstacle.bounds2D
            }
            .minByOrNull { (intersection, _) -> currentPosition.distanceTo(intersection) }
            ?.let { (intersection, bound) -> intersection to environment.makePosition(bound.centerX, bound.centerY) }
            ?.let { (intersection, center) -> (intersection - center).coerceAtMost(maxWalk) }
            /*
             * Otherwise we just don't apply any repulsion force.
             */
            ?: environment.origin
    }

    /**
     * Computes the target of the node, delegating to [reaction].steerStrategy.computeTarget.
     */
    private fun target(): Euclidean2DPosition = with(reaction) {
        steerStrategy.computeTarget(steerActions().filterNot { it is CognitiveAgentObstacleAvoidance<*, *> })
    }
}
