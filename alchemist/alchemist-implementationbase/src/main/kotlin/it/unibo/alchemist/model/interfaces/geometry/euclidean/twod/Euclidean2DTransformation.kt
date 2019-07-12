package it.unibo.alchemist.model.interfaces.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import kotlin.math.atan2

/**
 * Defines the possible transformations for a {@link GeometricShape} in a bidimensional euclidean space.
 */
interface Euclidean2DTransformation : GeometricTransformation<Euclidean2DPosition> {

    /**
     * Counter clockwise rotation
     *
     * @param angle the angle in radians
     */
    fun rotate(angle: Double)

    /**
     * Rotates toward the specified direction.
     *
     * @param direction the direction vector
     */
    fun rotate(direction: Euclidean2DPosition) =
        rotate(atan2(direction.y, direction.x))
}