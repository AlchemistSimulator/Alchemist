/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.io.Serializable

/**
 * Strategy for **spatial extrapolation**: what to return when the requested position is *outside*
 * the geographic extent of the grid (the data is entirely absent there, as opposed to a missing
 * in-grid cell, which is a [MissingValueStrategy] concern). Built-in implementations in
 * [SpatialExtrapolation].
 */
fun interface SpatialExtrapolationStrategy : Serializable {

    /**
     * @param grid the slice from which the point stands out.
     * @param latitude latitude of the point, outside the bounds of [grid].
     * @param longitude longitude of the point, outside the bounds of [grid].
     * @return the value to return outside the extent.
     */
    fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double
}

/**
 * Built-in spatial extrapolation strategies.
 */
enum class SpatialExtrapolation : SpatialExtrapolationStrategy {

    /**
     * Always returns `0.0` outside the grid.
     */
    ZERO {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double = 0.0
    },

    /**
     * Always returns `Double.NaN` outside the grid.
     */
    NAN {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double = Double.NaN
    },

    /**
     * Clamps the point to the nearest edge cell and returns its value.
     *
     * **Note:** the returned value is the raw edge cell, which may itself be [Double.NaN] if that edge
     * cell is a fill value. [SpatialSampler] applies the [MissingValueStrategy] only on the in-extent
     * path, so a NaN produced here is *not* substituted: it is returned as is.
     */
    NEAREST_EDGE {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double {
            // nearestIndex clamps an out-of-bounds coordinate to the boundary index, i.e. the edge cell.
            val edgeLatitudeIndex = nearestIndex(grid.latitudes, latitude)
            val edgeLongitudeIndex = nearestIndex(grid.longitudes, longitude)
            return grid.valueAt(edgeLatitudeIndex, edgeLongitudeIndex)
        }
    },
}
