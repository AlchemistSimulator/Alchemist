/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.mockGeoPosition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geospatial.reading.GridSnapshots
import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

private const val TOLERANCE = 1e-9

class TestGriddedGeoLayer {
    // fixed spatial grid
    private val lats = doubleArrayOf(44.0, 45.0, 46.0)
    private val lons = doubleArrayOf(11.0, 12.0, 13.0)

    private val center: GeoPosition = mockGeoPosition(45.0, 12.0)

    private val netCdfFileName = "data.nc"

    /**
     * A [Time] mock at [t].
     */
    private fun timeAt(t: Double): Time = mockk { every { toDouble() } returns t }

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
    private fun envAt(t: Double): Environment<Any?, GeoPosition> = envWith(observe(simulationAt(observe(timeAt(t)))))

    /**
     * Environment mock whose simulation time is [simulationTime].
     */
    private fun mutableEnv(simulationTime: Observable<Time>): Environment<Any?, GeoPosition> =
        envWith(observe(simulationAt(simulationTime)))

    /**
     * Environment mock where the simulation has not started yet.
     */
    private val envNoSim: Environment<Any?, GeoPosition> = envWith(observe(null))

    /**
     * A [RasterGrid] where every cell has a fixed [value].
     */
    private fun flatGrid(value: Double): RasterGrid = ArrayRasterGrid(
        lats,
        lons,
        DoubleArray(lats.size * lons.size) { value },
    )

    /**
     * Returns a [GridSnapshots] made of flat slices.
     * THe i-th slice has all cells equal to `sliceValue[i]`.
     * Time steps are spaced [step] apart starting from [base].
     */
    private fun syntheticGrid(
        vararg sliceValues: Double,
        base: Instant = Instant.EPOCH,
        step: Duration = 1.hours,
    ): GridSnapshots {
        val instants = sliceValues.indices.map { i -> base.plus(step * i) }
        val grids = sliceValues.map { v -> flatGrid(v) }
        return object : GridSnapshots {
            override val instants = instants
            override fun grid(index: Int): RasterGrid = grids[index]
        }
    }

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

    // timeOrigin tests
    @Test
    fun `default timeOrigin maps the first instant to time zero`() {
        val gridSnaps = syntheticGrid(7.0, 14.0)
        val layer = DoubleGriddedGeoLayer(
            environment = envAt(0.0),
            data = gridSnaps,
        )
        gridSnaps.instants.indices.forEach { _ ->
            assertEquals(7.0, layer.currentValueAt(center), "t=0.0 should hit the first slice exactly")
        }
    }

    @Test
    fun `explicit timeOrigin shifts the temporal origin`() {
        val layer = DoubleGriddedGeoLayer(
            envAt(0.0),
            syntheticGrid(10.0, 20.0, 30.0),
            // instants: EPOCH, EPOCH+1h, EPOCH+2h
            timeOrigin = (Instant.EPOCH + 1.hours).toString(),
        )
        assertEquals(20.0, layer.currentValueAt(center), "t=0.0 with shifted origin maps to the second slice = 20.0")
    }

    @Test
    fun `with an explicit timeOrigin, times outside the shifted range extrapolate with the first or last time slice`() {
        // computed simulation times: -1.0, 0.0, 1.0; t=-2.0 and 2 are out of range
        var layer = DoubleGriddedGeoLayer(
            envAt(-2.0),
            syntheticGrid(10.0, 20.0, 30.0),
            timeOrigin = (Instant.EPOCH + 1.hours).toString(),
        )
        assertEquals(
            10.0,
            layer.currentValueAt(center),
            "t=-2.0 < sliceTimes.first()=-1.0, extrapolates to first slice: 10.0",
        )
        layer = DoubleGriddedGeoLayer(
            envAt(2.0),
            syntheticGrid(10.0, 20.0, 30.0),
            timeOrigin = (Instant.EPOCH + 1.hours).toString(),
        )
        assertEquals(
            30.0,
            layer.currentValueAt(center),
            "t=2.0 > sliceTimes.last()=1.0, extrapolates to last slice: 30.0",
        )
    }

