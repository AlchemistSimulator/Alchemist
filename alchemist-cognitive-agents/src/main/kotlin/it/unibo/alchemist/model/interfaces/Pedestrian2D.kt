package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A bidimensional pedestrian.
 */
interface Pedestrian2D<T> : Pedestrian<T, Euclidean2DPosition, Euclidean2DTransformation> {

    /**
     * Access to the [environment].
     */
    val environment: Physics2DEnvironment<T>

    /**
     * The shape of any pedestrian in the Euclidean world.
     * Implementors should override this property to prevent the continuous creation of new [Euclidean2DShape]s.
     */
    override val shape: Euclidean2DShape get() = environment.shapeFactory.circle(defaultRadius)

    /**
     * The field of view of a pedestrian in the Euclidean world.
     * Implementors should override this property to prevent the continuous creation of new [FieldOfView2D]s.
     */
    val fieldOfView: FieldOfView2D<T> get() =
        FieldOfView2D(environment, this, defaultFieldOfViewDepth, defaultFieldOfViewAperture)

    companion object {
        /**
         * Default radius of pedestrian's [shape].
         */
        const val defaultRadius = 0.3
        /**
         * Default aperture of pedestrian's [fieldOfView].
         */
        const val defaultFieldOfViewAperture = Math.PI / 180 * 80
        /**
         * Default depth of pedestrian's [fieldOfView].
         */
        const val defaultFieldOfViewDepth = 10.0
    }
}
