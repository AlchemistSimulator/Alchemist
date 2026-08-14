/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

/**
 * In-memory implementation of [RasterGrid] where only non-missing values
 * are stored in a map keyed by latitude-longitude index pairs.
 *
 * Suited for sparse grids, where most cells hold a missing value.
 *
 * @see RasterGrid
 */
class MapRasterGrid(latitudes: DoubleArray, longitudes: DoubleArray, gridValues: DoubleArray) :
    RasterGrid(latitudes, longitudes, gridValues) {

    /**
     * A map that associates each (latIndex, longIndex) pair with the value, if present.
     */
    private val availableValues: Map<Pair<Int, Int>, Double> = buildMap {
        for (latIndex in latitudes.indices) {
            for (lonIndex in longitudes.indices) {
                val cellValue = gridValues[latIndex * longitudes.size + lonIndex]
                if (!cellValue.isNaN()) put(latIndex to lonIndex, cellValue)
            }
        }
    }

    override fun valueAt(latIndex: Int, lonIndex: Int): Double =
        availableValues.getOrDefault(latIndex to lonIndex, Double.NaN)

    private companion object {
        private const val serialVersionUID = 1L
    }
}
