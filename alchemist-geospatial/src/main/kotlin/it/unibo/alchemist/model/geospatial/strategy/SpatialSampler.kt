/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.io.Serializable

/**
 * Orchestrates the three spatial strategies (in-extent interpolation,
 * out-of-extent extrapolation, missing-value handling) and exposes a single "sample a slice at a
 * point" operation.
 *
 * @property interpolation strategy used when the point is inside the grid extent.
 * @property outOfBounds strategy used when the point is outside the grid extent.
 * @property missing strategy used when interpolation generates a missing value.
 */
class SpatialSampler(
    private val interpolation: SpatialInterpolationStrategy,
    private val outOfBounds: SpatialExtrapolationStrategy,
    private val missing: MissingValueStrategy,
) : Serializable {

    /**
     * Samples [grid] at the given position, applying in order: bounds check, interpolation and
     * missing-value handling.
     *
     * **Note**: missing-value substitution is applied **only** on the in-extent path. A
     * value produced by the out-of-extent strategy is returned as is, even if it is [Double.NaN].
     *
     * @param grid the spatial slice to sample.
     * @param latitude latitude of the point.
     * @param longitude longitude of the point.
     * @return the sampled value according to the configured strategies.
     */
    fun sample(grid: RasterGrid, latitude: Double, longitude: Double): Double {
        val isLatitudeInBounds = latitude in grid.latitudes.first()..grid.latitudes.last()
        val isLongitudeInBounds = longitude in grid.longitudes.first()..grid.longitudes.last()

        if (!isLatitudeInBounds || !isLongitudeInBounds) {
            return outOfBounds.valueAt(grid, latitude, longitude)
        }

        val interpolatedValue = interpolation.valueAt(grid, latitude, longitude)
        return if (interpolatedValue.isNaN()) missing.valueAt(grid, latitude, longitude) else interpolatedValue
    }

    private companion object {
        private const val serialVersionUID = 1L
    }
}
