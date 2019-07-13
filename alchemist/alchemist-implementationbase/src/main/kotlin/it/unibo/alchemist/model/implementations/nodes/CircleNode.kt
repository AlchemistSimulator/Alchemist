package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * A {@link Node} with a circle shape meant to be added to a {@link PhysicsEnvironment}.
 */
open class CircleNode<T>(
    env: EuclideanPhysics2DEnvironment<T>,
    radius: Double = 5.0
) : AbstractNode<T>(env) {

    private val shape = env.shapeFactory.circle(radius)

    /**
     * {@inheritDoc}
     */
    final override fun getShape() = shape

    /**
     * Returns null because T is unknown
     */
    override fun createT() = null
}