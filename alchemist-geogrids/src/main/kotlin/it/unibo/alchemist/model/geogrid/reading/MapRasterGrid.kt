/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

/**
 * A [RasterGrid] storing only the values that are not missing, suited for sparse grids.
 */
class MapRasterGrid(latitudes: Axis, longitudes: Axis, gridValues: DoubleArray) :
    RasterGrid(latitudes, longitudes, gridValues) {

    private val availableValues: Map<Pair<Int, Int>, Double> = buildMap {
        for (latIndex in 0 until latitudes.size) {
            for (lonIndex in 0 until longitudes.size) {
                val cellValue = gridValues[latIndex * longitudes.size + lonIndex]
                if (!cellValue.isNaN()) put(latIndex to lonIndex, cellValue)
            }
        }
    }

    override fun valueAt(latIndex: Int, lonIndex: Int): Double =
        availableValues.getOrDefault(latIndex to lonIndex, Double.NaN)
}
