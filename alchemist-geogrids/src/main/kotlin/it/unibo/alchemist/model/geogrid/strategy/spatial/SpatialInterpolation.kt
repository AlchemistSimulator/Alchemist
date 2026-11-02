/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatial

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.RasterGrid

/**
 * How to compute the value of a [RasterGrid] between its nodes.
 */
fun interface SpatialInterpolation {

    /**
     * The value of [grid] at [position], which lies within it; [Double.NaN] if missing.
     */
    fun valueAt(grid: RasterGrid, position: GeoPosition): Double
}
