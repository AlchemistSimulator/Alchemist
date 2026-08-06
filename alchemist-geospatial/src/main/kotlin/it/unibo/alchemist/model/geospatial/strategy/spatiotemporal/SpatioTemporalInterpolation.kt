/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.spatiotemporal

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.io.Serializable

/**
 * Strategy interface for performing combined spatio-temporal interpolation.
 *
 * Implementations estimate a value at a target [GeoPosition] and temporal state by combining
 * spatial grid data from two bounding time slices.
 *
 * Missing or invalid data during calculation should be signaled by returning [Double.NaN].
 */
fun interface SpatioTemporalInterpolation : Serializable {

    /**
     * Interpolates a value at the specified geographic [position] and temporal offset.
     *
     * @param position the geographic target position to sample.
     * @param gridBefore the raster grid slice at or immediately preceding the target time.
     * @param gridAfter the raster grid slice immediately following the target time.
     * @param timeWeight normalized temporal factor in the range `[0.0, 1.0]`, where `0.0`
     * corresponds exactly to [gridBefore] and `1.0` to [gridAfter].
     * @return the interpolated `Double` value, or [Double.NaN] if the interpolation
     * produces a missing/fill value.
     */
    fun interpolate(position: GeoPosition, gridBefore: RasterGrid, gridAfter: RasterGrid, timeWeight: Double): Double
}
