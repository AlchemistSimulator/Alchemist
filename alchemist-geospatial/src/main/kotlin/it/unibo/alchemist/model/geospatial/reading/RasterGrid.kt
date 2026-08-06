/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import java.io.Serializable

/**
 * A single 2D spatial "slice" of data on a regular geographic grid (latitude/longitude).
 *
 * Axis contract: [latitudes] and [longitudes] are ALWAYS sorted in ascending order.
 * Implementations that read files with descending axes (e.g. GloFAS, whose latitude runs
 * from +89.95 to −59.95) must normalize internally, so that strategies never need to
 * reason about axis direction.
 */
interface RasterGrid : Serializable {
    /**
     * Latitudes of grid nodes, in degrees, sorted in ascending order.
     */
    val latitudes: DoubleArray

    /**
     * Longitudes of grid nodes, in degrees, sorted in ascending order.
     */
    val longitudes: DoubleArray

    /**
     * Raw value of the cell at the given index coordinates.
     *
     * @param latIndex index on the [latitudes] axis.
     * @param lonIndex index on the [longitudes] axis.
     *
     * @return the raw value of the cell; a [Double.NaN]
     * denotes a missing/fill value.
     */
    fun valueAt(latIndex: Int, lonIndex: Int): Double
}

/**
 * Validates that the number of provided values matches the size of the grid
 * described by [latitudes] and [longitudes].
 *
 * @throws IllegalArgumentException if [valuesSize] does not equal `latitudes.size * longitudes.size`.
 */
internal fun requireMatchingGridSize(latitudes: DoubleArray, longitudes: DoubleArray, valuesSize: Int) {
    val expectedSize = latitudes.size * longitudes.size
    require(valuesSize == expectedSize) {
        "Dimension mismatch: expected $expectedSize values " +
            "(${latitudes.size} lat x ${longitudes.size} lon), but got $valuesSize"
    }
}
