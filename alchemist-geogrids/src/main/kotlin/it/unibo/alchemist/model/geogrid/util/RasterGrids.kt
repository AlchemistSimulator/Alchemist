/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.util

import it.unibo.alchemist.model.geogrid.Axis
import it.unibo.alchemist.model.geogrid.RasterGrid

/**
 * Utilities for the [RasterGrid]s storing their values row by row:
 * all longitudes of the first latitude, then of the second, and so on.
 */
internal object RasterGrids {

    /**
     * Checks that [values] hold exactly one value per cell of the grid of [latitudes] and [longitudes].
     *
     * @throws IllegalArgumentException otherwise.
     */
    fun requireOneValuePerCell(latitudes: Axis, longitudes: Axis, values: DoubleArray) {
        val expectedSize = latitudes.size * longitudes.size
        require(values.size == expectedSize) {
            "Dimension mismatch: expected $expectedSize values " +
                "(${latitudes.size} lat x ${longitudes.size} lon), but got ${values.size}"
        }
    }

    /**
     * The index, row by row, of the cell at the [latIndex]-th latitude and [lonIndex]-th longitude.
     *
     * @throws IndexOutOfBoundsException if [latIndex] or [lonIndex] is outside its axis.
     */
    fun RasterGrid.flatIndexOf(latIndex: Int, lonIndex: Int): Int {
        if (latIndex !in 0 until latitudes.size || lonIndex !in 0 until longitudes.size) {
            throw IndexOutOfBoundsException(
                "Cell ($latIndex, $lonIndex) is outside the ${latitudes.size} x ${longitudes.size} grid",
            )
        }
        return latIndex * longitudes.size + lonIndex
    }
}