    // timescale tests
    @Test
    fun `with timeScale PT30M one real hour maps to time two`() {
        /*
         * instants in the file: EPOCH, EPOCH+1h
         * sliceTimes should be [0.0, 2.0] with scale PT30M
         */
        val layer = DoubleGriddedGeoLayer(
            envAt(1.0),
            syntheticGrid(0.0, 10.0),
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
            syntheticGrid(0.0, 12.0, step = 6.hours),
            timeScale = "PT6H",
        )
        assertEquals(6.0, layer.currentValueAt(center), TOLERANCE, "t=0.5 halfway, LINEAR blending should return 6.0")
    }

    // out of bound test
    @Test
    fun `currentValueAt should raise an exception when an out-of-bounds position is passed`() {
        val gridSnaps = syntheticGrid(7.0)
        val layer = DoubleGriddedGeoLayer(
            environment = envAt(0.0),
            data = gridSnaps,
        )
        assertFailsWith<IllegalArgumentException> {
            layer.currentValueAt(mockGeoPosition(lats.first() - 1, lons.first() - 1))
        }
    }

    // strategies tests
    @Test
    fun `exact hits on slices produce values without temporal interpolation`() {
        val sliceValues = doubleArrayOf(1.0, 2.0, 3.0)
        for (i in sliceValues.indices) {
            val time = i.toDouble()
            val layer = DoubleGriddedGeoLayer(
                envAt(time),
                syntheticGrid(*sliceValues),
            )
            assertEquals(sliceValues[i], layer.currentValueAt(center), "t=$time hits slice $i = ${sliceValues[i]}")
        }
    }

    // no simulation test
    @Test
    fun `no attached simulation falls back to time zero and reads the first slice`() {
        val layer = DoubleGriddedGeoLayer(
            envNoSim,
            syntheticGrid(42.0, 84.0),
        )
        assertEquals(42.0, layer.currentValueAt(center), "null simulation: t=0.0, first slice = 42.0")
    }

    @Test
    fun `the observed value follows the simulation time once the simulation is attached`() {
        val simulation = observe<Simulation<Any?, GeoPosition>?>(null)
        val simulationTime = observe(timeAt(1.0))
        val layer = DoubleGriddedGeoLayer(envWith(simulation), syntheticGrid(42.0, 84.0))
        val observed = mutableListOf<Double>()
        layer.valueAt(center).onChange { observed += it }
        simulation.current = simulationAt(simulationTime)
        simulationTime.current = timeAt(0.5)
        assertEquals(
            listOf(42.0, 84.0, 63.0),
            observed,
            "t=0.0 before attachment, then t=1.0 at attachment, then t=0.5",
        )
    }

