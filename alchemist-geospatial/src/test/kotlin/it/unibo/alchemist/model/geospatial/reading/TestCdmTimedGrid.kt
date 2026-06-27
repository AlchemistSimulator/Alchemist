/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geospatial.writeTestNetcdf
import java.nio.file.Files
import java.nio.file.Path

class TestCdmTimedGrid : StringSpec({

    /**
     * the directory where the temporary NetCDF files for the tests will be created
     */
    val tempDir: Path = Files.createTempDirectory("cdm-timed-grid-test")

    // deletes the directory and its files after the tests
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    // Basic reading tests
    "should read a single file and expose the correct number of instants" {
        val dir = Files.createTempDirectory(tempDir, "basic")
        writeFixedTestNetcdf(dir, "data.nc", doubleArrayOf(0.0, 24.0, 48.0))
        val tg = CdmTimedGrid(dir)
        tg.instants.size shouldBe 3
    }

    "instants should be sorted in ascending chronological order regardless of file order" {
        val dir = Files.createTempDirectory(tempDir, "sorted")
        // b_file.nc is read second (in alphabetical order) but contains the most recent times
        writeFixedTestNetcdf(dir, "b_file.nc", doubleArrayOf(48.0, 72.0))
        writeFixedTestNetcdf(dir, "a_file.nc", doubleArrayOf(0.0, 24.0))
        val tg = CdmTimedGrid(dir)
        tg.instants.size shouldBe 4
        tg.instants shouldBeSortedWith compareBy { it }
    }

    "grid[i] should be accessible for every index in instants" {
        val dir = Files.createTempDirectory(tempDir, "align")
        writeFixedTestNetcdf(dir, "data.nc", doubleArrayOf(0.0, 24.0, 48.0, 72.0, 96.0))
        val tg = CdmTimedGrid(dir)
        // ff instants and grids are misaligned, the grid(s) would throw an IndexOutOfBoundsException
        tg.instants.indices.forEach { i -> tg.grid(i).latitudes.size shouldBe 3 }
    }

    // Descending latitude normalization tests
    "latitudes should be ascending even when the file stores them descending" {
        val dir = Files.createTempDirectory(tempDir, "lat-desc")
        writeTestNetcdf(
            path = dir.resolve("desc.nc"),
            lats = floatArrayOf(30f, 20f, 10f), // descending latitudes
            lons = floatArrayOf(5f, 15f, 25f, 35f),
            timeHours = doubleArrayOf(0.0),
        )
        val resultLats = CdmTimedGrid(dir).grid(0).latitudes
        resultLats shouldBe doubleArrayOf(10.0, 20.0, 30.0)
    }

    "values should be correctly re-mapped after descending latitude normalization" {
        val dir = Files.createTempDirectory(tempDir, "lat-remap")
        /*
         * 2-by-2 grid with descending lat. Row 0: north (20°), row 1: south (10°).
         * After normalization: iLat=0 = south (10°), iLat=1 = north (20°).
         */
        writeTestNetcdf(
            path = dir.resolve("remap.nc"),
            lats = floatArrayOf(20f, 10f),
            lons = floatArrayOf(5f, 15f),
            timeHours = doubleArrayOf(0.0),
            // north=[100,101], south=[200,201]
            rawValues = floatArrayOf(100f, 101f, 200f, 201f),
        )
        // checks if the rows get reversed
        val grid = CdmTimedGrid(dir).grid(0)
        grid.valueAt(0, 0) shouldBe 200.0
        grid.valueAt(0, 1) shouldBe 201.0
        grid.valueAt(1, 0) shouldBe 100.0
        grid.valueAt(1, 1) shouldBe 101.0
    }

    // "_FillValue" to NaN test
    "fill values should be exposed as Double.NaN" {
        val dir = Files.createTempDirectory(tempDir, "fillval")
        val fill = -9999f
        writeTestNetcdf(
            path = dir.resolve("fill.nc"),
            lats = floatArrayOf(10f, 20f),
            lons = floatArrayOf(5f, 15f),
            timeHours = doubleArrayOf(0.0),
            rawValues = floatArrayOf(fill, 42f, 42f, 42f),
            fillValue = fill,
        )
        val grid = CdmTimedGrid(dir).grid(0)
        grid.valueAt(0, 0).shouldBeNaN()
        grid.valueAt(0, 1) shouldBe 42.0
    }

    // Configuration errors tests
    "should throw IllegalArgumentException on empty directory" {
        val emptyDir = Files.createTempDirectory(tempDir, "empty")
        shouldThrow<IllegalArgumentException> { CdmTimedGrid(emptyDir) }
    }

    "should throw IllegalArgumentException on duplicate timestamps across files" {
        val dir = Files.createTempDirectory(tempDir, "dup")
        // writes multiple files with overlapping time offsets
        for (i in 1..3) {
            writeFixedTestNetcdf(dir, "f$i.nc", doubleArrayOf(0.0, 24.0))
        }
        shouldThrow<IllegalArgumentException> { CdmTimedGrid(dir) }
    }

    "should throw IllegalArgumentException when files have mismatched spatial grids" {
        val dir = Files.createTempDirectory(tempDir, "mismatch")
        writeTestNetcdf(
            dir.resolve("f1.nc"),
            floatArrayOf(10f, 20f, 30f),
            floatArrayOf(5f, 15f),
            doubleArrayOf(0.0),
        )
        writeTestNetcdf(
            dir.resolve("f2.nc"),
            floatArrayOf(40f, 50f, 60f),
            floatArrayOf(5f, 15f),
            doubleArrayOf(24.0),
        )
        shouldThrow<IllegalArgumentException> { CdmTimedGrid(dir) }
    }
})

/**
 * Creates a NetCDF-3 file with
 * - latitudes: (10°, 20°, 30°)
 * - longitudes: (5°, 15°, 25°, 35°)
 * - the provided hours offset from `2024-01-01 00:00`
 *
 * @param dir the directory where the file will be created (must exist)
 * @param fileName the name of the file
 * @param timeHours hours offsets from `2024-01-01 00:00`
 */
private fun writeFixedTestNetcdf(dir: Path, fileName: String, timeHours: DoubleArray) {
    writeTestNetcdf(
        path = dir.resolve(fileName),
        lats = floatArrayOf(10f, 20f, 30f),
        lons = floatArrayOf(5f, 15f, 25f, 35f),
        timeHours = timeHours,
    )
}
