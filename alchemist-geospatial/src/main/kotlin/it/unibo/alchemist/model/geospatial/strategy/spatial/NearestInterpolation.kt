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
import it.unibo.alchemist.model.geospatial.strategy.nearestIndex

/**
 * Value of the nearest cell to the point, generic respect to [T].
 *
 * @param T the type of data to interpolate.
 */
class NearestInterpolation<T> : SpatialInterpolationStrategy<T> {
    override fun valueAt(grid: RasterGrid<T>, position: GeoPosition): T? {
        val nearestLatitudeIndex = nearestIndex(grid.latitudes, position.latitude)
        val nearestLongitudeIndex = nearestIndex(grid.longitudes, position.longitude)
        return grid.valueAt(nearestLatitudeIndex, nearestLongitudeIndex)
    }
}

/**
 * Nearest spatial interpolation for [Double] values.
 */
class NearestDoubleInterpolation(delegate: SpatialInterpolationStrategy<Double> = NearestInterpolation()) :
    SpatialInterpolationStrategy<Double> by delegate
