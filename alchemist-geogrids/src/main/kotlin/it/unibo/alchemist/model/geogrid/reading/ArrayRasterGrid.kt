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
 * A [RasterGrid] storing a copy of all values, suited for dense grids where most cells hold a value.
 */
class ArrayRasterGrid(
    latitudes: Axis,
    longitudes: Axis,
    gridValues: DoubleArray,
) : RasterGrid(latitudes, longitudes, gridValues) {

    private val gridValues: DoubleArray = gridValues.copyOf()

    override fun valueAt(latIndex: Int, lonIndex: Int): Double = gridValues[latIndex * longitudes.size + lonIndex]
}
