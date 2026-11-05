/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid

import it.unibo.alchemist.model.GeoPosition

/**
 * Values over a latitude/longitude grid, in degrees, with [Double.NaN] for the missing ones.
 *
 * @property latitudes the latitudes of the grid.
 * @property longitudes the longitudes of the grid.
 */
interface RasterGrid {
    val latitudes: Axis
    val longitudes: Axis

    /**
     * The value at the [latIndex]-th latitude and [lonIndex]-th longitude, [Double.NaN] if missing.
     */
    fun valueAt(latIndex: Int, lonIndex: Int): Double

    /**
     * Whether [position] lies within the grid.
     */
    fun isInBounds(position: GeoPosition): Boolean =
        position.latitude in latitudes.range && position.longitude in longitudes.range
}
