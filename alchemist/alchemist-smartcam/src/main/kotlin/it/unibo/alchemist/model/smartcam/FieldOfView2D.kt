package it.unibo.alchemist.model.smartcam

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment

/**
 * TODO: Use influence spheres
 */
class FieldOfView2D<T> (
    private val env: EuclideanPhysics2DEnvironment<T>,
    position: Euclidean2DPosition,
    distance: Double,
    angle: Double,
    heading: Double = 0.0
) {

    private val shape = env.shapeFactory.circleSector(distance, angle, heading).transformed { origin(position) }
    
    /**
     * TODO: Use influence spheres
     */
    fun influencedNodes() =
        env.getNodesWithin(shape)
}