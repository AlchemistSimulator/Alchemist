package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation

/**
 * Defines the possible transformations for a {@link GeometricShape} in a bidimensional euclidean space.
 */
interface Euclidean2DTransformation : GeometricTransformation<Euclidean2DPosition> {

    /**
     * Counter clockwise rotation
     *
     * @parameter angle the angle in radians
     */
    fun rotate(angle: Double)
}