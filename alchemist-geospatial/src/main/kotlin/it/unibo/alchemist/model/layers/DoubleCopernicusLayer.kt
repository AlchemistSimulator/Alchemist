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
 * A [CopernicusLayer] producing plain [Double] values.
 *
 * It mirrors the three constructors of [CopernicusLayer], supplying [TrilinearInterpolation] and
 * [DoubleIdentityWithFallback] as defaults, so a minimal YAML needs no strategy at all.
 *
 * @see CopernicusLayer
 */
class DoubleCopernicusLayer : CopernicusLayer<Double> {

    /**
     * Builds the layer on a ready-made [GridSnapshots]. It is the
     * injection point for tests.
     *
     * @param environment simulation environment.
     * @param data temporal raster series backing this layer.
     * @param timeScale real-world [Duration] of one simulation time unit.
     * @param timeOrigin real-world [Instant] mapping to simulation time `0.0`; the first instant in
     * [data] is used when `null`.
     * @param interpolation strategy for spatio-temporal evaluation.
     * @param converter maps the interpolated value (possibly [Double.NaN]) to a [Double].
     */
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

    /**
     * Builds the layer on a **local directory** of already-available data files: no network, no
     * cache, no credentials.
     *
     * @param environment simulation environment.
     * @param dataDirectory path of a directory holding one or more homogeneous data files. A
     * leading `~` is expanded to the user home.
     * @param variable variable name inside the file (e.g. `"dis24"`); auto-detected when `null`.
     * @param timeScale real-world duration of one simulation time unit, as an ISO-8601 duration.
     * @param timeOrigin real-world instant mapping to simulation time `0.0`, as an ISO-8601
     * instant; the first instant found in the data is used when `null`.
     * @param interpolation strategy for spatio-temporal evaluation.
     * @param converter maps the interpolated value (possibly [Double.NaN]) to a [Double].
     */
    @JvmOverloads
    constructor(
        environment: Environment<*, GeoPosition>,
        dataDirectory: String,
        variable: String? = null,
        timeScale: String = DEFAULT_TIME_SCALE_ISO,
        timeOrigin: String? = null,
        interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
        converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
    ) : super(
        environment,
        dataDirectory,
        variable,
        timeScale,
        timeOrigin,
        interpolation,
        converter,
    )

    /**
     * Builds the layer on data fetched from a Copernicus-family datastore and kept in a local
     * cache.
     *
     * @param environment simulation environment.
     * @param endpoint base URL of the datastore (e.g. `"https://ewds.climate.copernicus.eu/api"`).
     * @param dataset dataset identifier (e.g. `"cems-glofas-historical"`).
     * @param inputs opaque request map for [dataset], passed verbatim to the datastore.
     * @param variable variable name inside the downloaded file; auto-detected when `null`.
     * @param timeScale real-world duration of one simulation time unit, as an ISO-8601 duration.
     * @param timeOrigin real-world instant mapping to simulation time `0.0`, as an ISO-8601
     * instant; the first instant found in the data is used when `null`.
     * @param cacheDirectory root of the local cache. A leading `~` is expanded to the user home.
     * @param cdsApiRcFile path of a `.cdsapirc`-formatted file holding the API token. A leading `~`
     * is expanded to the user home.
     * @param interpolation strategy for spatio-temporal evaluation.
     * @param converter maps the interpolated value (possibly [Double.NaN]) to a [Double].
     */
    @JvmOverloads
    constructor(
        environment: Environment<*, GeoPosition>,
        endpoint: String,
        dataset: String,
        inputs: Map<String, Any>,
        variable: String? = null,
        timeScale: String = DEFAULT_TIME_SCALE_ISO,
        timeOrigin: String? = null,
        cacheDirectory: String = DEFAULT_CACHE_DIRECTORY,
        cdsApiRcFile: String = DEFAULT_CDSAPIRC_FILE,
        interpolation: SpatioTemporalInterpolation = TrilinearInterpolation(),
        converter: MeasurementConverter<Double> = DoubleIdentityWithFallback(),
    ) : super(
        environment,
        endpoint,
        dataset,
        inputs,
        variable,
        timeScale,
        timeOrigin,
        cacheDirectory,
        cdsApiRcFile,
        interpolation,
        converter,
    )

    private companion object {
        private const val serialVersionUID = 1L
    }
}
