package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * Implementation of a basic pedestrian in the Euclidean world.
 */
class HomogeneousPedestrian2D<T>(
    env: EuclideanPhysics2DEnvironment<T>
) : HomogeneousPedestrianImpl<T>(env), Pedestrian2D {

    private val shape = env.defaultShape()

    /**
     * {@inheritDoc}
     */
    override fun getShape() = shape
}