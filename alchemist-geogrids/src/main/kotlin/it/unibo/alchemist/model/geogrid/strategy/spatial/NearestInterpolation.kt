/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatial

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.RasterGrid
import it.unibo.alchemist.model.geogrid.utils.AxisCell.Companion.enclosingCellOf
import it.unibo.alchemist.model.geogrid.utils.StatelessStrategy

/**
 * Value of the nearest cell to the point.
 */
class NearestInterpolation :
    StatelessStrategy(),
    SpatialInterpolation {

    override fun valueAt(grid: RasterGrid, position: GeoPosition): Double {
        val nearestLatitudeIndex = grid.latitudes.nearestIndex(position.latitude)
        val nearestLongitudeIndex = grid.longitudes.nearestIndex(position.longitude)
        return grid.valueAt(nearestLatitudeIndex, nearestLongitudeIndex)
    }

    internal companion object {
        /**
         * The index of the value of this ascending axis closest to [coordinate].
         * Coordinates outside the axis get the index of its nearest end;
         * ties go to the lower index.
         *
         * @throws IllegalArgumentException if the axis is empty or [coordinate] is NaN.
         */
        internal fun DoubleArray.nearestIndex(coordinate: Double): Int =
            enclosingCellOf(coordinate).run { if (weight <= 0.5) lower else upper }
    }
}
