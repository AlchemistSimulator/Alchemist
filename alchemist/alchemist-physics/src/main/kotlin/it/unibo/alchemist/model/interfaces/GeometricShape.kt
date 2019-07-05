package it.unibo.alchemist.model.interfaces

import java.awt.Shape

/**
 * Models a generic shape.
 *
 * @param <P> Position type
 */
interface GeometricShape<P : Position<P>> {

    /**
     * The largest distance between any pair of vertices.
     */
    val diameter: Double

    /**
     * The geometric center.
     */
    val centroid: P

    /**
     * Whether the given point is inside this shape.
     *
     * @param point the given point
     * @return true if this shape contains the given point
     */
    operator fun contains(point: P): Boolean

    /**
     * A shape intersects another if any of its points is contained in the other one.
     *
     * @param other the other shape
     * @return true if the intersection is not empty
     */
    fun intersects(other: GeometricShape<P>): Boolean

    /**
     * Returns a new shape with the given origin.
     *
     * @param position the new origin
     * @return a new shape having its origin in the given position
     */
    fun withOrigin(position: P): GeometricShape<P>

    /**
     * Rotates the shape around its origin
     * @param radians angle
     * @return a new shape
     */
    fun rotate(radians: Double): GeometricShape<P>

    /**
     * Handy method for compatibility with java's awt geometry
     */
    fun asShape(): Shape
}
