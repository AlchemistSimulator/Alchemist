/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.axes

import it.unibo.alchemist.model.geogrid.Axis
import java.util.Arrays

/**
 * The coordinates of the nodes of a grid along one dimension, strictly ascending.
 * The [coordinates] are copied, so later changes to the array do not affect the axis.
 *
 * @throws IllegalArgumentException if [coordinates] is empty, or not strictly ascending, or contains NaN.
 */
class ImmutableAxis(coordinates: DoubleArray) : Axis {

    private val coordinates: DoubleArray = coordinates.copyOf()

    init {
        require(this.coordinates.isNotEmpty()) { "An axis needs at least one coordinate." }
        // NaN fails every comparison, so it is rejected as well
        require(this.coordinates.none(Double::isNaN) && (1 until size).all { this[it - 1] < this[it] }) {
            "Axis coordinates must be strictly ascending, but got ${this.coordinates.contentToString()}"
        }
    }

    override val size: Int get() = coordinates.size

    override val range: ClosedFloatingPointRange<Double> get() = coordinates.first()..coordinates.last()

    override operator fun get(index: Int): Double = coordinates[index]

    override fun toDoubleArray(): DoubleArray = coordinates.copyOf()

    override fun equals(other: Any?): Boolean = when (other) {
        is ImmutableAxis -> coordinates.contentEquals(other.coordinates)
        is Axis -> coordinates.contentEquals(other.toDoubleArray())
        else -> false
    }

    override fun hashCode(): Int = coordinates.contentHashCode()

    override fun toString(): String = "Axis${coordinates.contentToString()}"

    override fun enclosingCellOf(coordinate: Double): Axis.Cell {
        require(!coordinate.isNaN()) { "The query coordinate cannot be NaN." }
        val searchResult = Arrays.binarySearch(coordinates, coordinate)
        // binarySearch returns -(insertion point) - 1 on a miss: the insertion point is the first greater value
        val upper = -searchResult - 1
        return when {
            searchResult >= 0 -> Axis.Cell(searchResult, searchResult, 0.0)
            upper == 0 -> Axis.Cell(0, 0, 0.0)
            upper == size -> Axis.Cell(size - 1, size - 1, 0.0)
            else -> Axis.Cell(upper - 1, upper, (coordinate - this[upper - 1]) / (this[upper] - this[upper - 1]))
        }
    }
}
