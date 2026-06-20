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
 * In-memory implementation of [RasterGrid], format-agnostic.
 *
 * Once data has been read from a file (NetCDF, GRIB, or any other source) and loaded here,
 * this class has no dependency on the reading library. Values are stored in a single
 * flattened row-major array, avoiding the overhead and boxing of Array<DoubleArray>.
 *
 * @property latitudes see [RasterGrid.latitudes] (ascending).
 * @property longitudes see [RasterGrid.longitudes] (ascending).
 * @property values Cell values in row-major order; [Double.NaN] indicates a missing value.
 */
class ArrayRasterGrid(
    override val latitudes: DoubleArray,
    override val longitudes: DoubleArray,
    private val values: DoubleArray,
) : RasterGrid {

    override fun valueAt(latIndex: Int, lonIndex: Int): Double = values[latIndex * longitudes.size + lonIndex]
}
