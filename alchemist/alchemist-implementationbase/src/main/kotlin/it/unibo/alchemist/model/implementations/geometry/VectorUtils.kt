/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Finds the magnitude of a vector.
 */
fun <V : Vector<V>> Vector<V>.magnitude(): Double {
    var sum = 0.0
    for (d in 0 until dimensions) {
        sum += getCoordinate(d).pow(2)
    }
    return sqrt(sum)
}

/**
 * Computes the dot product between two vectors.
 */
fun <V : Vector<V>> Vector<V>.dot(other: V): Double {
    var dot = 0.0
    for (d in 0 until dimensions) {
        dot += getCoordinate(d) * other.getCoordinate(d)
    }
    return dot
}

/**
 * Computes the angle in radians between two vectors.
 */
fun <V : Vector<V>> Vector<V>.angleBetween(other: V): Double =
    acos(dot(other) / (magnitude() * other.magnitude()))

/**
 * Multiplies each coordinate of a vector for a scalar number n.
 */
fun Euclidean2DPosition.times(n: Double) = Euclidean2DPosition(x * n, y * n)

/**
 * Normalizes the vector.
 */
fun Euclidean2DPosition.normalize(): Euclidean2DPosition = times(1.0 / sqrt(x * x + y * y))

/**
 * Resizes the vector in order for it to have a length equal
 * to the specified parameter. Its direction and verse are preserved.
 */
fun Euclidean2DPosition.resize(newLen: Double): Euclidean2DPosition = normalize().times(newLen)

/**
 * Find the normal of a vector.
 */
fun Euclidean2DPosition.normal(): Euclidean2DPosition = Euclidean2DPosition(-y, x)

/**
 * Computes the z component of the cross product of the given vectors.
 */
fun zCross(v1: Euclidean2DPosition, v2: Euclidean2DPosition) = v1.x * v2.y - v1.y * v2.x

/**
 * Dot product between bidimensional vectors.
 */
fun Euclidean2DPosition.dot(other: Euclidean2DPosition) = x * other.x + y * other.y
