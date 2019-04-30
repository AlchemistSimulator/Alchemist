package it.unibo.alchemist.shapes

import it.unibo.alchemist.model.interfaces.Position

/**
 * Defines a generic n-dimensional shape.
 */
interface GeometricShape<P : Position<P>> {

    /**
     * Whenever or not a given point is inside this shape.
     */
    fun contains(point: P): Boolean
}