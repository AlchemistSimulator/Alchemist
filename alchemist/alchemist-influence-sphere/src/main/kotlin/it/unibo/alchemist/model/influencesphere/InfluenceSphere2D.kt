package it.unibo.alchemist.model.influencesphere

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape

/**
 * A sphere of influence in the Euclidean world.
 *
 * @param env
 *          the environment where this sphere of influence is.
 * @param owner
 *          the node who owns this sphere of influence.
 * @param shape
 *          the shape of this sphere of influence
 */
open class InfluenceSphere2D<T>(
    private val env: EuclideanPhysics2DEnvironment<T>,
    private val owner: Node<T>,
    private val shape: Euclidean2DShape
) : InfluenceSphere {

    override fun influentialNodes(): List<Node<T>> = env.getNodesWithin(shape.transformed {
        origin(env.getPosition(owner))
        rotate(env.getHeading(owner))
    }).minusElement(owner)
}
