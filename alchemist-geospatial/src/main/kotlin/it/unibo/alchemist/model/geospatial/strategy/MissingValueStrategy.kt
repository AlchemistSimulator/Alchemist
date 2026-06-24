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
 * Strategy for **missing values**: what to return when the position is *inside* the grid but the
 * cell (or the cells involved in an interpolation) are fill values ([Double.NaN]). Distinct from
 * [SpatialExtrapolationStrategy] because the cause differs (in-grid missing data, not a position
 * outside the extent of the grid). Built-in implementations in [MissingValue].
 */
fun interface MissingValueStrategy : Serializable {

    /**
     * @param grid the slice on which a missing value was encountered.
     * @param latitude latitude of the point.
     * @param longitude longitude of the point.
     * @return the value to return in place of the missing one.
     */
    fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double
}

/**
 * Built-in missing-value strategies.
 */
enum class MissingValue : MissingValueStrategy {

    /**
     * Propagates [Double.NaN], i.e. the missing data.
     */
    NAN {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double = Double.NaN
    },

    /**
     * Returns `0.0` on missing data.
     */
    ZERO {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double = 0.0
    },
}
