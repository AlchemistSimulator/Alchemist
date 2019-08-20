package it.unibo.alchemist.model.interfaces.geometry

import java.io.Serializable

/**
 * Models a generic shape.
 *
 * @param <S> Vector type for the space this shapes is defined in
 * @param <A> The transformations supported by the shapes in this space
 */
interface GeometricShape<S : Vector<S>, A : GeometricTransformation<S>> : Serializable {

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
    fun intersects(other: GeometricShape<S, A>): Boolean

    /**
     * Check if the shape contains a vector.
     * @param vector the position vector
     * @return true if the vector is contained in the shape
     */
    fun contains(vector: S): Boolean

    /**
     * Transforms the shape.
     * @param transformation describes the transformations to apply to the shape
     * @return a copy of the shape transformed accordingly
     */
    fun transformed(transformation: A.() -> Unit): GeometricShape<S, A>
}
