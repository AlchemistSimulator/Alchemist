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
import it.unibo.alchemist.model.GeoLayer
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geogrid.Axis
import it.unibo.alchemist.model.geogrid.GridSnapshots
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.model.geogrid.SpatialInterpolation
import it.unibo.alchemist.model.geogrid.TemporalInterpolation
import it.unibo.alchemist.model.geogrid.axes.ImmutableAxis
import it.unibo.alchemist.model.geogrid.snapshots.CopernicusData
import it.unibo.alchemist.model.geogrid.snapshots.GridsFromDirectory
import it.unibo.alchemist.model.geogrid.spatialinterpolations.BilinearInterpolation
import it.unibo.alchemist.model.geogrid.temporalinterpolations.LinearInterpolation
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * A [GeoLayer] over gridded geographic data changing over time, such as weather or climate data.
 * At each simulation time, the value at a position is interpolated with [spatialInterpolation] in the two grids
 * around that time, then between them with [temporalInterpolation], and finally converted by [converter].
 * Before the first and after the last instant of the data, the first and last grids are used;
 * positions outside the grid have no value.
 *
 * @param environment the environment, whose simulation provides the time.
 * @param data the data, such as [GridsFromDirectory] or [CopernicusData].
 * @param converter how to turn the interpolated values, [Double.NaN] if missing, into the values of the layer.
 * @param timeScale the real-world duration of a simulation time unit, as an ISO-8601 duration (e.g., `PT6H`).
 * @param timeOrigin the real-world instant of simulation time 0, as an ISO-8601 instant
 * (e.g., `2024-06-10T00:00:00Z`); the first instant of [data] when `null`.
 * @param spatialInterpolation how to compute values between the nodes of a grid.
 * @param temporalInterpolation how to compute values between two instants of the data.
 * @throws IllegalArgumentException if [data] is empty, if [timeScale] is not a positive finite ISO-8601 duration,
 * or if [timeOrigin] is not an ISO-8601 instant.
 */
open class GriddedGeoLayer<T> @JvmOverloads constructor(
    private val environment: Environment<*, GeoPosition>,
    private val data: GridSnapshots,
    private val converter: (Double) -> T,
    timeScale: String = DEFAULT_TIME_SCALE.toIsoString(),
    timeOrigin: String? = null,
    private val spatialInterpolation: SpatialInterpolation = BilinearInterpolation,
    private val temporalInterpolation: TemporalInterpolation = LinearInterpolation,
) : GeoLayer<T> {

    init {
        require(data.instants.isNotEmpty()) { "GridSnapshots is empty." }
    }

    /**
     * The instants of the [data], in simulation time.
     */
    private val sliceTimes: Axis = run {
        val scale = Duration.parseIsoString(timeScale)
        require(scale.isPositive() && scale.isFinite()) { "timeScale must hold a finite positive value." }
        val origin: Instant = timeOrigin?.let(Instant::parse) ?: data.instants.first()
        ImmutableAxis(
            data.instants
                .map { it.toSimulationTime(origin, scale) }
                .toDoubleArray(),
        )
    }

    /**
     * The value at [position], following the time of the simulation, or time 0 until there is one.
     *
     * @throws IllegalArgumentException if [position] is outside the grid.
     */
    override fun valueAt(position: GeoPosition): Observable<T> =
        environment.attachedSimulation.switchMap { simulation ->
            simulation?.time?.map { valueAt(position, it.toDouble()) } ?: observe(valueAt(position, 0.0))
        }

    /**
     * The value at [position] at simulation time [t].
     */
    private fun valueAt(position: GeoPosition, t: Double): T {
        // the indices of the time slices that enclose t: the same one if t falls on a slice or outside them
        val (gridBeforeIndex, gridAfterIndex, timeWeight) = sliceTimes.enclosingCellOf(t)
        val gridBefore = data.grid(gridBeforeIndex)
        val gridAfter = data.grid(gridAfterIndex)
        require(gridBefore.isInBounds(position)) { outOfBoundsMessage(position, gridBefore) }
        val valueBefore = spatialInterpolation.valueAt(gridBefore, position)
        // a single grid is interpolated once
        val valueAfter = if (gridAfterIndex == gridBeforeIndex) {
            valueBefore
        } else {
            spatialInterpolation.valueAt(gridAfter, position)
        }
        return converter(temporalInterpolation.interpolate(valueBefore, valueAfter, timeWeight))
    }

    /**
     * Defaults of the layer.
     */
    companion object {
        /**
         * The default real-world duration of one simulation time unit: one hour.
         */
        val DEFAULT_TIME_SCALE: Duration = 1.hours

        /**
         * This instant in simulation time, starting at [origin] and with [scale] per time unit.
         */
        private fun Instant.toSimulationTime(origin: Instant, scale: Duration): Double = (this - origin) / scale

        /**
         * The error message for a [position] outside [grid].
         */
        private fun outOfBoundsMessage(position: GeoPosition, grid: RasterGrid): String {
            val latitudes = grid.latitudes.range
            val longitudes = grid.longitudes.range
            return "GeoPosition out of bounds: requested (lat: ${position.latitude}, lon: ${position.longitude}) " +
                "but the bounding box is lat: [${latitudes.start}, ${latitudes.endInclusive}], " +
                "lon: [${longitudes.start}, ${longitudes.endInclusive}]"
        }
    }
}
