/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.layers

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geogrid.GridSnapshots
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.model.geogrid.axes.ImmutableAxis
import it.unibo.alchemist.model.geogrid.rastergrids.ArrayRasterGrid
import it.unibo.alchemist.model.geogrid.snapshots.GridsFromDirectory
import it.unibo.alchemist.model.geogrid.spatialinterpolations.BilinearInterpolation
import it.unibo.alchemist.model.geogrid.spatialinterpolations.NearestInterpolation
import it.unibo.alchemist.model.geogrid.temporalinterpolations.ClosestInterpolation
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.maps.environments.oSMEnvironment
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import it.unibo.alchemist.test.environmentTestReaction
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private const val TOLERANCE = 1e-9

class TestGriddedGeoLayer {
    private val center: GeoPosition = LatLongPosition(45.0, 12.0)

    /*
     * Default interpolation, bilinear in space and then linear in time, between two 2x2 grids:
     * the second one has the values of the first plus 1000, to tell time apart from space.
     * In the middle of the cell, the bilinear values are the averages: 32.5 before, 1032.5 after.
     */
    private val cellCenter = LatLongPosition(44.5, 11.5)
    private val gridBefore = cornerGrid(0.0, 10.0, 20.0, 100.0)
    private val gridAfter = cornerGrid(1000.0, 1010.0, 1020.0, 1100.0)
    private val gridBeforeWithHole = cornerGrid(Double.NaN, 10.0, 20.0, 100.0)
    private val gridAfterWithHole = cornerGrid(Double.NaN, 1010.0, 1020.0, 1100.0)

    /**
     * Environment mock where the simulation has not started yet.
     */
    private val envNoSim: Environment<Any?, GeoPosition> = envWith(observe(null))

    private val tempDir: Path = createTempDirectory("geo-raster-layer-test")

    // deletes the directory and its files after each test
    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `require fails on empty GridSnapshots`() {
        val emptyGrid = object : GridSnapshots {
            override val instants: List<Instant> = emptyList()
            override fun grid(index: Int): RasterGrid = throw UnsupportedOperationException()
        }
        assertFailsWith<IllegalArgumentException> {
            DoubleGriddedGeoLayer(
                environment = envAt(0.0),
                data = emptyGrid,
            )
        }
    }

    @Test
    fun `by default, values are interpolated bilinearly in space and then linearly in time`() {
        assertEquals(32.5, defaultInterpolationAt(0.0), TOLERANCE)
        assertEquals(532.5, defaultInterpolationAt(0.5), TOLERANCE)
        assertEquals(1032.5, defaultInterpolationAt(1.0), TOLERANCE)
    }

    @Test
    fun `by default, a missing corner makes the value missing`() {
        val value = defaultInterpolationAt(0.5, before = gridBeforeWithHole)
        assertTrue(value.isNaN(), "Expected NaN, but got $value")
    }

    @Test
    fun `a missing corner in a grid that does not contribute is ignored`() {
        assertEquals(32.5, defaultInterpolationAt(0.0, after = gridAfterWithHole), TOLERANCE)
        assertEquals(1032.5, defaultInterpolationAt(1.0, before = gridBeforeWithHole), TOLERANCE)
    }

    @Test
    fun `spatial and temporal interpolations can be chosen independently`() {
        val layer = DoubleGriddedGeoLayer(
            envAt(0.6),
            snapshotsOf(listOf(gridBefore, gridAfter)),
            spatialInterpolation = NearestInterpolation,
            temporalInterpolation = ClosestInterpolation,
        )
        // nearest to the south-west corner, closest to the second grid
        assertEquals(1000.0, layer.currentValueAt(LatLongPosition(44.1, 11.1)))
    }

    @Test
    fun `the converter turns the interpolated values into the values of the layer`() {
        val layer = GriddedGeoLayer(envAt(0.0), syntheticSnapshots(5.0), { value: Double -> "level ${value.toInt()}" })
        assertEquals("level 5", layer.currentValueAt(center))
    }

    @Test
    fun `missing values are NaN by default`() {
        val value = DoubleGriddedGeoLayer(envAt(0.0), syntheticSnapshots(Double.NaN)).currentValueAt(center)
        assertTrue(value.isNaN(), "expected NaN, but got $value")
    }

