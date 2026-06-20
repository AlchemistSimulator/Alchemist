/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

/**
 * A single 2D spatial "slice" of data on a regular geographic grid (latitude/longitude).
 *
 * This interface represents a data container: it exposes axes and cell access,
 * but contains no interpolation logic (that lives in the spatial strategies).
 *
 * Axis contract: [latitudes] and [longitudes] are ALWAYS sorted in ascending order.
 * Implementations that read files with descending axes (e.g. GloFAS, whose latitude runs
 * from +89.95 to −59.95) must normalize internally, so that strategies never need to
 * reason about axis direction.
 */
interface RasterGrid {
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
     * @param latIndex index on the [latitudes] axis
     * @param lonIndex index on the [longitudes] axis
     *
     * @return the value of the cell, or [Double.NaN] if the cell
     * contains a missing value or a placeholder
     */
    fun valueAt(latIndex: Int, lonIndex: Int): Double
}
