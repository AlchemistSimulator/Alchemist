/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry

import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Bidimensional vector with [x] and [y] coordinates.
 */
interface Vector2D<P : Vector2D<P>> : Vector<P> {
    /**
     * x coordinate.
     */
    @JvmDefault
    val x get() = this[0]

    /**
     * y coordinate.
     */
    @JvmDefault
    val y get() = this[1]

    /**
     * Computes the angle with atan2(y, x).
     *
     * @return atan2(y, x) (in radians)
     */
    @JvmDefault
    val asAngle: Double get() = atan2(y, x)

    /**
     * Normalizes the vector.
     */
    @JvmDefault
    override fun normalized(): P = times(1.0 / sqrt(x * x + y * y))

    /**
     * Dot product between bidimensional vectors.
     */
    @JvmDefault
    override fun dot(other: P) = x * other.x + y * other.y

    /**
     * Creates a new Vector2D with the same type of the current one with different [x] and [y].
     */
    fun newFrom(x: Double, y: Double): P

    companion object {
        /**
         * Computes the z component of the cross product of the given vectors.
         */
        fun zCross(v1: Vector2D<*>, v2: Vector2D<*>) = v1.x * v2.y - v1.y * v2.x
    }
}
