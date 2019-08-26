package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.movestrategies.speed.GloballyConstantSpeed
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.smartcam.closestPositionToTargetAtDistance
import it.unibo.alchemist.model.smartcam.toPosition
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Follows a target at distance.
 * @param <T> concentration type
 * @param env the environment containing the nodes
 * @param node the follower
 * @param reaction the reaction hosting this action
 * @param target molecule from which to read the destination to follow in the form of coordinates or a tuple
 * @param distance the distance to keep from the destination
 * @param speed the maximum speed
 */
class FollowAtDistance<T>(
    node: Node<T>,
    private val reaction: Reaction<T>,
    private val env: Environment<T, Euclidean2DPosition>,
    private val target: Molecule,
    private val distance: Double,
    private val speed: Double
) : AbstractAction<T>(node) {

    private val speedStrategy = GloballyConstantSpeed<Euclidean2DPosition>(reaction, speed)

    override fun cloneAction(n: Node<T>, r: Reaction<T>) = FollowAtDistance(n, r, env, target, distance, speed)

    override fun execute() {
        node.getConcentration(target)?.also {
            val targetPosition = it.toPosition(env)
            val currentPosition = env.getPosition(node)
            var destination = closestPositionToTargetAtDistance(env, currentPosition, targetPosition, distance)
            if (currentPosition != destination) { // avoid "bouncing"
                val currentSpeed = min(speedStrategy.getNodeMovementLength(destination), currentPosition.getDistanceTo(destination))
                val direction = destination - currentPosition
                val angle = direction.asAngle()
                destination = currentPosition + Euclidean2DPosition(currentSpeed * cos(angle), currentSpeed * sin(angle))
                env.moveNodeToPosition(node, destination)
            }
        }
    }

    override fun getContext() = Context.LOCAL
}
