/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.strategy.spatiotemporal

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.RasterGrid

/**
 * How to compute a value between the nodes of two consecutive [RasterGrid]s.
 */
fun interface SpatioTemporalInterpolation {

    /**
     * The value at [position] between [gridBefore] and [gridAfter], at [timeWeight] from 0 (before) to 1 (after);
     * [Double.NaN] if missing.
     */
    fun interpolate(position: GeoPosition, gridBefore: RasterGrid, gridAfter: RasterGrid, timeWeight: Double): Double
}
