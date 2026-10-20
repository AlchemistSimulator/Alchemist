/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positions

import it.unibo.alchemist.model.Position
import org.apache.commons.math3.util.MathArrays
import org.apache.commons.math3.util.MathUtils

/**
 * N-dimensional position.
 *
 * @param P actual type
 * @param copy true if it is unsafe to store the [coordinates] array as-is.
 * @param coordinates the array of coordinates.
 */
abstract class AbstractPosition<P : Position<P>> protected constructor(copy: Boolean, coordinates: DoubleArray) :
    Position<P> {
    final override val coordinates: DoubleArray get() = internalCoordinates.copyOf()

    final override val dimensions: Int get() = internalCoordinates.size

    private val internalCoordinates: DoubleArray = if (copy) coordinates.copyOf() else coordinates
    private var hash = 0
    private var stringCache: String? = null

    init {
        MathUtils.checkFinite(internalCoordinates)
    }

    final override fun boundingBox(range: Double): List<P> = List(internalCoordinates.size) { i ->
        // Canonical base
        fromCoordinates(
            DoubleArray(internalCoordinates.size) { j ->
                internalCoordinates[j] +
                    if (i == j) -range else range
            },
        )
    }

    final override fun distanceTo(other: P): Double = MathArrays.distance(internalCoordinates, other.internal)

    final override fun equals(other: Any?): Boolean = this === other ||
        other != null && other.javaClass == javaClass &&
        internalCoordinates.contentEquals((other as AbstractPosition<*>).internalCoordinates)

    final override fun get(dimension: Int): Double {
        require(dimension in internalCoordinates.indices) {
            "$dimension is not an allowed dimension, " +
                "only values between 0 and ${internalCoordinates.lastIndex} are allowed."
        }
        return internalCoordinates[dimension]
    }

    final override fun hashCode(): Int {
        if (hash == 0) {
            hash = internalCoordinates.contentHashCode()
        }
        return hash
    }

    /**
     * Same as [minus], with the internal representation of [other].
     */
    operator fun minus(other: P): P = minus(other.internal)

    final override fun minus(other: DoubleArray): P =
        fromCoordinates(MathArrays.ebeSubtract(internalCoordinates, other))

    /**
     * Same as [plus], with the internal representation of [other].
     */
    operator fun plus(other: P): P = plus(other.internal)

    final override fun plus(other: DoubleArray): P = fromCoordinates(MathArrays.ebeAdd(internalCoordinates, other))

    /**
     * Prints the coordinates.
     */
    override fun toString(): String = stringCache ?: internalCoordinates.contentToString().also { stringCache = it }

    /**
     * Calls an internal constructor of subclasses that provides a way to instance a
     * new position (with the correct subtype) given its [coordinates].
     */
    protected abstract fun fromCoordinates(coordinates: DoubleArray): P

    private val P.internal: DoubleArray get() = (this as AbstractPosition<*>).internalCoordinates
}
