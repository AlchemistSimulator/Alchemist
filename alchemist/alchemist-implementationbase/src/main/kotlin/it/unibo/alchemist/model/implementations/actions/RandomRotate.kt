package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Changes the heading of the node randomly.
 */
class RandomRotate<T>(
    node: Node<T>,
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val rng: RandomGenerator
) : AbstractAction<T>(node) {

    /**
     * {@inheritDoc}
     */
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        RandomRotate(n, env, rng)

    /**
     * Changes the heading of the node randomly.
     */
    override fun execute() {
        val delta = Math.PI / 8 * (2 * rng.nextDouble() - 1)
        val originalAngle = env.getHeading(node).asAngle()
        env.setHeading(node, (originalAngle + delta).toDirection())
    }

    /**
     * {@inheritDoc}
     */
    override fun getContext() = Context.LOCAL

    private fun Euclidean2DPosition.asAngle() = atan2(y, x)
    private fun Double.toDirection() = Euclidean2DPosition(cos(this), sin(this))
}