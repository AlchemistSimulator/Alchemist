/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.reading.GridSnapshots
import it.unibo.alchemist.model.geogrid.strategy.converter.DoubleIdentityWithFallback
import it.unibo.alchemist.model.geogrid.strategy.converter.MeasurementConverter
import it.unibo.alchemist.model.geogrid.strategy.spatiotemporal.SpatioTemporalInterpolation
import it.unibo.alchemist.model.geogrid.strategy.spatiotemporal.TrilinearInterpolation

/**
 * A [GriddedGeoLayer] yielding `Double` values, which by default are the interpolated values as they are
 * (see [DoubleIdentityWithFallback]).
 *
 * @param environment simulation environment. Used only to observe the simulation time.
 * @param data temporal raster series backing this layer, such as [GriddedGeoFiles] or [CopernicusData].
 * @param timeScale real-world duration of one simulation time unit, as an ISO-8601 duration,
 * by default [GriddedGeoLayer.DEFAULT_TIME_SCALE].
 * @param timeOrigin real-world instant mapping to simulation time `0.0`, as an ISO-8601 instant.
 * The first instant found in [data] is used when `null`.
 * @param interpolation strategy for spatio-temporal evaluation, by default [TrilinearInterpolation].
 * @param converter defines how interpolated values are converted, by default [DoubleIdentityWithFallback].
 * @see GriddedGeoLayer
 */
class DoubleGriddedGeoLayer @JvmOverloads constructor(
    environment: Environment<*, GeoPosition>,
    data: GridSnapshots,
    timeScale: String = DEFAULT_TIME_SCALE.toIsoString(),
    timeOrigin: String? = null,
    interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
    converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
) : GriddedGeoLayer<Double>(environment, data, converter, timeScale, timeOrigin, interpolation)
