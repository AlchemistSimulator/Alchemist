package it.unibo.alchemist.model.interfaces.geometry

/**
 * A generic vector in a multidimensional space
 *
 * @param <S> self type to prevent vector operations between vectors of different spaces.
 */
interface Vector<S : Vector<S>> {

    /**
     * The dimensions of the space this vector belongs to.
     */
    val dimensions: Int

    /**
     * The coordinate of this vector in the specified dimension relatively to the basis its space is described with.
     *
     * @param dim
     *            the dimension. E.g., in a 2-dimensional implementation, 0
     *            could be the X-axis and 1 the Y-axis
     * @return the coordinate value
     */
    fun getCoordinate(dim: Int): Double

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
}
