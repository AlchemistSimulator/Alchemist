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

/**
 * Strategy for **spatial interpolation**: given a grid slice and a position assumed to be *inside*
 * its extent, produces a value by combining nearby cells. It is a functional interface, so a custom rule
 * can be passed as a lambda. The built-in implementations live in [SpatialInterpolation].
 *
 * Bounds checking and out-of-extent handling are NOT this strategy's concern: they belong to
 * [SpatialExtrapolationStrategy] instead when the point is outside.
 */
fun interface SpatialInterpolationStrategy {

    /**
     * @param grid the slice to sample.
     * @param latitude latitude of the point, assumed to be inside of [grid].
     * @param longitude longitude of the point, assumed to be inside of [grid].
     * @return the interpolated value, or [Double.NaN] if the involved cells are missing.
     */
    fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double
}

/**
 * Built-in spatial interpolation strategies.
 */
enum class SpatialInterpolation : SpatialInterpolationStrategy {

    /**
     * Value of the nearest cell to the point.
     */
    NEAREST {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double {
            val nearestLatitudeIndex = nearestIndex(grid.latitudes, latitude)
            val nearestLongitudeIndex = nearestIndex(grid.longitudes, longitude)
            return grid.valueAt(nearestLatitudeIndex, nearestLongitudeIndex)
        }
    },

    /**
     * Bilinear interpolation over the 4 cells surrounding the point. If any of the 4 corners is
     * missing ([Double.NaN]), returns [Double.NaN] rather than blending in a fictitious value.
     * Interpolation is performed by applying these calculations:
     * [Bilinear Interpolation Wikipedia](https://en.wikipedia.org/wiki/Bilinear_interpolation)
     */
    BILINEAR {
        override fun valueAt(grid: RasterGrid, latitude: Double, longitude: Double): Double {
            val (lowerLatitudeIndex, upperLatitudeIndex) = bracketIndices(grid.latitudes, latitude)
            val (lowerLongitudeIndex, upperLongitudeIndex) = bracketIndices(grid.longitudes, longitude)

            /**
             * axes are sorted in ascending order, so lower latiduce index = south,
             * lower longitude index = west
             */
            val southWestValue = grid.valueAt(lowerLatitudeIndex, lowerLongitudeIndex)
            val southEastValue = grid.valueAt(lowerLatitudeIndex, upperLongitudeIndex)
            val northWestValue = grid.valueAt(upperLatitudeIndex, lowerLongitudeIndex)
            val northEastValue = grid.valueAt(upperLatitudeIndex, upperLongitudeIndex)

            val anyCornerMissing = southWestValue.isNaN() || southEastValue.isNaN() ||
                northWestValue.isNaN() || northEastValue.isNaN()
            if (anyCornerMissing) return Double.NaN

            val longitudeWeight = weight(
                grid.longitudes,
                lowerLongitudeIndex,
                upperLongitudeIndex,
                longitude,
            )
            val latitudeWeight = weight(
                grid.latitudes,
                lowerLatitudeIndex,
                upperLatitudeIndex,
                latitude,
            )

            // interpolates along longitude first (one value per latitude row), then along latitude.
            val southValue = southWestValue + (southEastValue - southWestValue) * longitudeWeight
            val northValue = northWestValue + (northEastValue - northWestValue) * longitudeWeight
            return southValue + (northValue - southValue) * latitudeWeight
        }
    },
}
