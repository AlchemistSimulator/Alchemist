/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatiotemporal

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.RasterGrid
import it.unibo.alchemist.model.geogrid.strategy.spatial.SpatialInterpolation
import it.unibo.alchemist.model.geogrid.strategy.temporal.TemporalInterpolation

/**
 * Interpolates each grid with [spatialStrategy], then the two results with [temporalStrategy],
 * which also decides what to do with missing values.
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
