package it.unibo.alchemist.model.interfaces.geometry

import kotlin.math.acos
import kotlin.math.sqrt

/**
 * A generic vector in a multidimensional space.
 *
 * @param S self type to prevent vector operations between vectors of different spaces.
 */
interface Vector<S : Vector<S>> {

    /**
     * The dimensions of the space this vector belongs to.
     */
    val dimensions: Int

    /**
     * Coordinates for a Cartesian space.
     * Implementors must guarantee that internal state is not exposed.
     */
    val coordinates: DoubleArray

    /**
     * The coordinate of this vector in the specified dimension relatively to the basis
     * its space is described with.
     *
     * @param dim
     *            the dimension. E.g., in a 2-dimensional implementation, 0 could be the
     *            X-axis and 1 the Y-axis
     * @return the coordinate value
     */
    operator fun get(dim: Int): Double

    /**
     * Support for sum.
     * Note: the dimensions must coincide.
     * @return a vector containing the result
     */
    operator fun plus(other: S): S

    /**
     * Support for subtraction.
     * Note: the dimensions must coincide.
     * @return a vector containing the result
     */
    operator fun minus(other: S): S

    /**
     * Multiplication by a Double.
     * @return the resulting vector, the operation is not performed in-place.
     */
    operator fun times(other: Double): S

    /**
     * Division by a Double.
     * @return the resulting vector, the operation is not performed in-place.
     */
    operator fun div(other: Double): S = times(1 / other)

    /**
     * Finds the magnitude of a vector.
     */
    val magnitude get() = sqrt(coordinates.map { it * it }.sum())

    /**
     * Computes the dot product between two vectors.
     */
    fun dot(other: S): Double = coordinates
        .zip(other.coordinates)
        .map { (a, b) -> a * b }
        .sum()

    /**
     * Computes the angle in radians between two vectors.
     */
    fun angleBetween(other: S): Double = acos(dot(other) / (magnitude * other.magnitude))

    /**
     * Computes the distance between two vectors, interpreted as points in an Euclidean
     * space. Throws [IllegalArgumentException] if vectors have different dimensions.
     */
    fun distanceTo(other: S): Double

    /**
     * @return a resized version of the vector, whose magnitude is equal to [newLen].
     * Direction and verse of the original vector are preserved.
     */
    fun resized(newLen: Double): S = normalized().times(newLen)

    /**
     * @return a normalized version of the vector (i.e. of unitary magnitude).
     */
    fun normalized(): S

    @Suppress("UNCHECKED_CAST")
    private fun resizedIf(toBeResized: Boolean, newLen: Double): S = when {
        toBeResized -> resized(newLen)
        else -> this as S
    }

    /**
     * @return this vector if its [magnitude] is smaller than or equal to [maximumMagnitude] or a resized version
     * of [maximumMagnitude] otherwise.
     */
    fun coerceAtMost(maximumMagnitude: Double): S =
        resizedIf(magnitude > maximumMagnitude, maximumMagnitude)

    /**
     * @return this vector if its [magnitude] is greater than or equal to [minimumMagnitude] or a resized version
     * of [minimumMagnitude] otherwise.
     */
    fun coerceAtLeast(minimumMagnitude: Double): S =
        resizedIf(magnitude < minimumMagnitude, minimumMagnitude)

    /**
     * Performs a coercion at least and at most.
     */
    fun coerceIn(minimumMagnitude: Double, maximumMagnitude: Double): S =
        coerceAtLeast(minimumMagnitude).coerceAtMost(maximumMagnitude)

    /**
     * Find the normal of a vector.
     */
    fun normal(): S
}
