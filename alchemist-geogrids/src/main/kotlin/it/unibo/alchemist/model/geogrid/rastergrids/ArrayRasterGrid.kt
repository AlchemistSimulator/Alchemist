/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.rastergrids

import it.unibo.alchemist.model.geogrid.Axis
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.model.geogrid.util.RasterGrids.flatIndexOf
import it.unibo.alchemist.model.geogrid.util.RasterGrids.requireOneValuePerCell

/**
 * A [RasterGrid] storing a copy of all values, suited for dense grids where most cells hold a value.
 *
 * @param gridValues the values, row by row: all longitudes of the first latitude, then of the second, and so on.
 * @throws IllegalArgumentException if there is not exactly one value per cell.
 */
class ArrayRasterGrid(
    override val latitudes: Axis,
    override val longitudes: Axis,
    gridValues: DoubleArray,
) : RasterGrid {

    private val values: DoubleArray = gridValues.copyOf()

    init {
        requireOneValuePerCell(latitudes, longitudes, values)
    }

    /**
     * @throws IndexOutOfBoundsException if [latIndex] or [lonIndex] is outside its axis.
     */
    override fun valueAt(latIndex: Int, lonIndex: Int): Double = values[flatIndexOf(latIndex, lonIndex)]
}
