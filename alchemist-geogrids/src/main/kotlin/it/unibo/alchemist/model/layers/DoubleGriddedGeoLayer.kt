/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.boundary.copernicus.CopernicusData
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.GridSnapshots
import it.unibo.alchemist.model.geogrid.strategy.converter.DoubleIdentityWithFallback
import it.unibo.alchemist.model.geogrid.strategy.converter.MeasurementConverter
import it.unibo.alchemist.model.geogrid.strategy.spatiotemporal.SpatioTemporalInterpolation
import it.unibo.alchemist.model.geogrid.strategy.spatiotemporal.TrilinearInterpolation

/**
 * A [GriddedGeoLayer] of [Double]s.
 *
 * @param environment the environment, whose simulation provides the time.
 * @param data the data, such as [GriddedGeoFiles] or [CopernicusData].
 * @param timeScale the real-world duration of a simulation time unit, as an ISO-8601 duration.
 * @param timeOrigin the real-world instant of simulation time 0, as an ISO-8601 instant;
 * the first instant of [data] when `null`.
 * @param interpolation how to compute values between the nodes and the instants of the data.
 * @param converter how to convert the interpolated values, by default leaving them as they are.
 */
class DoubleGriddedGeoLayer @JvmOverloads constructor(
    environment: Environment<*, GeoPosition>,
    data: GridSnapshots,
    timeScale: String = DEFAULT_TIME_SCALE.toIsoString(),
    timeOrigin: String? = null,
    interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
    converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
) : GriddedGeoLayer<Double>(environment, data, converter, timeScale, timeOrigin, interpolation)
