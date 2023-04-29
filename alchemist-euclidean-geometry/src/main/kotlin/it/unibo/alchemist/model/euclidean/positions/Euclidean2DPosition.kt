/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.euclidean.positions

import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.geometry.Vector2D
import javax.annotation.Nonnull

/**
 *
 */
class Euclidean2DPosition :
    AbstractEuclideanPosition<Euclidean2DPosition>,
    Position2D<Euclidean2DPosition>,
    Vector2D<Euclidean2DPosition> {
    /**
     * @param xp
     * The X coordinate
     * @param yp
     * The Y coordinate
     */
    constructor(xp: Double, yp: Double) : super(false, xp, yp)

    /**
     * @param c an array of length 2 containing the coordinates
     */
    constructor(c: DoubleArray) : super(false, *c) {
        require(c.size == 2) { "The array must have exactly two elements." }
    }

    override val x: Double
        get() = getCoordinate(0)
    override val y: Double
        get() = getCoordinate(1)

    override fun div(other: Double) = Euclidean2DPosition(x / other, y / other)

    override fun fromCoordinates(coordinates: DoubleArray): Euclidean2DPosition {
        require(coordinates.size == 2) {
            "Wrong number of coordinates for ${this::class.simpleName}: ${coordinates.toList()}"
        }
        return Euclidean2DPosition(coordinates[0], coordinates[1])
    }

    @Nonnull
    override fun normal(): Euclidean2DPosition {
        return Euclidean2DPosition(-y, x)
    }

    @Nonnull
    override fun newFrom(x: Double, y: Double): Euclidean2DPosition {
        return Euclidean2DPosition(x, y)
    }

    override fun times(other: Double) = Euclidean2DPosition(x * other, y * other)

    override val zero: Euclidean2DPosition get() = Companion.zero

    companion object {
        private const val serialVersionUID = 1L

        /**
         * Origin.
         */
        val zero = Euclidean2DPosition(0.0, 0.0)
    }
}
