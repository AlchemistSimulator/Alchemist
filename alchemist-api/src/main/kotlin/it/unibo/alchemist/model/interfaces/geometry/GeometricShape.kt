/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

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
     * Half the [diameter].
     */
    val radius: Double get() = diameter / 2

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
