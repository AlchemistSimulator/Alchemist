/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.spatialinterpolations

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.Axis
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.model.geogrid.SpatialInterpolation

/**
 * The value of the node nearest to the position.
 */
data object NearestInterpolation : SpatialInterpolation {

    override fun valueAt(grid: RasterGrid, position: GeoPosition): Double {
        fun Axis.nearestIndex(coordinate: Double): Int =
            enclosingCellOf(coordinate).run { if (weight <= 0.5) lower else upper }
        val nearestLatitudeIndex = grid.latitudes.nearestIndex(position.latitude)
        val nearestLongitudeIndex = grid.longitudes.nearestIndex(position.longitude)
        return grid.valueAt(nearestLatitudeIndex, nearestLongitudeIndex)
    }
}
