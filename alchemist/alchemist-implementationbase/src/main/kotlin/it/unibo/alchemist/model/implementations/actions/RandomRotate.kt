package it.unibo.alchemist.model.implementations.actions

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.random.RandomGenerator

/**
 * Changes the heading of the node randomly.
 */
class RandomRotate<T>(
    node: Node<T>,
    private val env: Physics2DEnvironment<T>,
    private val rng: RandomGenerator
) : AbstractAction<T>(node) {

    /**
     * {@inheritDoc}.
     */
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> =
        RandomRotate(n, env, rng)

    /**
     * Changes the heading of the node randomly.
     */
    override fun execute() {
        val delta = PI_8 * (2 * rng.nextDouble() - 1)
        val originalAngle = env.getHeading(node).asAngle()
        env.setHeading(node, (originalAngle + delta).toDirection())
    }

    /**
     * {@inheritDoc}.
     */
    override fun getContext() = Context.LOCAL

    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private fun Euclidean2DPosition.asAngle() = atan2(y, x)

    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private fun Double.toDirection() = Euclidean2DPosition(cos(this), sin(this))

    companion object {
        private const val PI_8 = Math.PI / 8
    }
}
