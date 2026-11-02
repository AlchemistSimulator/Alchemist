/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.layers

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.GridSnapshots
import it.unibo.alchemist.model.geogrid.SpatialInterpolation
import it.unibo.alchemist.model.geogrid.TemporalInterpolation
import it.unibo.alchemist.model.geogrid.snapshots.CopernicusData
import it.unibo.alchemist.model.geogrid.snapshots.GridsFromDirectory
import it.unibo.alchemist.model.geogrid.spatialinterpolations.BilinearInterpolation
import it.unibo.alchemist.model.geogrid.temporalinterpolations.LinearInterpolation

/**
 * A [GriddedGeoLayer] of [Double]s.
 *
 * @param environment the environment, whose simulation provides the time.
 * @param data the data, such as [GridsFromDirectory] or [CopernicusData].
 * @param timeScale the real-world duration of a simulation time unit, as an ISO-8601 duration.
 * @param timeOrigin the real-world instant of simulation time 0, as an ISO-8601 instant;
 * the first instant of [data] when `null`.
 * @param spatialInterpolation how to compute values between the nodes of a grid.
 * @param temporalInterpolation how to compute values between two instants of the data.
 * @param missingValue the value where the data is missing.
 */
class DoubleGriddedGeoLayer @JvmOverloads constructor(
    environment: Environment<*, GeoPosition>,
    data: GridSnapshots,
    timeScale: String = DEFAULT_TIME_SCALE.toIsoString(),
    timeOrigin: String? = null,
    spatialInterpolation: SpatialInterpolation = BilinearInterpolation(),
    temporalInterpolation: TemporalInterpolation = LinearInterpolation(),
    missingValue: Double = Double.NaN,
) : GriddedGeoLayer<Double>(
    environment,
    data,
    { if (it.isNaN()) missingValue else it },
    timeScale,
    timeOrigin,
    spatialInterpolation,
    temporalInterpolation,
)
