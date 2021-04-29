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
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Bidimensional vector with [x] and [y] coordinates.
 */
interface Vector2D<P : Vector2D<P>> : Vector<P> {
    /**
     * x coordinate.
     */
    val x get() = this[0]

    /**
     * y coordinate.
     */
    val y get() = this[1]

    /**
     * Computes the angle with atan2(y, x).
     *
     * @return atan2(y, x) (in radians)
     */
    val asAngle: Double get() = atan2(y, x)

    /**
     * Dot product between bidimensional vectors.
     */
    override fun dot(other: P) = x * other.x + y * other.y

    /**
     * Checks whether the given point is inside a rectangular region described by an [origin]
     * point and [width] and [height] values (only positive).
     */
    fun isInRectangle(origin: Vector2D<*>, width: Double, height: Double): Boolean =
        x >= origin.x && y >= origin.y && x <= origin.x + width && y <= origin.y + height

    /**
     * Creates a new Vector2D with the same type of the current one with different [x] and [y].
     */
    fun newFrom(x: Double, y: Double): P

    /**
     * Allows subtraction with a [Pair].
     */
    operator fun minus(other: Pair<Double, Double>) = newFrom(x - other.first, y - other.second)

    /**
     * Normalizes the vector.
     */
    override fun normalized(): P = times(1.0 / sqrt(x * x + y * y))

    /**
     * Allows summaction with a [Pair].
     */
    operator fun plus(other: Pair<Double, Double>) = newFrom(x + other.first, y + other.second)

    /**
     * Computes a point which is at a certain [distance] and [angle] (in radians) from this one.
     */
    fun surroundingPointAt(angle: Double, distance: Double) =
        newFrom(x + cos(angle) * distance, y + sin(angle) * distance)

    /**
     * Computes a point which is at a certain [distance] and angle (expressed as a [versor] centered in this node)
     * from this one.
     */
    fun surroundingPointAt(versor: P, distance: Double) = surroundingPointAt(versor.asAngle, distance)

    /**
     * Creates a list of [count] points equally spaced in the circle of given [radius] with center in this vector.
     *
     * @param radius
     *          the distance each generated position must have from this.
     * @param count
     *          the number of positions to generate.
     */
    // @JvmOverloads disabled due to https://youtrack.jetbrains.com/issue/KT-12224
    fun surrounding(radius: Double, count: Int = 12): List<P> = (1..count)
        .map {
            @Suppress("UNCHECKED_CAST")
            surroundingPointAt(angle = it * Math.PI * 2 / count, distance = radius)
        }

    companion object {
        /**
         * Computes the z component of the cross product of the given vectors.
         */
        fun zCross(v1: Vector2D<*>, v2: Vector2D<*>) = v1.x * v2.y - v1.y * v2.x
    }
}
