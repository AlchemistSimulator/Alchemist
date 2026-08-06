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
import it.unibo.alchemist.model.geospatial.strategy.spatial.SpatialInterpolation
import it.unibo.alchemist.model.geospatial.strategy.temporal.TemporalInterpolation

/**
 * A [SpatioTemporalInterpolation] that decouples the interpolation process
 * into two independent steps.
 *
 * This strategy first resolves the spatial dimensions by applying the provided
 * [spatialStrategy] independently to both the `gridBefore` and `gridAfter` slices.
 * Then, it blends the two resulting values across the time dimension using the
 * [temporalStrategy].
 *
 * This approach is modular, allowing any combination of spatial and temporal
 * algorithms.
 *
 * **Warning!** Whether this decoupling preserves an exact mathematical equivalence
 * a "joint" spatio-temporal formula depends on the strategies plugged in.
 *
 * **Note on missing values:** If the [spatialStrategy] evaluates to [Double.NaN]
 * for one or both grids, those `NaN` values are propagated to the [temporalStrategy],
 * which is then strictly responsible for resolving or propagating the missing data.
 *
 * @property spatialStrategy the strategy used to interpolate the target position within a single time slice.
 * @property temporalStrategy the strategy used to blend the spatially interpolated values across time.
 */
class SeparableSpatioTemporalInterpolation(
    private val spatialStrategy: SpatialInterpolation,
    private val temporalStrategy: TemporalInterpolation,
) : SpatioTemporalInterpolation {

    override fun interpolate(
        position: GeoPosition,
        gridBefore: RasterGrid,
        gridAfter: RasterGrid,
        timeWeight: Double,
    ): Double {
        val resolvedBeforeValue = spatialStrategy.valueAt(gridBefore, position)
        val resolvedAfterValue = spatialStrategy.valueAt(gridAfter, position)

        return temporalStrategy.interpolate(
            resolvedBeforeValue,
            resolvedAfterValue,
            timeWeight,
        )
    }
}
