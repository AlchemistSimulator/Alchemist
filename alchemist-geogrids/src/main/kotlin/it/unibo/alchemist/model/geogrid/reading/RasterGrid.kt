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
 * A single 2D spatial bounding box, based on a geographic grid (latitude/longitude).
 *
 * @property latitudes of grid nodes, in degrees.
 * @property longitudes of grid nodes, in degrees.
 * @param gridValues cell values in row-major order respect to [latitudes] x [longitudes];
 * [Double.NaN] values represent missing/fill values.
 * @throws IllegalArgumentException if [gridValues]' size does not equal `latitudes.size * longitudes.size`.
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
     * Raw value of the cell at the given index coordinates.
     *
     * @param latIndex index on the [latitudes] axis.
     * @param lonIndex index on the [longitudes] axis.
     * @return the raw value of the cell; a [Double.NaN] denotes a missing/fill value.
     */
    abstract fun valueAt(latIndex: Int, lonIndex: Int): Double

    /**
     * Returns whether the geographic location falls within this grid or not.
     *
     * @param position the geographical position to check.
     * @return `true` if [position] is within the spatial extent of this grid, `false` otherwise.
     */
    fun isInBounds(position: GeoPosition): Boolean =
        position.latitude in latitudes.range && position.longitude in longitudes.range
}
