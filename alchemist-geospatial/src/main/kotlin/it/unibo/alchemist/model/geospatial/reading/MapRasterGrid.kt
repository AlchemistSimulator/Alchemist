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
 * are stored, in a map keyed by latitude-longitude index pairs.
 *
 * Suited for sparse grids, where most cells hold a missing value.
 *
 * @property latitudes see [RasterGrid.latitudes] (ascending).
 * @property longitudes see [RasterGrid.longitudes] (ascending).
 * @param values cell values in row-major order; `null` values represent missing/fill values.
 */
class MapRasterGrid<T>(override val latitudes: DoubleArray, override val longitudes: DoubleArray, values: Array<T?>) :
    RasterGrid<T> {

    init {
        requireMatchingGridSize(latitudes, longitudes, values.size)
    }

    /**
     * A map that associates each (latIndex, longIndex) pair with the value, if present.
     */
    private val availableValues: Map<Pair<Int, Int>, T> = buildMap {
        for (latIndex in latitudes.indices) {
            for (lonIndex in longitudes.indices) {
                values[latIndex * longitudes.size + lonIndex]?.let { cellValue ->
                    put(latIndex to lonIndex, cellValue)
                }
            }
        }
    }

    override fun valueAt(latIndex: Int, lonIndex: Int): T? = availableValues[latIndex to lonIndex]

    private companion object {
        private const val serialVersionUID = 1L
    }
}