    // directory constructor with real NetCDF files tests
    @Test
    fun `directory constructor with a single file hits exactly at time zero with the default timeOrigin`() {
        val dir = tempDir.resolve("single").also { it.toFile().mkdirs() }
        /*
         * slice values:
         * slice 0: all 10.0
         * slice 1: all 20.0
         * slice 2: all 30.0
         */
        writeTestNetcdf(
            path = dir.resolve(netCdfFileName),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0, 1.0, 2.0),
            variables = listOf(TestVariable(rawValues = DoubleArray(27) { idx -> ((idx / 9) + 1) * 10.0 })),
        )
        val layer = DoubleGriddedGeoLayer(envAt(0.0), GriddedGeoFiles(dir.absolutePathString()))
        assertEquals(10.0, layer.currentValueAt(center), TOLERANCE, "t=0.0 -> first slice -> all cells = 10.0")
    }

    @Test
    fun `directory constructor interpolates linearly between two slices in a file`() {
        val dir = tempDir.resolve("interp").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve(netCdfFileName),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0, 1.0),
            variables = listOf(TestVariable(rawValues = DoubleArray(18) { idx -> if (idx < 9) 0.0 else 10.0 })),
        )
        val layer = DoubleGriddedGeoLayer(envAt(0.5), GriddedGeoFiles(dir.absolutePathString()))
        assertEquals(5.0, layer.currentValueAt(center), TOLERANCE, "t=0.5 halfway between 0.0 and 10.0 -> 5.0")
    }

    @Test
    fun `directory constructor normalises a descending latitude axis to ascending`() {
        val dir = tempDir.resolve("desc-lat").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve(netCdfFileName),
            lats = doubleArrayOf(46.0, 45.0, 44.0), // lats descending
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0),
            variables = listOf(
                TestVariable(
                    rawValues = DoubleArray(9) { idx ->
                        when (idx / 3) {
                            0 -> 1.0
                            1 -> 2.0
                            else -> 3.0
                        }
                    },
                ),
            ),
        )
        val layer = DoubleGriddedGeoLayer(envAt(0.0), GriddedGeoFiles(dir.absolutePathString()))
        val lat44: GeoPosition = mockGeoPosition(44.0, 12.0)
        val lat46: GeoPosition = mockGeoPosition(46.0, 12.0)
        assertEquals(
            3.0,
            layer.currentValueAt(lat44),
            TOLERANCE,
            "after normalisation, lat=44 (file-row 2) should return 3.0",
        )
        assertEquals(
            1.0,
            layer.currentValueAt(lat46),
            TOLERANCE,
            "after normalisation, lat=46 (file-row 0) should return 1.0",
        )
    }

    @Test
    fun `directory constructor auto-detects the variable when variableName is null`() {
        val dir = tempDir.resolve("auto-detect").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve(netCdfFileName),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0),
            variables = listOf(
                TestVariable(
                    "wonderfulvariablename",
                    DoubleArray(9) { 7.0 },
                ),
            ),
        )
        val layer = DoubleGriddedGeoLayer(envAt(0.0), GriddedGeoFiles(dir.absolutePathString(), variable = null))
        assertEquals(7.0, layer.currentValueAt(center), TOLERANCE, "auto-detected variable; all cells = 7.0")
    }

    @Test
    fun `directory constructor uses an explicit variable name correctly`() {
        val dir = tempDir.resolve("explicit-var").also { it.toFile().mkdirs() }
        val varName = "temperature"
        writeTestNetcdf(
            path = dir.resolve(netCdfFileName),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0),
            variables = listOf(
                TestVariable(
                    varName,
                    DoubleArray(9) { 42.0 },
                ),
            ),
        )
        val layer = DoubleGriddedGeoLayer(envAt(0.0), GriddedGeoFiles(dir.absolutePathString(), variable = varName))
        assertEquals(42.0, layer.currentValueAt(center), TOLERANCE, "explicit variable '$varName'; all cells = 42.0")
    }

    @Test
    fun `directory constructor merges two different files into a single ordered time series`() {
        val dir = tempDir.resolve("two-files").also { it.toFile().mkdirs() }
        // file 1: t=0h (all values=10.0) and t=1h (all values=20.0)
        writeTestNetcdf(
            path = dir.resolve("part1.nc"),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(0.0, 1.0),
            variables = listOf(
                TestVariable(rawValues = DoubleArray(18) { idx -> if (idx < 9) 10.0 else 20.0 }),
            ),
        )
        // file 2: t=2h (all values=30.0) and t=3h (all values=40.0)
        writeTestNetcdf(
            path = dir.resolve("part2.nc"),
            lats = doubleArrayOf(44.0, 45.0, 46.0),
            lons = doubleArrayOf(11.0, 12.0, 13.0),
            timeHours = doubleArrayOf(2.0, 3.0),
            variables = listOf(
                TestVariable(rawValues = DoubleArray(18) { idx -> if (idx < 9) 30.0 else 40.0 }),
            ),
        )
        val simulationTime = observe(timeAt(0.0))
        val layer = DoubleGriddedGeoLayer(mutableEnv(simulationTime), GriddedGeoFiles(dir.absolutePathString()))
        (0..3).forEach {
            val time = it.toDouble()
            val expectedTime = (it + 1) * 10.0
            simulationTime.current = timeAt(time)

            assertEquals(
                expectedTime,
                layer.currentValueAt(center),
                TOLERANCE,
                "t=$time. Exact hit on merged series = $expectedTime",
            )
        }
        // interpolation across the file boundary: t=1.5 should return the mean with linear interpolation
        simulationTime.current = timeAt(1.5)
        assertEquals(
            25.0,
            layer.currentValueAt(center),
            TOLERANCE,
            "t=1.5 crosses file boundary. LINEAR blend of 20.0 and 30.0 = 25.0",
        )
    }
}

/**
 * Shorthand for EPOCH time.
 */
private val Instant.Companion.EPOCH: Instant
    get() = Instant.fromEpochMilliseconds(0)
