package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.smartcam.toPosition
import org.apache.commons.math3.util.FastMath.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

/**
 * Reads the target's absolute coordinates from the [target] molecule contained in [node] and sets the [node]'s heading accordingly.
 */
class HeadTowardTarget<T> @JvmOverloads constructor(
    node: Node<T>,
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val reaction: Reaction<T>,
    private val target: Molecule,
    private val angularSpeedDegrees: Double = 360.0
) : AbstractAction<T>(node) {

    private val angularSpeedRadians = toRadians(angularSpeedDegrees)

    override fun cloneAction(n: Node<T>, r: Reaction<T>) = HeadTowardTarget(n, env, r, target, angularSpeedDegrees)

    /**
     * Sets the heading of the node according to the target molecule.
     */
    override fun execute() {
        node.getConcentration(target)?.also {
            val speedRadians = angularSpeedRadians / reaction.timeDistribution.rate
            val targetPosition = it.toPosition(env)
            val myHeading = env.getHeading(node)
            if (targetPosition != myHeading) {
                if (speedRadians >= 2 * Math.PI) {
                    env.setHeading(node, targetPosition - env.getPosition(node))
                } else {
                    val targetAngle = (targetPosition - env.getPosition(node)).asAngle()
                    val currentAngle = env.getHeading(node).asAngle()
                    val rotation = shortestRotationAngle(currentAngle, targetAngle)
                    val absDistance = abs(rotation)
                    if (absDistance > 0) {
                        val newAngle = currentAngle + min(speedRadians, absDistance) * rotation.sign
                        env.setHeading(node, env.makePosition(cos(newAngle), sin(newAngle)))
                    }
                }
            }
        }
    }

    override fun getContext() = Context.LOCAL

    /**
     * Shortest distance in radians from the angles [from] to [to] in radians.
     * The sign determines whether it is clockwise or counter-clockwise.
     * Returns d so that [from] + d = [to].
     * Source: https://math.stackexchange.com/questions/110080/shortest-way-to-achieve-target-angle
     */
    private fun shortestRotationAngle(from: Double, to: Double) =
        (to - from + 3 * Math.PI) % (2 * Math.PI) - Math.PI
}