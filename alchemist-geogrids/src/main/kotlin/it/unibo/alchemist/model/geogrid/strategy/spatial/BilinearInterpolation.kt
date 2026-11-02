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
import it.unibo.alchemist.model.geogrid.utils.StatelessStrategy

/**
 * Bilinear interpolation of the four nodes around the position, missing if any of them is.
 */
class BilinearInterpolation :
    StatelessStrategy(),
    SpatialInterpolation {

    override fun valueAt(grid: RasterGrid, position: GeoPosition): Double {
        val latitude = grid.latitudes.enclosingCellOf(position.latitude)
        val longitude = grid.longitudes.enclosingCellOf(position.longitude)
        /*
         * axes are sorted in ascending order, so lower latitude index = south,
         * lower longitude index = west.
         */
        val southWestValue = grid.valueAt(latitude.lower, longitude.lower)
        val southEastValue = grid.valueAt(latitude.lower, longitude.upper)
        val northWestValue = grid.valueAt(latitude.upper, longitude.lower)
        val northEastValue = grid.valueAt(latitude.upper, longitude.upper)
        // if any of the four points is missing, returns a missing value
        val anyCornerMissing = southWestValue.isNaN() ||
            southEastValue.isNaN() ||
            northWestValue.isNaN() ||
            northEastValue.isNaN()
        if (anyCornerMissing) {
            return Double.NaN
        }
        // interpolates along longitude first (one value per latitude row), then along latitude.
        val southValue = southWestValue + (southEastValue - southWestValue) * longitude.weight
        val northValue = northWestValue + (northEastValue - northWestValue) * longitude.weight
        return southValue + (northValue - southValue) * latitude.weight
    }
}