    @Test
    fun `missing values take the value of missingValue`() {
        val layer = DoubleGriddedGeoLayer(envAt(0.0), syntheticSnapshots(Double.NaN), missingValue = 0.0)
        assertEquals(0.0, layer.currentValueAt(center))
    }

    @Test
    fun `default timeOrigin maps the first instant to time zero`() {
        val gridSnaps = syntheticSnapshots(7.0, 14.0)
        val layer = DoubleGriddedGeoLayer(
            environment = envAt(0.0),
            data = gridSnaps,
        )
        assertEquals(7.0, layer.currentValueAt(center), "t=0.0 should hit the first slice exactly")
    }

    @Test
    fun `explicit timeOrigin shifts the temporal origin`() {
        val layer = shiftedOriginLayerAt(0.0)
        assertEquals(20.0, layer.currentValueAt(center), "t=0.0 with shifted origin maps to the second slice = 20.0")
    }

    @Test
    fun `with an explicit timeOrigin, times before the first slice take the first one`() {
        // computed simulation times: -1.0, 0.0, 1.0
        val layer = shiftedOriginLayerAt(-2.0)
        assertEquals(10.0, layer.currentValueAt(center), "t=-2.0 is before the first slice, at -1.0")
    }

    @Test
    fun `with an explicit timeOrigin, times after the last slice take the last one`() {
        // computed simulation times: -1.0, 0.0, 1.0
        val layer = shiftedOriginLayerAt(2.0)
        assertEquals(30.0, layer.currentValueAt(center), "t=2.0 is after the last slice, at 1.0")
    }

    @Test
    fun `with timeScale PT30M one real hour maps to time two`() {
        /*
         * instants in the file: EPOCH, EPOCH+1h
         * sliceTimes should be [0.0, 2.0] with scale PT30M
         */
        val layer = DoubleGriddedGeoLayer(
            envAt(1.0),
            syntheticSnapshots(0.0, 10.0),
            timeScale = "PT30M",
        )
        assertEquals(
            5.0,
            layer.currentValueAt(center),
            TOLERANCE,
            "t=1.0 is halfway between 0.0 and 2.0, LINEAR blending should return 5.0",
        )
    }

    @Test
    fun `with timeScale PT6H one real six-hour step maps to time one`() {
        /*
         * instants in the file: EPOCH, EPOCH+6h
         * sliceTimes should be [0.0, 1.0] with scale PT6H
         */
        val layer = DoubleGriddedGeoLayer(
            envAt(0.5),
            syntheticSnapshots(0.0, 12.0, step = 6.hours),
            timeScale = "PT6H",
        )
        assertEquals(6.0, layer.currentValueAt(center), TOLERANCE, "t=0.5 halfway, LINEAR blending should return 6.0")
    }

    @Test
    fun `currentValueAt should raise an exception when an out-of-bounds position is passed`() {
        val gridSnaps = syntheticSnapshots(7.0)
        val layer = DoubleGriddedGeoLayer(envAt(0.0), gridSnaps)
        assertFailsWith<IllegalArgumentException> {
            layer.currentValueAt(LatLongPosition(lats.first() - 1, lons.first() - 1))
        }
    }

    @Test
    fun `exact hits on slices produce values without temporal interpolation`() {
        val sliceValues = doubleArrayOf(1.0, 2.0, 3.0)
        for (i in sliceValues.indices) {
            val time = i.toDouble()
            val layer = DoubleGriddedGeoLayer(
                envAt(time),
                syntheticSnapshots(*sliceValues),
            )
            assertEquals(sliceValues[i], layer.currentValueAt(center), "t=$time hits slice $i = ${sliceValues[i]}")
        }
    }

    @Test
    fun `exact hits on slices interpolate the slice once`() {
        val interpolatedGrids = mutableListOf<RasterGrid>()
        val layer = DoubleGriddedGeoLayer(
            envAt(1.0),
            snapshotsOf(listOf(gridBefore, gridAfter)),
            spatialInterpolation = { grid, position ->
                interpolatedGrids += grid
                BilinearInterpolation.valueAt(grid, position)
            },
        )
        assertEquals(1032.5, layer.currentValueAt(cellCenter), TOLERANCE)
        assertEquals(listOf(gridAfter), interpolatedGrids)
    }

