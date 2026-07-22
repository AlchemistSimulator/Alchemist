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
 * In-memory implementation of [RasterGrid].
 * Values are stored in a single flattened row-major array.
 *
 * @property latitudes see [RasterGrid.latitudes] (ascending).
 * @property longitudes see [RasterGrid.longitudes] (ascending).
 * @property values cell values in row-major order; [Double.NaN] indicates a missing value.
 */
class ArrayRasterGrid(
    override val latitudes: DoubleArray,
    override val longitudes: DoubleArray,
    private val values: DoubleArray,
) : RasterGrid<Double> {

    init {
        val expectedSize = latitudes.size * longitudes.size
        require(values.size == expectedSize) {
            "Dimension mismatch: expected $expectedSize values " +
                "(${latitudes.size} lat x ${longitudes.size} lon), but got ${values.size}"
        }
    }

    override fun valueAt(latIndex: Int, lonIndex: Int): Double? =
        values[latIndex * longitudes.size + lonIndex].takeUnless { it.isNaN() }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
