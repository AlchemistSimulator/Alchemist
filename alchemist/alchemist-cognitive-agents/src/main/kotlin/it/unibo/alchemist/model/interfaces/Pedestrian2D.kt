package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.influencesphere.FieldOfView2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape

/**
 * The bidimensional representation of a pedestrian.
 */
interface Pedestrian2D<T> : Pedestrian<T> {

    /**
     * The shape of any pedestrian in the Euclidean world.
     *
     * @param env
     *          the environment appointed to create the shape.
     */
    fun shape(env: EuclideanPhysics2DEnvironment<T>): Euclidean2DShape = env.shapeFactory.circle(0.3)

    /**
     * The field of view of a pedestrian in the Euclidean world.
     *
     * @param env
     *          the environment where the pedestrian is.
     */
    fun fieldOfView(env: EuclideanPhysics2DEnvironment<T>): FieldOfView2D<T> =
        FieldOfView2D(env, this, 10.0, Math.PI / 180 * 80)
}