    @Test
    fun `no attached simulation falls back to time zero and reads the first slice`() {
        val layer = DoubleGriddedGeoLayer(envNoSim, syntheticSnapshots(42.0, 84.0))
        assertEquals(42.0, layer.currentValueAt(center), "null simulation: t=0.0, first slice = 42.0")
    }

    @Test
    fun `the observed value follows the simulation time once the simulation is attached`() {
        val simulation = observe<Simulation<Any?, GeoPosition>?>(null)
        val simulationTime = observe(DoubleTime(1.0))
        val layer = DoubleGriddedGeoLayer(envWith(simulation), syntheticSnapshots(42.0, 84.0))
        val observed = mutableListOf<Double>()
        layer.valueAt(center).onChange { observed += it }
        simulation.current = simulationAt(simulationTime)
        simulationTime.current = DoubleTime(0.5)
        assertEquals(
            listOf(42.0, 84.0, 63.0),
            observed,
            "t=0.0 before attachment, then t=1.0 at attachment, then t=0.5",
        )
    }

    @Test
    fun `the value follows the time of a running simulation`() {
        val position = LatLongPosition(44.14, 12.24)
        val valuesAt = mutableMapOf<Double, Double>()
        val simulation = simulationOnMap(ProtelisIncarnation()) {
            environment(oSMEnvironment("maps/cesena.pbf")) {
                // slices of 10, 20, and 30, one hour apart
                val riverDischarge = doubleGriddedGeoLayer(GridsFromDirectory("copernicus/glofas-cesena"), "PT1H")
                layer("river-discharge", riverDischarge)
                // with no nodes, this program makes time advance, every half hour of the data
                globalProgram(DiracComb(2.0), environmentTestReaction(DiracComb(2.0)))
                terminator(AfterTime(DoubleTime(2.5)))
                monitor(
                    object : OutputMonitor<Any, GeoPosition> {
                        override fun stepDone(
                            environment: Environment<Any, GeoPosition>,
                            reaction: Reaction<Any>?,
                            time: Time,
                            step: Long,
                        ) {
                            valuesAt[time.toDouble()] = riverDischarge.currentValueAt(position)
                        }
                    },
                )
            }
        }.getDefault<Any, GeoPosition>().runInCurrentThread()
        simulation.error.onSome { throw it }
        // interpolated linearly between the slices
        assertEquals(
            mapOf(0.0 to 10.0, 0.5 to 15.0, 1.0 to 20.0, 1.5 to 25.0, 2.0 to 30.0),
            valuesAt.filterKeys { it <= 2.0 },
        )
    }

    @Test
    fun `data from several files is interpolated across the file boundary`() {
        val dir = tempDir.resolve("two-files").createDirectories()
        // file 1: t=0h (all values=10.0) and t=1h (all values=20.0)
        writeTestNetcdf(
            path = dir.resolve("part1.nc"),
            lats = lats,
            lons = lons,
            timeHours = doubleArrayOf(0.0, 1.0),
            variables = listOf(TestVariable(rawValues = DoubleArray(18) { idx -> if (idx < 9) 10.0 else 20.0 })),
        )
        // file 2: t=2h (all values=30.0) and t=3h (all values=40.0)
        writeTestNetcdf(
            path = dir.resolve("part2.nc"),
            lats = lats,
            lons = lons,
            timeHours = doubleArrayOf(2.0, 3.0),
            variables = listOf(TestVariable(rawValues = DoubleArray(18) { idx -> if (idx < 9) 30.0 else 40.0 })),
        )
        val simulationTime = observe(DoubleTime(0.0))
        val layer = DoubleGriddedGeoLayer(envFollowing(simulationTime), GridsFromDirectory(dir.absolutePathString()))
        (0..3).forEach {
            val time = it.toDouble()
            val expectedValue = (it + 1) * 10.0
            simulationTime.current = DoubleTime(time)
            assertEquals(
                expectedValue,
                layer.currentValueAt(center),
                TOLERANCE,
                "t=$time. Exact hit on merged series = $expectedValue",
            )
        }
        // interpolation across the file boundary: t=1.5 should return the mean with linear interpolation
        simulationTime.current = DoubleTime(1.5)
        assertEquals(
            25.0,
            layer.currentValueAt(center),
            TOLERANCE,
            "t=1.5 crosses file boundary. LINEAR blend of 20.0 and 30.0 = 25.0",
        )
    }

