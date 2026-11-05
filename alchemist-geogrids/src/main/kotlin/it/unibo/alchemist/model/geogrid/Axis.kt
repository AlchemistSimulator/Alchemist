/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid

/**
 * The coordinates of the nodes of a grid along one dimension, strictly ascending.
 */
interface Axis {

    /**
     * The interval from the first to the last coordinate.
     */
    val range: ClosedFloatingPointRange<Double>

    /**
     * The number of coordinates.
     */
    val size: Int

    /**
     * The [Cell] enclosing [coordinate].
     *
     * @throws IllegalArgumentException if [coordinate] is NaN.
     */
    fun enclosingCellOf(coordinate: Double): Cell

    /**
     * Two axes are equal when they have the same coordinates, in the same order.
     */
    override fun equals(other: Any?): Boolean

    /**
     * The coordinate at [index].
     */
    operator fun get(index: Int): Double

    /**
     * The [DoubleArray.contentHashCode] of the coordinates, consistently with [equals].
     */
    override fun hashCode(): Int

    /**
     * A copy of the coordinates.
     */
    fun toDoubleArray(): DoubleArray

    /**
     * The cell of an axis enclosing a coordinate: the [lower] and [upper] indices of the axis values around it,
     * and the [weight] of the coordinate between them, 0 at [lower] and 1 at [upper].
     * A coordinate on a value, or outside the axis, has [lower] equal to [upper] and weight 0.
     */
    @ConsistentCopyVisibility
    data class Cell internal constructor(val lower: Int, val upper: Int, val weight: Double)
}
