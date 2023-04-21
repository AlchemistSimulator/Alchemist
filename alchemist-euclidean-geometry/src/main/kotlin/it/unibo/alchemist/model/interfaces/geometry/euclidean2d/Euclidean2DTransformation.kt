package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

/**
 * Defines the possible transformations for a [it.unibo.alchemist.model.interfaces.geometry.GeometricShape] in a
 * bidimensional euclidean space.
 */
interface Euclidean2DTransformation : GeometricTransformation<Euclidean2DPosition> {

    /**
     * Counter clockwise rotation.
     *
     * @param angle the angle in radians
     */
    fun rotate(angle: Double)

    /**
     * Rotates toward the specified direction.
     *
     * @param direction the direction vector
     */
    fun rotate(direction: Euclidean2DPosition) = rotate(direction.asAngle)

    /**
     * Changes origin.
     */
    fun origin(x: Double, y: Double) = origin(Euclidean2DPosition(x, y))
}
