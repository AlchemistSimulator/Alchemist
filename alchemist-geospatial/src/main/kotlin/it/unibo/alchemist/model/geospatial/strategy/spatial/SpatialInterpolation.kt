/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.spatial

import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.io.Serializable

/**
 * Strategy for **spatial interpolation**: given a [RasterGrid] and a position assumed to be *inside*
 * its extent, produces a value by combining nearby cells. It is a functional interface, so a custom rule
 * can be passed as a lambda.
 */
fun interface SpatialInterpolation : Serializable {

    /**
     * @param grid the slice to sample.
     * @param position the requested geographical position, assumed to be inside of [grid].
     * @return the interpolated value, or [Double.NaN] if the interpolation yields a missing value.
     */
    fun valueAt(grid: RasterGrid, position: GeoPosition): Double
}
