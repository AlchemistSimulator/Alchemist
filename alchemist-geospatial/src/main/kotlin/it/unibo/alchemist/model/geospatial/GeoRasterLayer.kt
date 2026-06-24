/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.geospatial.reading.CdmTimedGrid
import it.unibo.alchemist.model.geospatial.reading.TimedGrid
import it.unibo.alchemist.model.geospatial.strategy.MissingValue
import it.unibo.alchemist.model.geospatial.strategy.SpatialExtrapolation
import it.unibo.alchemist.model.geospatial.strategy.SpatialInterpolation
import it.unibo.alchemist.model.geospatial.strategy.SpatialSampler
import it.unibo.alchemist.model.geospatial.strategy.TemporalExtrapolation
import it.unibo.alchemist.model.geospatial.strategy.TemporalExtrapolationStrategy
import it.unibo.alchemist.model.geospatial.strategy.TemporalInterpolation
import it.unibo.alchemist.model.geospatial.strategy.TemporalInterpolationStrategy
import it.unibo.alchemist.model.geospatial.strategy.bracketIndices
import it.unibo.alchemist.model.geospatial.strategy.weight
import java.nio.file.Path
import java.time.Duration
import java.time.Instant

/**
 * A [Layer] that exposes a [Double] raster value for any [GeoPosition] as a function
 * of the current simulation time, backed by temporal NetCDF/GRIB data.
 * On [getValue], the layer knows the perceived simulation time at that moment via
 * [Environment.simulationOrNull], so no reaction is needed to "advance" the layer.
 *
 * When [getValue] is called, the two time slices that enclose the current time are selected,
 * their values are evaluated using [spatial], and they are blended using [temporalInterpolation].
 * If the simulation time falls outside the calculated simulation time range,
 * [temporalExtrapolation] is applied.
 *
 * Once the construction is complete, real-world time [Instant] are converted proportionally to
 * simulation time using [timeOrigin] and [timeScale].
 *
 * Two constructors are currently available (a third one is planned once the
 * `acquisition` subpackage is implemented. SEE THE TODO below):
 * - **Primary** (this): accepts a ready-built [TimedGrid]; intended for unit tests.
 * - **Directory**: accepts a local [Path], builds [CdmTimedGrid] internally (no network I/O).
 *
 * @property environment simulation environment. Used only to read the current time via [Environment.simulationOrNull].
 * @property data temporal raster series backing this layer.
 * @param timeOrigin real-world [Instant] corresponding to simulation `Time.ZERO`. Defaults to the first instant in [data].
 * @param timeScale real-world [Duration] of one simulation time unit. Defaults to one hour.
 * @param spatial composer of the three spatial strategies (in-extent interpolation,
 * out-of-extent extrapolation, missing-value handling). Defaults to `NEAREST / ZERO / NAN`.
 * @param temporalInterpolation strategy for blending adjacent slice values. Defaults to [TemporalInterpolation.LINEAR].
 * @param temporalExtrapolation strategy for values outside the covered time range. Defaults to [TemporalExtrapolation.LAST].
 */
