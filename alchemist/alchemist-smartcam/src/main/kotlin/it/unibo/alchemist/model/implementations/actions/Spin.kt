package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.geometry.asAngle
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.util.FastMath.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spins a [node] around itself at [angularSpeedDegrees] normalized according to the speed of the [reaction].
 */
class Spin<T>(
    node: Node<T>,
    private val reaction: Reaction<T>,
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val angularSpeedDegrees: Double
) : AbstractAction<T>(node) {

    private val angularSpeedRadians = toRadians(angularSpeedDegrees)

    override fun cloneAction(n: Node<T>, r: Reaction<T>) = Spin(n, r, env, angularSpeedDegrees)

    /**
     * Spins the node around itself.
     */
    override fun execute() {
        val realSpeed = angularSpeedRadians / reaction.timeDistribution.rate
        val headingAngle = env.getHeading(node).asAngle() + realSpeed
        env.setHeading(node, env.makePosition(cos(headingAngle), sin(headingAngle)))
    }

    override fun getContext() = Context.LOCAL
}
