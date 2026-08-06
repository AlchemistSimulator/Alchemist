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
import it.unibo.alchemist.model.geospatial.strategy.bracketIndices
import it.unibo.alchemist.model.geospatial.strategy.weight

/**
 * Bilinear interpolation over the 4 cells surrounding the point. If any of the 4 corners is
 * missing, returns [Double.NaN].
 */
class BilinearInterpolation : SpatialInterpolation {
    override fun valueAt(grid: RasterGrid, position: GeoPosition): Double {
        val (lowerLatitudeIndex, upperLatitudeIndex) = bracketIndices(
            grid.latitudes,
            position.latitude,
        )
        val (lowerLongitudeIndex, upperLongitudeIndex) = bracketIndices(
            grid.longitudes,
            position.longitude,
        )

        /**
         * axes are sorted in ascending order, so lower latitude index = south,
         * lower longitude index = west
         */
        val southWestValue = grid.valueAt(lowerLatitudeIndex, lowerLongitudeIndex)
        val southEastValue = grid.valueAt(lowerLatitudeIndex, upperLongitudeIndex)
        val northWestValue = grid.valueAt(upperLatitudeIndex, lowerLongitudeIndex)
        val northEastValue = grid.valueAt(upperLatitudeIndex, upperLongitudeIndex)

        // if any of the four points is missing, returns a missing value
        val anyCornerMissing = southWestValue.isNaN() ||
            southEastValue.isNaN() ||
            northWestValue.isNaN() ||
            northEastValue.isNaN()
        if (anyCornerMissing) return Double.NaN

        val longitudeWeight = weight(
            grid.longitudes,
            lowerLongitudeIndex,
            upperLongitudeIndex,
            position.longitude,
        )
        val latitudeWeight = weight(
            grid.latitudes,
            lowerLatitudeIndex,
            upperLatitudeIndex,
            position.latitude,
        )

        // interpolates along longitude first (one value per latitude row), then along latitude.
        val southValue = southWestValue + (southEastValue - southWestValue) * longitudeWeight
        val northValue = northWestValue + (northEastValue - northWestValue) * longitudeWeight
        return southValue + (northValue - southValue) * latitudeWeight
    }
}
