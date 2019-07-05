package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.Position2D


/**
 * A {@link Node} with a circle shape meant to be added to a {@link PhysicsEnvironment}.
 */
class CircleNode<T, P : Position2D<P>>(
    env: PhysicsEnvironment<T, P>,
    radius: Double = 5.0
) : AbstractNode<T>(env) {
    init {
        env.setShape(this, env.shapeFactory.circle(radius))
    }

    /**
     * Returns null because T is unknown
     */
    override fun createT() = null
}