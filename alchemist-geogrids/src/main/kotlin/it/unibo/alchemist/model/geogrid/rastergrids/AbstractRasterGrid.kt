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

/**
 * Values over a latitude/longitude grid, in degrees, with [Double.NaN] for the missing ones.
 *
 * @property latitudes the latitudes of the grid.
 * @property longitudes the longitudes of the grid.
 * @param gridValues the values, row by row: all longitudes of the first latitude, then of the second, and so on.
 * @throws IllegalArgumentException if there is not exactly one value per cell.
 */
abstract class AbstractRasterGrid(
    override val latitudes: Axis,
    override val longitudes: Axis,
    gridValues: DoubleArray,
) : RasterGrid {

    private val values = gridValues.copyOf()

    init {
        val expectedSize = latitudes.size * longitudes.size
        require(gridValues.size == expectedSize) {
            "Dimension mismatch: expected $expectedSize values " +
                "(${latitudes.size} lat x ${longitudes.size} lon), but got ${gridValues.size}"
        }
    }

    protected fun valueAtIndex(index: Int) = values[index]
}
