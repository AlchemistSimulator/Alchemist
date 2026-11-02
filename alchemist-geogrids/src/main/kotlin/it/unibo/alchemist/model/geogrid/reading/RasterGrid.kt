/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

import it.unibo.alchemist.model.GeoPosition

/**
 * Values over a latitude/longitude grid, in degrees, with [Double.NaN] for the missing ones.
 *
 * @property latitudes the latitudes of the grid.
 * @property longitudes the longitudes of the grid.
 * @param gridValues the values, row by row: all longitudes of the first latitude, then of the second, and so on.
 * @throws IllegalArgumentException if there is not exactly one value per cell.
 */
abstract class RasterGrid(val latitudes: Axis, val longitudes: Axis, gridValues: DoubleArray) {

    init {
        val expectedSize = latitudes.size * longitudes.size
        require(gridValues.size == expectedSize) {
            "Dimension mismatch: expected $expectedSize values " +
                "(${latitudes.size} lat x ${longitudes.size} lon), but got ${gridValues.size}"
        }
    }

    /**
     * The value at the [latIndex]-th latitude and [lonIndex]-th longitude, [Double.NaN] if missing.
     */
    abstract fun valueAt(latIndex: Int, lonIndex: Int): Double

    /**
     * Whether [position] lies within the grid.
     */
    fun isInBounds(position: GeoPosition): Boolean =
        position.latitude in latitudes.range && position.longitude in longitudes.range
}
