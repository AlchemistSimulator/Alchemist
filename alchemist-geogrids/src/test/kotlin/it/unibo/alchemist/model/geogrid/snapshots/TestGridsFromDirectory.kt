/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.snapshots

import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.mockGeoPosition
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestGridsFromDirectory {

    /**
     * Creates a NetCDF-3 file with:
     * - latitudes: (10°, 20°, 30°).
     * - longitudes: (5°, 15°, 25°, 35°).
     * - the provided hours offset from `2024-01-01 00:00`.
     *
     * @param dir the directory where the file will be created (must exist).
     * @param fileName the name of the file.
     * @param timeHours hours offsets from `2024-01-01 00:00`.
     */
    private fun writeFixedTestNetcdf(dir: Path, fileName: String, timeHours: DoubleArray) {
        writeTestNetcdf(
            path = dir.resolve(fileName),
            lats = doubleArrayOf(10.0, 20.0, 30.0),
            lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0),
            timeHours = timeHours,
        )
    }

    /**
     * the directory where the temporary NetCDF files for the tests will be created.
     */
    private val tempDir: Path = Files.createTempDirectory("eagergridsnapshots-test")

    /**
     * A new directory, named after [label], with a 2-by-2 NetCDF file of [variables].
     */
    private fun singleFileDirectory(
        label: String,
        lats: DoubleArray = doubleArrayOf(10.0, 20.0),
        lons: DoubleArray = doubleArrayOf(5.0, 15.0),
        timeHours: DoubleArray = doubleArrayOf(0.0),
        variables: List<TestVariable> = listOf(TestVariable("dis24")),
    ): String {
        val dir = Files.createTempDirectory(tempDir, label)
        writeTestNetcdf(dir.resolve("data.nc"), lats, lons, timeHours, variables)
        return dir.toString()
    }

    private fun RasterGrid.values(): List<Double> = (0 until latitudes.size).flatMap { latitude ->
        (0 until longitudes.size).map { longitude -> valueAt(latitude, longitude) }
    }

    // deletes the directory and its files after the tests
    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    // Basic reading tests
    @Test
    fun `a single file should be read exposing the correct number of instants`() {
        val dir = Files.createTempDirectory(tempDir, "basic")
        writeFixedTestNetcdf(dir, "data.nc", doubleArrayOf(0.0, 24.0, 48.0))
        val gridSnaps = GridsFromDirectory(dir.toString())
        assertEquals(3, gridSnaps.instants.size)
    }

    @Test
    fun `instants should be sorted in ascending chronological order regardless of file order`() {
        val dir = Files.createTempDirectory(tempDir, "sorted")
        // b_file.nc is read second (in alphabetical order) but contains the most recent times
        writeFixedTestNetcdf(dir, "b_file.nc", doubleArrayOf(48.0, 72.0))
        writeFixedTestNetcdf(dir, "a_file.nc", doubleArrayOf(0.0, 24.0))
        val gridSnaps = GridsFromDirectory(dir.toString())
        assertEquals(4, gridSnaps.instants.size)
        assertEquals(gridSnaps.instants.sortedWith(compareBy { it }), gridSnaps.instants)
    }

    @Test
    fun `the grid should be accessible for every index in instants`() {
        val dir = Files.createTempDirectory(tempDir, "align")
        writeFixedTestNetcdf(dir, "data.nc", doubleArrayOf(0.0, 24.0, 48.0, 72.0, 96.0))
        val gridSnaps = GridsFromDirectory(dir.toString())
        // if instants and grids were misaligned, the grid(s) would throw an IndexOutOfBoundsException
        gridSnaps.instants.indices.forEach { i -> assertEquals(3, gridSnaps.grid(i).latitudes.size) }
    }

    // Descending latitude normalization tests
    @Test
    fun `latitudes should be ascending even when the file stores them descending`() {
        val dir = Files.createTempDirectory(tempDir, "lat-desc")
        writeTestNetcdf(
            path = dir.resolve("desc.nc"),
            lats = doubleArrayOf(30.0, 20.0, 10.0), // descending latitudes
            lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0),
            timeHours = doubleArrayOf(0.0),
        )
        val resultLats = GridsFromDirectory(dir.toString()).grid(0).latitudes.toDoubleArray()
        assertContentEquals(doubleArrayOf(10.0, 20.0, 30.0), resultLats)
    }

    @Test
    fun `values should be correctly re-mapped after descending latitude normalization`() {
        val dir = Files.createTempDirectory(tempDir, "lat-remap")
        /*
         * 2-by-2 grid with descending lat. Row 0: north (20°), row 1: south (10°).
         * After normalization: iLat=0 = south (10°), iLat=1 = north (20°).
         */
        writeTestNetcdf(
            path = dir.resolve("remap.nc"),
            lats = doubleArrayOf(20.0, 10.0),
            lons = doubleArrayOf(5.0, 15.0),
            timeHours = doubleArrayOf(0.0),
            // north=[100,101], south=[200,201]
            variables = listOf(TestVariable(rawValues = doubleArrayOf(100.0, 101.0, 200.0, 201.0))),
        )
        // checks if the rows get reversed
        val grid = GridsFromDirectory(dir.toString()).grid(0)
        assertEquals(200.0, grid.valueAt(0, 0))
        assertEquals(201.0, grid.valueAt(0, 1))
        assertEquals(100.0, grid.valueAt(1, 0))
        assertEquals(101.0, grid.valueAt(1, 1))
    }

    // "_FillValue" to NaN test
    @Test
    fun `fill values should be exposed as NaN`() {
        val dir = Files.createTempDirectory(tempDir, "fillval")
        val fill = -9999.0
        writeTestNetcdf(
            path = dir.resolve("fill.nc"),
            lats = doubleArrayOf(10.0, 20.0),
            lons = doubleArrayOf(5.0, 15.0),
            timeHours = doubleArrayOf(0.0),
            variables = listOf(
                TestVariable(
                    rawValues = doubleArrayOf(fill, 42.0, 42.0, 42.0),
                    fillValue = fill,
                ),
            ),
        )
        val grid = GridsFromDirectory(dir.toString()).grid(0)
        val filled = grid.valueAt(0, 0)
        assertTrue(filled.isNaN(), "Expected NaN, but got $filled")
        assertEquals(42.0, grid.valueAt(0, 1))
    }

    // Dimension order test
    @Test
    fun `values should be read correctly regardless of the on-disk dimension order`() {
        val dir = Files.createTempDirectory(tempDir, "dim-order")
        writeTestNetcdf(
            path = dir.resolve("permuted.nc"),
            lats = doubleArrayOf(10.0, 20.0),
            lons = doubleArrayOf(5.0, 15.0),
            timeHours = doubleArrayOf(0.0, 24.0),
            // scrambled relative to the canonical (time, latitude, longitude) order
            dimensionOrder = listOf("longitude", "time", "latitude"),
        )
        val grid = GridsFromDirectory(dir.toString()).grid(0)
        assertEquals(0.0, grid.valueAt(0, 0))
        assertEquals(1.0, grid.valueAt(0, 1))
        assertEquals(10.0, grid.valueAt(1, 0))
        assertEquals(11.0, grid.valueAt(1, 1))
    }

    // Configuration errors tests
    @Test
    fun `an empty directory should raise an IllegalArgumentException`() {
        val emptyDir = Files.createTempDirectory(tempDir, "empty")
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(emptyDir.toString()) }
    }

    @Test
    fun `duplicate timestamps across files should be ignored keeping the data of the first one read`() {
        val dir = Files.createTempDirectory(tempDir, "dup-values")
        val lats = doubleArrayOf(10.0)
        val lons = doubleArrayOf(5.0)
        val time = doubleArrayOf(0.0)
        val firstVal = 42.0
        val secondVal = 99.0
        // both files use the same instant
        writeTestNetcdf(
            path = dir.resolve("first_file.nc"),
            lats = lats,
            lons = lons,
            timeHours = time,
            variables = listOf(TestVariable(rawValues = doubleArrayOf(firstVal))),
        )
        writeTestNetcdf(
            path = dir.resolve("second_file.nc"),
            lats = lats,
            lons = lons,
            timeHours = time,
            variables = listOf(TestVariable(rawValues = doubleArrayOf(secondVal))),
        )
        val gridSnaps = GridsFromDirectory(dir.toString())
        assertEquals(1, gridSnaps.instants.size)
        assertEquals(firstVal, gridSnaps.grid(0).valueAt(0, 0))
    }

    @Test
    fun `files with mismatched spatial grids should raise an IllegalArgumentException`() {
        val dir = Files.createTempDirectory(tempDir, "mismatch")
        writeTestNetcdf(
            dir.resolve("f1.nc"),
            doubleArrayOf(10.0, 20.0, 30.0),
            doubleArrayOf(5.0, 15.0),
            doubleArrayOf(0.0),
        )
        writeTestNetcdf(
            dir.resolve("f2.nc"),
            doubleArrayOf(40.0, 50.0, 60.0),
            doubleArrayOf(5.0, 15.0),
            doubleArrayOf(24.0),
        )
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir.toString()) }
    }

    @Test
    fun `files with mismatched longitudes should raise an IllegalArgumentException`() {
        val dir = Files.createTempDirectory(tempDir, "lon-mismatch")
        writeTestNetcdf(dir.resolve("f1.nc"), doubleArrayOf(10.0, 20.0), doubleArrayOf(5.0, 15.0), doubleArrayOf(0.0))
        writeTestNetcdf(dir.resolve("f2.nc"), doubleArrayOf(10.0, 20.0), doubleArrayOf(6.0, 16.0), doubleArrayOf(24.0))
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir.toString()) }
    }

    @Test
    fun `files with different variables should raise an IllegalArgumentException`() {
        val dir = Files.createTempDirectory(tempDir, "variable-mismatch")
        val lats = doubleArrayOf(10.0, 20.0)
        val lons = doubleArrayOf(5.0, 15.0)
        writeTestNetcdf(dir.resolve("f1.nc"), lats, lons, doubleArrayOf(0.0), listOf(TestVariable("prima")))
        writeTestNetcdf(dir.resolve("f2.nc"), lats, lons, doubleArrayOf(24.0), listOf(TestVariable("seconda")))
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir.toString()) }
    }

    // Variable selection tests
    @Test
    fun `the variable with the given name should be read`() {
        val dir = singleFileDirectory(
            "explicit-variable",
            variables = listOf(
                TestVariable("first", rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0)),
                TestVariable("second", rawValues = doubleArrayOf(5.0, 6.0, 7.0, 8.0)),
            ),
        )
        assertEquals(listOf(5.0, 6.0, 7.0, 8.0), GridsFromDirectory(dir, variable = "second").grid(0).values())
    }

    @Test
    fun `a missing variable should raise an IllegalArgumentException`() {
        val dir = singleFileDirectory("missing-variable")
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir, variable = "not_in_the_file") }
    }

    @Test
    fun `an ambiguous variable should raise an IllegalArgumentException`() {
        val dir = singleFileDirectory("ambiguous-variable", variables = listOf(TestVariable("a"), TestVariable("b")))
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir) }
    }

    // Values tests
    @Test
    fun `each instant should expose the values of its own time slice`() {
        val dir = singleFileDirectory(
            "time-slices",
            timeHours = doubleArrayOf(0.0, 24.0),
            variables = listOf(
                TestVariable(rawValues = doubleArrayOf(100.0, 101.0, 102.0, 103.0, 200.0, 201.0, 202.0, 203.0)),
            ),
        )
        val snapshots = GridsFromDirectory(dir)
        assertEquals(listOf(100.0, 101.0, 102.0, 103.0), snapshots.grid(0).values())
        assertEquals(listOf(200.0, 201.0, 202.0, 203.0), snapshots.grid(1).values())
    }

    @Test
    fun `values should be correctly re-mapped after descending longitude normalization`() {
        val dir = singleFileDirectory(
            "lon-remap",
            lons = doubleArrayOf(15.0, 5.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val grid = GridsFromDirectory(dir).grid(0)
        assertContentEquals(doubleArrayOf(5.0, 15.0), grid.longitudes.toDoubleArray())
        assertEquals(listOf(2.0, 1.0, 4.0, 3.0), grid.values())
    }

    @Test
    fun `longitudes stored in 0 to 360 should be moved to -180 to 180, along with their values`() {
        val dir = singleFileDirectory(
            "lon-0-360",
            lons = doubleArrayOf(0.0, 90.0, 180.0, 270.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0))),
        )
        val grid = GridsFromDirectory(dir).grid(0)
        assertContentEquals(doubleArrayOf(-90.0, 0.0, 90.0, 180.0), grid.longitudes.toDoubleArray())
        assertEquals(listOf(4.0, 1.0, 2.0, 3.0, 8.0, 5.0, 6.0, 7.0), grid.values())
        assertTrue(grid.isInBounds(mockGeoPosition(15.0, -45.0)))
    }

    @Test
    fun `longitudes overlapping once moved to -180 to 180 should raise an IllegalArgumentException`() {
        val dir = singleFileDirectory("lon-overlap", lons = doubleArrayOf(0.0, 360.0))
        assertFailsWith<IllegalArgumentException> { GridsFromDirectory(dir) }
    }

    // GRIB reading test
    @Test
    fun `a classpath directory of GRIB files and their indexes should be read`() {
        /*
         * real, manually downloaded data from the "reanalysis-era5-single-levels" dataset,
         * next to the index files of netCDF-Java, which would fail to open as data
         */
        val snapshots = GridsFromDirectory("gribs")
        assertTrue(snapshots.instants.isNotEmpty(), "Expected at least one time instant")
        val grid = snapshots.grid(0)
        assertTrue(grid.latitudes.size > 0, "Expected at least one latitude, got ${grid.latitudes.size}")
        assertTrue(grid.longitudes.size > 0, "Expected at least one longitude, got ${grid.longitudes.size}")
    }
}
