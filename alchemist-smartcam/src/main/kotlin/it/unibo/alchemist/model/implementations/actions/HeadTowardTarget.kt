package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.toPosition
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.util.FastMath.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

/**
 * Reads the target's absolute coordinates from the [target] molecule
 * contained in [node] and sets the node's heading accordingly.
 */
class HeadTowardTarget<T> @JvmOverloads constructor(
    node: Node<T>,
    private val environment: Physics2DEnvironment<T>,
    private val reaction: Reaction<T>,
    private val target: Molecule,
    private val angularSpeedDegrees: Double = 360.0
) : AbstractAction<T>(node) {

    private val angularSpeedRadians = toRadians(angularSpeedDegrees)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        HeadTowardTarget(node, environment, reaction, target, angularSpeedDegrees)

    /**
     * Sets the heading of the node according to the target molecule.
     */
    override fun execute() {
        node.getConcentration(target)?.also {
            val speedRadians = angularSpeedRadians / reaction.timeDistribution.rate
            val targetPosition = it.toPosition(environment)
            val myHeading = environment.getHeading(node)
            if (targetPosition != myHeading) {
                if (speedRadians >= 2 * Math.PI) {
                    environment.setHeading(node, targetPosition - environment.getPosition(node))
                } else {
                    val targetAngle = (targetPosition - environment.getPosition(node)).asAngle
                    val currentAngle = environment.getHeading(node).asAngle
                    val rotation = shortestRotationAngle(currentAngle, targetAngle)
                    val absDistance = abs(rotation)
                    if (absDistance > 0) {
                        val newAngle = currentAngle + min(speedRadians, absDistance) * rotation.sign
                        environment.setHeading(node, environment.makePosition(cos(newAngle), sin(newAngle)))
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
