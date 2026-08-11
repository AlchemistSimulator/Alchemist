/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.spatial

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import it.unibo.alchemist.model.geospatial.strategy.StatelessStrategy
import it.unibo.alchemist.model.geospatial.strategy.nearestIndex

/**
 * Value of the nearest cell to the point.
 */
class NearestInterpolation :
    StatelessStrategy(),
    SpatialInterpolation {

    override fun valueAt(grid: RasterGrid, position: GeoPosition): Double {
        val nearestLatitudeIndex = nearestIndex(grid.latitudes, position.latitude)
        val nearestLongitudeIndex = nearestIndex(grid.longitudes, position.longitude)
        return grid.valueAt(nearestLatitudeIndex, nearestLongitudeIndex)
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
