package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * Implementation of a basic pedestrian in the Euclidean world.
 */
class HomogeneousPedestrian2D<T>(
    env: EuclideanPhysics2DEnvironment<T>
) : HomogeneousPedestrianImpl<T>(env) {

    private val shape = env.shapeFactory.circle(0.2)

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}