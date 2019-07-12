package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * A {@link Node} with a circle shape meant to be added to a {@link PhysicsEnvironment}.
 */
open class CircleNode<T>(
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val radius: Double = 5.0
) : AbstractNode<T>(env) {

    /**
     * {@inheritDoc}
     */
    final override fun getShape() =
        env.shapeFactory.circle(radius)

    /**
     * Returns null because T is unknown
     */
    override fun createT() = null
}