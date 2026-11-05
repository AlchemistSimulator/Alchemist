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
 * A [RasterGrid] storing only the values that are not missing, suited for sparse grids.
 * Each value takes 12 bytes (its index and itself), against the 8 bytes per cell of [ArrayRasterGrid],
 * so it takes less memory when fewer than two thirds of the cells hold a value.
 *
 * @param gridValues the values, row by row: all longitudes of the first latitude, then of the second, and so on.
 * @throws IllegalArgumentException if there is not exactly one value per cell.
 */
class MapRasterGrid(
    override val latitudes: Axis,
    override val longitudes: Axis,
    gridValues: DoubleArray,
) : RasterGrid {

    init {
        requireOneValuePerCell(latitudes, longitudes, gridValues)
    }

    // the row-major indices of the cells holding a value, ascending, so they can be binary searched
    private val indices: IntArray = gridValues.indices.filterNot { gridValues[it].isNaN() }.toIntArray()

    private val values: DoubleArray = DoubleArray(indices.size) { gridValues[indices[it]] }

    /**
     * @throws IndexOutOfBoundsException if [latIndex] or [lonIndex] is outside its axis.
     */
    override fun valueAt(latIndex: Int, lonIndex: Int): Double =
        indices.binarySearch(flatIndexOf(latIndex, lonIndex)).let { if (it >= 0) values[it] else Double.NaN }
}
