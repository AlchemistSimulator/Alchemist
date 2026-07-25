/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.geospatial.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geospatial.reading.GridSnapshots
import it.unibo.alchemist.model.geospatial.reading.RasterGrid
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.io.path.createTempDirectory

private const val TOLERANCE = 1e-9

class TestCopernicusLayer : StringSpec({
    // fixed spatial grid
    val lats = doubleArrayOf(44.0, 45.0, 46.0)
    val lons = doubleArrayOf(11.0, 12.0, 13.0)

    val center: GeoPosition = mockk {
        every { latitude } returns 45.0
        every { longitude } returns 12.0
    }

    /**
     * Environment mock with the simulation fixed at time [t]
     */
    fun envAt(t: Double): Environment<Any?, GeoPosition> = mockk {
        every { simulationOrNull } returns mockk {
            every { time } returns mockk { every { toDouble() } returns t }
        }
    }

    /**
     * Environment mock whose simulation time is read from [currentT].
     */
    fun mutableEnv(currentT: () -> Double): Environment<Any?, GeoPosition> = mockk {
        every { simulationOrNull } returns mockk {
            every { time } returns mockk { every { toDouble() } answers { currentT() } }
        }
    }

    /**
     * Environment mock where the simulation has not started yet.
     */
    val envNoSim: Environment<Any?, GeoPosition> = mockk {
        every { simulationOrNull } returns null
    }

    /**
     * A [RasterGrid] where every cell has a fixed [value].
     */
    fun flatGrid(value: Double): RasterGrid<Double> = ArrayRasterGrid(
        lats,
        lons,
        Array(lats.size * lons.size) { value },
    )

    /**
     * Returns a [GridSnapshots] made of flat slices.
     * THe i-th slice has all cells equal to `sliceValue[i]`.
     * Time steps are spaced [step] apart starting from [base].
     */
    fun syntheticGrid(
        vararg sliceValues: Double,
        base: Instant = Instant.EPOCH,
        step: Duration = Duration.ofHours(1),
    ): GridSnapshots<Double> {
        val instants = sliceValues.indices.map { i ->
            base.plus(step.multipliedBy(i.toLong()))
        }
        val grids = sliceValues.map { v -> flatGrid(v) }
        return object : GridSnapshots<Double> {
            override val instants = instants
            override fun grid(index: Int): RasterGrid<Double> = grids[index]
        }
    }

    val tempDir: Path = createTempDirectory("geo-raster-layer-test")

    // deletes the directory and its files after the tests
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    "require fails on empty TimedGrid" {
        val emptyGrid = object : GridSnapshots<Double> {
            override val instants: List<Instant> = emptyList()
            override fun grid(index: Int): RasterGrid<Double> = throw UnsupportedOperationException()
        }
        shouldThrow<IllegalArgumentException> {
            DoubleCopernicusLayer(
                environment = envAt(0.0),
                data = emptyGrid,
            )
        }
    }

    // timeOrigin tests
    "default timeOrigin maps the first instant to t=0.0" {
        val timedGrid = syntheticGrid(7.0, 14.0)
        val layer = DoubleCopernicusLayer(
            environment = envAt(0.0),
            data = timedGrid,
        )
        withClue("t=0.0 should hit the first slice exactly") {
            for (i in timedGrid.instants.indices) {
                layer.getValue(center) shouldBe 7.0
            }
        }
    }

    "explicit timeOrigin shifts the temporal origin" {
        val layer = DoubleCopernicusLayer(
            envAt(0.0),
            syntheticGrid(10.0, 20.0, 30.0),
            // instants: EPOCH, EPOCH+1h, EPOCH+2h
            timeOrigin = Instant.EPOCH.plus(Duration.ofHours(1)),
        )
        withClue("t=0.0 with shifted origin maps to the second slice = 20.0") {
            layer.getValue(center) shouldBe 20.0
        }
    }

    "explicit timeOrigin: t before shifted range extrapolates with the first or last time slice" {
        // computed simulation times: -1.0, 0.0, 1.0; t=-2.0 and 2 are out of range
        var layer = DoubleCopernicusLayer(
            envAt(-2.0),
            syntheticGrid(10.0, 20.0, 30.0),
            timeOrigin = Instant.EPOCH.plus(Duration.ofHours(1)),
        )
        withClue("t=-2.0 < sliceTimes.first()=-1.0, extrapolates to first slice: 10.0") {
            layer.getValue(center) shouldBe 10.0
        }

        layer = DoubleCopernicusLayer(
            envAt(2.0),
            syntheticGrid(10.0, 20.0, 30.0),
            timeOrigin = Instant.EPOCH.plus(Duration.ofHours(1)),
        )
        withClue("t=2.0 > sliceTimes.last()=1.0, extrapolates to last slice: 30.0") {
            layer.getValue(center) shouldBe 30.0
        }
    }

    // timescale tests
    "timeScale PT30M: one real hour maps to t=2.0" {
        /*
         * instants in the file: EPOCH, EPOCH+1h
         * sliceTimes should be [0.0, 2.0] with scale PT30M
         */
        val layer = DoubleCopernicusLayer(
            envAt(1.0),
            syntheticGrid(0.0, 10.0),
            timeScale = Duration.ofMinutes(30),
        )
        withClue("t=1.0 is halfway between 0.0 and 2.0, LINEAR blending should return 5.0") {
            layer.getValue(center) shouldBe (5.0 plusOrMinus TOLERANCE)
        }
    }

    "timeScale PT6H: one real six-hour step maps to t=1.0" {
        /*
         * instants in the file: EPOCH, EPOCH+6h
         * sliceTimes should be [0.0, 1.0] with scale PT6H
         */
        val layer = DoubleCopernicusLayer(
            envAt(0.5),
            syntheticGrid(0.0, 12.0, step = Duration.ofHours(6)),
            timeScale = Duration.ofHours(6),
        )
        withClue("t=0.5 halfway, LINEAR blending should return 6.0") {
            layer.getValue(center) shouldBe (6.0 plusOrMinus TOLERANCE)
        }
    }

    // out of bound test
    "getValue() should raise an exception when an out-of-bounds position is passed" {
        val timedGrid = syntheticGrid(7.0)
        val layer = DoubleCopernicusLayer(
            environment = envAt(0.0),
            data = timedGrid,
        )
        shouldThrow<IllegalArgumentException> {
            layer.getValue(
                mockk {
                    every { latitude } returns (lats.first() - 1)
                    every { longitude } returns (lons.first() - 1)
                },
            )
        }
    }

    // strategies tests
    "exact hits on slices produce values without temporal interpolation" {
        val sliceValues = doubleArrayOf(1.0, 2.0, 3.0)
        for (i in sliceValues.indices) {
            val time = i.toDouble()
            val layer = DoubleCopernicusLayer(
                envAt(time),
                syntheticGrid(*sliceValues),
            )
            withClue("t=$time hits slice $i = ${sliceValues[i]}") {
                layer.getValue(center) shouldBe sliceValues[i]
            }
        }
    }

    // no simulation test
    "simulationOrNull null falls back to t=0.0 and reads the first slice" {
        val layer = DoubleCopernicusLayer(
            envNoSim,
            syntheticGrid(42.0, 84.0),
        )
        withClue("null simulation: t=0.0, first slice = 42.0") {
            layer.getValue(center) shouldBe 42.0
        }
    }

    // directory constructor with real NetCDF files tests
    "directory constructor: single file, exact hit at t=0 with default timeOrigin" {
        val dir = tempDir.resolve("single").also { it.toFile().mkdirs() }
        /*
         * slice values:
         * slice 0: all 10.0
         * slice 1: all 20.0
         * slice 2: all 30.0
         */
        writeTestNetcdf(
            path = dir.resolve("data.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0, 1.0, 2.0),
            rawValues = FloatArray(27) { idx -> ((idx / 9) + 1) * 10f },
        )

        val layer = DoubleCopernicusLayer(envAt(0.0), dir)
        withClue("t=0.0 -> first slice -> all cells = 10.0") {
            layer.getValue(center) shouldBe (10.0 plusOrMinus TOLERANCE)
        }
    }

    "directory constructor: linear interpolation between two slices in file" {
        val dir = tempDir.resolve("interp").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve("data.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0, 1.0),
            rawValues = FloatArray(18) { idx -> if (idx < 9) 0f else 10f },
        )

        val layer = DoubleCopernicusLayer(envAt(0.5), dir)
        withClue("t=0.5 halfway between 0.0 and 10.0 -> 5.0") {
            layer.getValue(center) shouldBe (5.0 plusOrMinus TOLERANCE)
        }
    }

    "directory constructor: descending latitude axis is normalised to ascending" {
        val dir = tempDir.resolve("desc-lat").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve("data.nc"),
            lats = floatArrayOf(46f, 45f, 44f), // lats descending
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0),
            rawValues = FloatArray(9) { idx ->
                when (idx / 3) {
                    0 -> 1f
                    1 -> 2f
                    else -> 3f
                }
            },
        )

        val layer = DoubleCopernicusLayer(envAt(0.0), dir)
        val lat44: GeoPosition = mockk {
            every { latitude } returns 44.0
            every { longitude } returns 12.0
        }
        val lat46: GeoPosition = mockk {
            every { latitude } returns 46.0
            every { longitude } returns 12.0
        }

        withClue("after normalisation, lat=44 (file-row 2) should return 3.0") {
            layer.getValue(lat44) shouldBe (3.0 plusOrMinus TOLERANCE)
        }
        withClue("after normalisation, lat=46 (file-row 0) should return 1.0") {
            layer.getValue(lat46) shouldBe (1.0 plusOrMinus TOLERANCE)
        }
    }

    "directory constructor: variable auto-detected when variableName is null" {
        val dir = tempDir.resolve("auto-detect").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve("data.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0),
            rawValues = FloatArray(9) { 7f },
        )

        val layer = DoubleCopernicusLayer(envAt(0.0), dir, variable = null)
        withClue("auto-detected variable; all cells = 7.0") {
            layer.getValue(center) shouldBe (7.0 plusOrMinus TOLERANCE)
        }
    }

    "directory constructor: explicit variable name is used correctly" {
        val dir = tempDir.resolve("explicit-var").also { it.toFile().mkdirs() }
        writeTestNetcdf(
            path = dir.resolve("data.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0),
            rawValues = FloatArray(9) { 42f },
            variableName = "dis24",
        )

        val layer = DoubleCopernicusLayer(envAt(0.0), dir, variable = "dis24")
        withClue("explicit variable 'dis24'; all cells = 42.0") {
            layer.getValue(center) shouldBe (42.0 plusOrMinus TOLERANCE)
        }
    }

    "directory constructor: two disjoint files are merged into a single ordered time series" {
        val dir = tempDir.resolve("two-files").also { it.toFile().mkdirs() }
        // file 1: t=0h (all values=10.0) and t=1h (all values=20.0)
        writeTestNetcdf(
            path = dir.resolve("part1.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(0.0, 1.0),
            rawValues = FloatArray(18) { idx -> if (idx < 9) 10f else 20f },
        )
        // file 2: t=2h (all values=30.0) and t=3h (all values=40.0)
        writeTestNetcdf(
            path = dir.resolve("part2.nc"),
            lats = floatArrayOf(44f, 45f, 46f),
            lons = floatArrayOf(11f, 12f, 13f),
            timeHours = doubleArrayOf(2.0, 3.0),
            rawValues = FloatArray(18) { idx -> if (idx < 9) 30f else 40f },
        )
        var t = 0.0
        val layer = DoubleCopernicusLayer(mutableEnv { t }, dir)

        (0..3).forEach {
            val time = it.toDouble()
            val expectedTime = (it + 1) * 10.0
            t = time

            withClue("t=$time. Exact hit on merged series = $expectedTime") {
                layer.getValue(center) shouldBe (expectedTime plusOrMinus TOLERANCE)
            }
        }

        // interpolation across the file boundary: t=1.5 should return the mean with linear interpolation
        t = 1.5
        withClue("t=1.5 crosses file boundary. LINEAR blend of 20.0 and 30.0 = 25.0") {
            layer.getValue(center) shouldBe (25.0 plusOrMinus TOLERANCE)
        }
    }
})
