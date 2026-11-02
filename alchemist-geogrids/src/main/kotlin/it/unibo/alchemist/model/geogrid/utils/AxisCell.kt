/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import java.util.Arrays

/**
 * The cell of an axis enclosing a coordinate: the [lower] and [upper] indices of the axis values around it,
 * and the [weight] of the coordinate between them, 0 at [lower] and 1 at [upper].
 * A coordinate on a value, or outside the axis, has [lower] equal to [upper] and weight 0.
 */
internal data class AxisCell(val lower: Int, val upper: Int, val weight: Double) {

    companion object {
        /**
         * The [AxisCell] of this axis enclosing [coordinate].
         * The axis must be strictly ascending and free of NaNs: this is not checked, to keep the lookup logarithmic.
         *
         * @throws IllegalArgumentException if the axis is empty or [coordinate] is NaN.
         */
        internal fun DoubleArray.enclosingCellOf(coordinate: Double): AxisCell {
            require(isNotEmpty()) { "Axis cannot be empty." }
            require(!coordinate.isNaN()) { "The query coordinate cannot be NaN." }
            val searchResult = Arrays.binarySearch(this, coordinate)
            // binarySearch returns -(insertion point) - 1 on a miss: the insertion point is the first greater value
            val upper = -searchResult - 1
            return when {
                searchResult >= 0 -> AxisCell(searchResult, searchResult, 0.0)
                upper == 0 -> AxisCell(0, 0, 0.0)
                upper == size -> AxisCell(lastIndex, lastIndex, 0.0)
                else -> AxisCell(upper - 1, upper, (coordinate - this[upper - 1]) / (this[upper] - this[upper - 1]))
            }
        }
    }
}