@Suppress("serial")
class GeoRasterLayer(
    private val environment: Environment<*, GeoPosition>,
    private val data: TimedGrid,
    timeOrigin: Instant? = null,
    timeScale: Duration = Duration.ofHours(1),
    private val spatial: SpatialSampler = SpatialSampler(
        SpatialInterpolation.NEAREST,
        SpatialExtrapolation.ZERO,
        MissingValue.NAN,
    ),
    private val temporalInterpolation: TemporalInterpolationStrategy = TemporalInterpolation.LINEAR,
    private val temporalExtrapolation: TemporalExtrapolationStrategy = TemporalExtrapolation.LAST,
) : Layer<Double, GeoPosition> {

    init {
        require(data.instants.isNotEmpty()) { "TimedGrid must have at least one time instant" }
    }

    /**
     * Real-world instant corresponding to simulation `Time.ZERO`.
     * Equals `timeOrigin` if provided, otherwise the first instant in [data].
     */
    private val origin: Instant = timeOrigin ?: data.instants.first()

    /**
     * Conversion of real-world time instants to simulation time (as an array of `Double`).
     * The conversion occurs only during construction and takes into account the `origin`
     * and the `timeScale`.
     */
    private val sliceTimes: DoubleArray = data.instants
        .map { toSimulationTime(it, origin, timeScale) }
        .toDoubleArray()

    /**
     * It reads the simulation time, finds the adjacent time slices using [bracketIndices],
     * samples each of them using [spatial], and blends the result using [temporalInterpolation].
     * It delegates value retrieval to [temporalExtrapolation] if the current simulation time
     * is outside the simulation time range.
     *
     * **Note**: If the simulation has not yet started, the simulation time taken into account is `0.0`.
     *
     * @param position the geographic position to query.
     * @return the interpolated or extrapolated [Double] value.
     */
    override fun getValue(position: GeoPosition): Double {
        val t = environment.simulationOrNull?.time?.toDouble() ?: 0.0

        /*
         * a function that retrieves the value resolved by the SpatialSampler for
         * the grid at a given index.
         */
        val sample = { i: Int ->
            spatial.sample(
                data.grid(i),
                position.latitude,
                position.longitude,
            )
        }

        // the simulation time is outside the calculated simulation time range. Extrapolates the value.
        if (t < sliceTimes.first() || t > sliceTimes.last()) {
            return temporalExtrapolation.valueAt(
                t,
                sliceTimes.toList(),
                sample,
            )
        }

        // the indices of the spatial slices that enclose time t.
        val (sliceIndexBefore, sliceIndexAfter) = bracketIndices(sliceTimes, t)

        // the time t falls exactly on a slice.
        if (sliceIndexBefore == sliceIndexAfter) return sample(sliceIndexBefore)

        /*
         * the time t lies between two distinct slices.
         * Applies the interpolation strategy between the two values measured in the slices.
         */
        val blendWeight = weight(
            sliceTimes,
            sliceIndexBefore,
            sliceIndexAfter,
            t,
        )
        return temporalInterpolation.interpolate(
            sample(sliceIndexBefore),
            sample(sliceIndexAfter),
            blendWeight,
        )
    }

    /**
     * Constructs a [GeoRasterLayer] from a local directory of data files (no network I/O).
     *
     * Builds a [CdmTimedGrid] from [dataDirectory] and delegates to the primary constructor.
     * (Intended for integration tests with pre downloaded static NetCDF files)
     *
     * @param environment simulation environment.
     * @param dataDirectory directory containing one or more homogeneous data files (same variable
     * and spatial grid, disjoint time ranges).
     * @param variable variable name inside the file (e.g. `"dis24"`, the GRIB shortName).
     * If `null`, it is auto-detected as the unique `(time, lat, lon)` variable.
     * @param timeOrigin see primary constructor.
     * @param timeScale see primary constructor.
     * @param spatial see primary constructor.
     * @param temporalInterpolation see primary constructor.
     * @param temporalExtrapolation see primary constructor.
     */
    constructor(
        environment: Environment<*, GeoPosition>,
        dataDirectory: Path,
        variable: String? = null,
        timeOrigin: Instant? = null,
        timeScale: Duration = Duration.ofHours(1),
        spatial: SpatialSampler = SpatialSampler(
            SpatialInterpolation.NEAREST,
            SpatialExtrapolation.ZERO,
            MissingValue.NAN,
        ),
        temporalInterpolation: TemporalInterpolationStrategy = TemporalInterpolation.LINEAR,
        temporalExtrapolation: TemporalExtrapolationStrategy = TemporalExtrapolation.LAST,
    ) : this(
        environment,
        CdmTimedGrid(dataDirectory, variable),
        timeOrigin,
        timeScale,
        spatial,
        temporalInterpolation,
        temporalExtrapolation,
    )

    /*
     * TODO!!! YAML constructor (to add after the acquisition subpackage is implemented).
     * Reminder: it will accept endpoint, dataset, REQUEST!!, credentials and cache parameters.
     * (As strings?)
     */
    private companion object {
        private const val serialVersionUID = 1L
    }
}

/**
 * Converts a real-world [Instant] to a simulation time [Double] using millisecond precision.
 *
 * @param instant timestamp to convert.
 * @param origin the instant that maps to `0.0` in simulation time.
 * @param scale duration of one simulation time unit.
 */
private fun toSimulationTime(instant: Instant, origin: Instant, scale: Duration): Double =
    Duration.between(origin, instant).toMillis().toDouble() / scale.toMillis().toDouble()
