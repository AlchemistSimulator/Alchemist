package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape

/**
 * A sphere of influence in the Euclidean world.
 *
 * @param environment
 *          the environment where this sphere of influence is.
 * @param owner
 *          the node who owns this sphere of influence.
 * @param shape
 *          the shape of this sphere of influence
 */
open class InfluenceSphere2D<T>(
    private val environment: Physics2DEnvironment<T>,
    private val owner: Node<T>,
    private val shape: Euclidean2DShape,
) : InfluenceSphere<T> {
    override fun influentialNodes(): List<Node<T>> = environment.getNodesWithin(
        shape.transformed {
            origin(environment.getPosition(owner))
            rotate(environment.getHeading(owner))
        },
    ).minusElement(owner)
}
