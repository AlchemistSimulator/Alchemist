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
 * In-memory implementation of [RasterGrid] where values are stored in a single flattened row-major array.
 *
 * Suited for dense grids, where most cells hold a value.
 *
 * @see RasterGrid
 */
class ArrayRasterGrid(latitudes: DoubleArray, longitudes: DoubleArray, gridValues: DoubleArray) :
    RasterGrid(latitudes, longitudes, gridValues) {

    override fun valueAt(latIndex: Int, lonIndex: Int): Double = gridValues[latIndex * longitudes.size + lonIndex]

    private companion object {
        private const val serialVersionUID = 1L
    }
}
