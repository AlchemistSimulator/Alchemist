/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import java.io.Serializable

/**
 * A single 2D spatial bounding box, based on a geographic grid (latitude/longitude).
 *
 * @property latitudes latitudes of grid nodes, in degrees, sorted in **strictly** ascending order.
 * @property longitudes longitudes of grid nodes, in degrees, sorted in **strictly** ascending order.
 * @param gridValues cell values in row-major order respect to [latitudes] x [longitudes];
 * [Double.NaN] values represent missing/fill values.
 * @throws IllegalArgumentException if [latitudes]/[longitudes] are not strictly ascending or if
 * [gridValues]' size does not equal `latitudes.size * longitudes.size`.
 */
abstract class RasterGrid(val latitudes: DoubleArray, val longitudes: DoubleArray, gridValues: DoubleArray) :
    Serializable {

    init {
        require(latitudes.isStrictlyAscending()) {
            "latitudes must be strictly ascending, but got ${latitudes.contentToString()}"
        }
        require(longitudes.isStrictlyAscending()) {
            "longitudes must be strictly ascending, but got ${longitudes.contentToString()}"
        }
        val expectedSize = latitudes.size * longitudes.size
        require(gridValues.size == expectedSize) {
            "Dimension mismatch: expected $expectedSize values " +
                "(${latitudes.size} lat x ${longitudes.size} lon), but got ${gridValues.size}"
        }
    }

    /**
     * Raw value of the cell at the given index coordinates.
     *
     * @param latIndex index on the [latitudes] axis.
     * @param lonIndex index on the [longitudes] axis.
     * @return the raw value of the cell; a [Double.NaN] denotes a missing/fill value.
     */
    abstract fun valueAt(latIndex: Int, lonIndex: Int): Double

    private companion object {
        private const val serialVersionUID = 1L
    }
}

/**
 * @return `true` if this [DoubleArray] is strictly ascending.
 */
private fun DoubleArray.isStrictlyAscending(): Boolean = when {
    this.size <= 1 -> true
    else -> {
        for (i in 0 until this.size - 1) {
            if (this[i] >= this[i + 1]) return false
        }
        return true
    }
}
