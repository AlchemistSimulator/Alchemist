package it.unibo.alchemist.model.interfaces.geometry

/**
 * Models a generic shape.
 *
 * @param <S> Vector type for the space this shapes is defined in
 * @param <F> The transformations supported by the shapes in this space
 */
interface GeometricShape<S : Vector<S>, F : GeometricTransformation<S>> {

    /**
     * The largest distance between any pair of vertices.
     */
    val diameter: Double

    /**
     * The geometric center.
     */
    val centroid: S

    /**
     * A shape intersects another if any of its points is contained in the other one.
     *
     * @param other the other shape
     * @return true if the intersection is not empty
     */
    fun intersects(other: GeometricShape<S, F>): Boolean

    /**
     * @param vector the position vector
     * @return true if the vector is contained in the shape
     */
    fun contains(vector: S): Boolean

    fun transformed(transformation: F.() -> Unit): GeometricShape<S, F>

    /*


    /**
     * Returns a new shape centered in given origin.
     *
     * @param newOrigin the new origin
     * @return a new shape having its origin in the given position
     */
    fun withOrigin(newOrigin: S): GeometricShape<S>

    /**
     * Rotates the shape in its origin about the xy plane
     *
     * @param radians angle
     * @return a new shape
     */
    fun rotated(radians: Double): GeometricShape<S>
     */
}