    /**
     * A [Simulation] mock whose time is [simulationTime].
     */
    private fun simulationAt(simulationTime: Observable<Time>): Simulation<Any?, GeoPosition> = mockk {
        every { time } returns simulationTime
    }

    /**
     * Environment mock attached to the simulation observed by [simulation].
     */
    private fun envWith(simulation: Observable<Simulation<Any?, GeoPosition>?>): Environment<Any?, GeoPosition> =
        mockk {
            every { attachedSimulation } returns simulation
        }

    /**
     * Environment mock with the simulation fixed at time [t].
     */
    private fun envAt(t: Double): Environment<Any?, GeoPosition> = envFollowing(observe(DoubleTime(t)))

    /**
     * Environment mock whose simulation follows [simulationTime].
     */
    private fun envFollowing(simulationTime: Observable<Time>): Environment<Any?, GeoPosition> =
        envWith(observe(simulationAt(simulationTime)))

    /**
     * A 2x2 grid over latitudes 44 and 45 and longitudes 11 and 12, with the given [values] row by row.
     */
    private fun cornerGrid(vararg values: Double): RasterGrid = ArrayRasterGrid(
        ImmutableAxis(doubleArrayOf(44.0, 45.0)),
        ImmutableAxis(doubleArrayOf(11.0, 12.0)),
        values,
    )

    /**
     * A layer over [shiftedData] at simulation time [t], with the origin on its second instant.
     */
    private fun shiftedOriginLayerAt(t: Double) =
        DoubleGriddedGeoLayer(envAt(t), shiftedData, timeOrigin = shiftedOrigin)

    private fun defaultInterpolationAt(t: Double, before: RasterGrid = gridBefore, after: RasterGrid = gridAfter) =
        DoubleGriddedGeoLayer(envAt(t), snapshotsOf(listOf(before, after))).currentValueAt(cellCenter)

    private companion object {
        // fixed spatial grid
        val lats = doubleArrayOf(44.0, 45.0, 46.0)
        val lons = doubleArrayOf(11.0, 12.0, 13.0)

        /**
         * Three flat slices, valued 10, 20, and 30, one hour apart from [Instant.EPOCH].
         */
        val shiftedData: GridSnapshots = syntheticSnapshots(10.0, 20.0, 30.0)

        /**
         * The second instant of [shiftedData], so that its simulation times are -1, 0, and 1.
         */
        val shiftedOrigin: String = (Instant.EPOCH + 1.hours).toString()

        /**
         * A [RasterGrid] where every cell has a fixed [value].
         */
        fun flatGrid(value: Double): RasterGrid = ArrayRasterGrid(
            ImmutableAxis(lats),
            ImmutableAxis(lons),
            DoubleArray(lats.size * lons.size) { value },
        )

        /**
         * A [GridSnapshots] made of [grids], spaced [step] apart starting from [base].
         */
        fun snapshotsOf(
            grids: List<RasterGrid>,
            base: Instant = Instant.EPOCH,
            step: Duration = 1.hours,
        ): GridSnapshots = object : GridSnapshots {
            override val instants = grids.indices.map { i -> base + step * i }
            override fun grid(index: Int): RasterGrid = grids[index]
        }

        /**
         * A [GridSnapshots] made of flat slices: the i-th one has all cells equal to `sliceValues[i]`.
         * Time steps are spaced [step] apart starting from [base].
         */
        fun syntheticSnapshots(
            vararg sliceValues: Double,
            base: Instant = Instant.EPOCH,
            step: Duration = 1.hours,
        ): GridSnapshots = snapshotsOf(sliceValues.map(::flatGrid), base, step)

        /**
         * Shorthand for EPOCH time.
         */
        private val Instant.Companion.EPOCH: Instant
            get() = Instant.fromEpochMilliseconds(0)
    }
}
