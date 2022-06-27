/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.interfaces

import java.io.Serializable

/**
 * An interface to represent a generic coordinates system.
 *
 * @param <P>
 *            the actual {@link Position} type: this strategy allows to
 *            progressively refine the {@link Position} by inheritance, allowing
 *            for specifying incrementally fine grained model elements.
 */
interface Position<P : Position<P>> : Serializable {

    /**
     * Given a range, produces N coordinates, representing the N opposite
     * vertices of the hypercube having the current coordinate as center and
     * circumscribing the N-sphere defined by the range. In the case of two
     * dimensional coordinates, it must return the opposite vertices of the
     * square circumscribing the circle with center in this position and radius
     * range.
     *
     * @param range
     *            the radius of the hypersphere
     * @return the vertices of the circumscribed hypercube
     */
    fun boundingBox(range: Double): List<P>

    /**
     * Allows to get the position as a Number array.
     *
     * @return an array of size getDimensions() where each element represents a
     *         coordinate.
     */
    val coordinates: DoubleArray

    /**
     * Allows to access the value of a coordinate.
     *
     * @param dimension
     *            the dimension. E.g., in a 2-dimensional implementation, 0
     *            could be the X-axis and 1 the Y-axis
     * @return the coordinate value
     */
    fun getCoordinate(dimension: Int): Double

    /**
     * @return the number of dimensions of this {@link Position}.
     */
    val dimensions: Int

    /**
     * Computes the distance between this position and another compatible
     * position.
     *
     * @param other
     *            the position you want to know the distance to
     * @return the distance between this and p
     */
    fun distanceTo(other: P): Double

    /**
     * Considers both positions as vectors, and sums them.
     *
     * @param other the other position
     * @return a new {@link Position} that is the sum of the two.
     */
    operator fun plus(other: DoubleArray): P

    /**
     * Considers both positions as vectors, and returns the difference between this position and the passed one.
     *
     * @param other the other position
     * @return a new {@link Position} that is this position minus the one passed.
     */
    operator fun minus(other: DoubleArray): P
}
