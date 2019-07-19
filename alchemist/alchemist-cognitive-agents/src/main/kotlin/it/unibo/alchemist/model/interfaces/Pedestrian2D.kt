package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape

/**
 * The bidimensional representation of a pedestrian.
 */
interface Pedestrian2D {

    /**
     * The default shape of any pedestrian in the Euclidean world.
     */
    fun EuclideanPhysics2DEnvironment<*>.defaultShape(): Euclidean2DShape = shapeFactory.circle(0.5)
}