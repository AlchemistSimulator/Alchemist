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
import it.unibo.alchemist.model.geospatial.reading.GridSnapshots
import it.unibo.alchemist.model.geospatial.strategy.converter.DoubleIdentityWithFallback
import it.unibo.alchemist.model.geospatial.strategy.converter.MeasurementConverter
import it.unibo.alchemist.model.geospatial.strategy.spatiotemporal.SpatioTemporalInterpolation
import it.unibo.alchemist.model.geospatial.strategy.spatiotemporal.TrilinearInterpolation
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * A specialized version of [CopernicusLayer] configured to yield `Double` values.
 *
 * This class mirrors the constructors of its superclass but provides default
 * strategies: it uses [TrilinearInterpolation] for spatial and temporal interpolation,
 * and [DoubleIdentityWithFallback] to convert measurements into `Double`.
 *
 * For detailed descriptions of the common parameters (such as `environment`, `timeScale`,
 * `endpoint`, and configuration files), refer to the documentation of [CopernicusLayer].
 *
 * @see CopernicusLayer
 */
class DoubleCopernicusLayer : CopernicusLayer<Double> {

    constructor(
        environment: Environment<*, GeoPosition>,
        data: GridSnapshots,
        timeScale: Duration = DEFAULT_TIME_SCALE,
        timeOrigin: Instant? = null,
        interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
        converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
    ) : super(
        environment,
        data,
        timeScale,
        timeOrigin,
        interpolation,
        converter,
    )

    @JvmOverloads
    constructor(
        environment: Environment<*, GeoPosition>,
        dataDirectory: String,
        timeScale: String = DEFAULT_TIME_SCALE_ISO,
        timeOrigin: String? = null,
        variable: String? = null,
        interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
        converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
    ) : super(
        environment,
        dataDirectory,
        timeScale,
        timeOrigin,
        variable,
        interpolation,
        converter,
    )

    @JvmOverloads
    constructor(
        environment: Environment<*, GeoPosition>,
        endpoint: String,
        dataset: String,
        inputsFile: String,
        checkMd5: Boolean,
        timeScale: String = DEFAULT_TIME_SCALE_ISO,
        timeOrigin: String? = null,
        variable: String? = null,
        cacheDirectory: String = DEFAULT_CACHE_DIRECTORY,
        cdsApiRcFile: String = DEFAULT_CDSAPIRC_FILE,
        interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
        converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
    ) : super(
        environment,
        endpoint,
        dataset,
        inputsFile,
        checkMd5,
        timeScale,
        timeOrigin,
        variable,
        cacheDirectory,
        cdsApiRcFile,
        interpolation,
        converter,
    )

    private companion object {
        private const val serialVersionUID = 1L
    }
}
