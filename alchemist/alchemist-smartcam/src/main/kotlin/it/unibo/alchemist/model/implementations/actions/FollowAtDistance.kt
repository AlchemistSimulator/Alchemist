package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.implementations.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.implementations.movestrategies.target.FollowTarget
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import kotlin.math.cos
import kotlin.math.sin

private class FollowTargetAtDistance<T>(
    env: Environment<T, Euclidean2DPosition>,
    n: Node<T>,
    targetMolecule: Molecule,
    private val distance: Double
) : FollowTarget<T, Euclidean2DPosition>(env, n, targetMolecule) {
    override fun createPosition(x: Double, y: Double): Euclidean2DPosition {
        val dest = super.createPosition(x, y)
        val direction = currentPosition - dest
        val angle = direction.asAngle()
        return super.createPosition(x + distance * cos(angle), y + distance * sin(angle))
    }
}

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
    reaction: Reaction<T>,
    private val env: Environment<T, Euclidean2DPosition>,
    private val target: Molecule,
    private val distance: Double,
    private val speed: Double
) : AbstractConfigurableMoveNode<T, Euclidean2DPosition>(
    env,
    node,
    RoutingStrategy { p1, p2 -> PolygonalChain<Euclidean2DPosition>(listOf(p1, p2)) },
    FollowTargetAtDistance<T>(env, node, target, distance),
    ConstantSpeed(reaction, speed)
) {
    override fun cloneAction(n: Node<T>, r: Reaction<T>) =
        FollowAtDistance(n, r, env, target, distance, speed)

    override fun getDestination(current: Euclidean2DPosition, t: Euclidean2DPosition, maxWalk: Double): Euclidean2DPosition {
        val direction = t - current
        if (current.getDistanceTo(t) < maxWalk) {
            return direction
        }
        val angle = direction.asAngle()
        return environment.makePosition(maxWalk * cos(angle), maxWalk * sin(angle))
    }
}