package it.unibo.alchemist.model.influencesphere.shapes

import it.unibo.alchemist.model.interfaces.Position

/**
 * Defines a generic n-dimensional shape.
 */
interface GeometricShape<P : Position<P>> {

    /**
     * Whether or not a given point is inside this shape.
     */
    fun contains(point: P): Boolean
}