/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestEagerGridSnapshots {

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
        val gridSnaps = EagerGridSnapshots(dir)
        assertEquals(3, gridSnaps.instants.size)
    }

    @Test
    fun `instants should be sorted in ascending chronological order regardless of file order`() {
        val dir = Files.createTempDirectory(tempDir, "sorted")
        // b_file.nc is read second (in alphabetical order) but contains the most recent times
        writeFixedTestNetcdf(dir, "b_file.nc", doubleArrayOf(48.0, 72.0))
        writeFixedTestNetcdf(dir, "a_file.nc", doubleArrayOf(0.0, 24.0))
        val gridSnaps = EagerGridSnapshots(dir)
        assertEquals(4, gridSnaps.instants.size)
        assertEquals(gridSnaps.instants.sortedWith(compareBy { it }), gridSnaps.instants)
    }

    @Test
    fun `the grid should be accessible for every index in instants`() {
        val dir = Files.createTempDirectory(tempDir, "align")
        writeFixedTestNetcdf(dir, "data.nc", doubleArrayOf(0.0, 24.0, 48.0, 72.0, 96.0))
        val gridSnaps = EagerGridSnapshots(dir)
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
        val resultLats = EagerGridSnapshots(dir).grid(0).latitudes
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
        val grid = EagerGridSnapshots(dir).grid(0)
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
        val grid = EagerGridSnapshots(dir).grid(0)
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
        val grid = EagerGridSnapshots(dir).grid(0)
        assertEquals(0.0, grid.valueAt(0, 0))
        assertEquals(1.0, grid.valueAt(0, 1))
        assertEquals(10.0, grid.valueAt(1, 0))
        assertEquals(11.0, grid.valueAt(1, 1))
    }

    // Configuration errors tests
    @Test
    fun `an empty directory should raise an IllegalArgumentException`() {
        val emptyDir = Files.createTempDirectory(tempDir, "empty")
        assertFailsWith<IllegalArgumentException> { EagerGridSnapshots(emptyDir) }
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
        val gridSnaps = EagerGridSnapshots(dir)
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
        assertFailsWith<IllegalArgumentException> { EagerGridSnapshots(dir) }
    }
}
