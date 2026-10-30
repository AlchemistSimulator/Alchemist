/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ucar.ma2.Array

class TestGridReading {

    private val tempDir: Path = Files.createTempDirectory("netcdf-grid-reading-test")

    private val gribFileName = "fc.grib"

    /**
     * Directory containing a real, manually downloaded GRIB fixture.
     * The grib is from the "reanalysis-era5-single-levels" dataset.
     */
    private val realGribsDir: Path = Path.of(
        requireNotNull(javaClass.getResource("/gribs")) {
            "Test resource directory 'gribs' not found on the classpath"
        }.toURI(),
    )

    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    /**
     * Opens [file], extracts [FileAxes] and closes it.
     */
    private fun axesOf(file: Path, variableName: String? = null): FileAxes =
        openNetcdfDataset(file).use { readFileAxes(it, variableName, file) }

    /**
     * Writes a minimal test NetCDF file in a subdir of [tempDir].
     */
    private fun testFile(
        label: String,
        lats: DoubleArray = doubleArrayOf(10.0, 20.0),
        lons: DoubleArray = doubleArrayOf(5.0, 15.0),
        timeHours: DoubleArray = doubleArrayOf(0.0),
        variables: List<TestVariable> = listOf(TestVariable("dis24")),
        dimensionOrder: List<String> = listOf("time", "latitude", "longitude"),
    ): Path {
        val file = Files.createTempDirectory(tempDir, label).resolve("data.nc")
        writeTestNetcdf(
            path = file,
            lats = lats,
            lons = lons,
            timeHours = timeHours,
            variables = variables,
            dimensionOrder = dimensionOrder,
        )
        return file
    }

    /**
     * Opens [file] and returns its [FileAxes] with the permuted slice at time index [t].
     */
    private fun readAxesAndSlice(file: Path, t: Int = 0, nLat: Int = 2, nLon: Int = 2): Pair<FileAxes, Array> =
        openNetcdfDataset(file).use { ds ->
            val axes = readFileAxes(ds, null, file)
            axes to readPermutedSlice(axes, t, nLat, nLon)
        }

    // listDataFiles tests
    @Test
    fun `listDataFiles should list regular files sorted in a deterministic order`() {
        val dir = Files.createTempDirectory(tempDir, "list-sorted")
        val c = Files.createFile(dir.resolve("c.nc"))
        val a = Files.createFile(dir.resolve("a.nc"))
        val b = Files.createFile(dir.resolve("b.nc"))
        assertEquals(listOf(a, b, c), listDataFiles(dir))
    }

    @Test
    fun `listDataFiles should throw on an empty directory`() {
        val dir = Files.createTempDirectory(tempDir, "list-empty")
        assertFailsWith<IllegalArgumentException> { listDataFiles(dir) }
    }

    // readFileAxes tests
    @Test
    fun `readFileAxes should be able to extract all its properties`() {
        val axes = axesOf(
            testFile(
                "axes-basic",
                lats = doubleArrayOf(10.0, 20.0, 30.0),
                lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0),
                timeHours = doubleArrayOf(0.0, 24.0, 48.0),
            ),
        )
        assertContentEquals(doubleArrayOf(10.0, 20.0, 30.0), axes.latitudes)
        assertContentEquals(doubleArrayOf(5.0, 15.0, 25.0, 35.0), axes.longitudes)
        assertFalse(axes.latDescending)
        assertFalse(axes.lonDescending)
        assertEquals("dis24", axes.variable.shortName)
        assertEquals(0, axes.timePosition)
        assertEquals(1, axes.latPosition)
        assertEquals(2, axes.lonPosition)
        assertEquals(3, axes.timeAxis.size.toInt())
    }

    @Test
    fun `readFileAxes should detect a descending latitude or longitude axis and reverse it`() {
        // lat check
        val latAxes = axesOf(testFile("axes-lat-desc", lats = doubleArrayOf(30.0, 20.0, 10.0)))
        assertContentEquals(doubleArrayOf(10.0, 20.0, 30.0), latAxes.latitudes)
        assertTrue(latAxes.latDescending)
        // lon check
        val lonAxes = axesOf(testFile("axes-lon-desc", lons = doubleArrayOf(35.0, 25.0, 15.0, 5.0)))
        assertContentEquals(doubleArrayOf(5.0, 15.0, 25.0, 35.0), lonAxes.longitudes)
        assertTrue(lonAxes.lonDescending)
    }

    @Test
    fun `readFileAxes should preserve an explicit variable name`() {
        val file = testFile("axes-explicit-name", variables = listOf(TestVariable("wind_speed")))
        assertEquals("wind_speed", axesOf(file, variableName = "wind_speed").variable.shortName)
    }

    @Test
    fun `readFileAxes should compute axis positions correctly regardless of the file dimension order`() {
        val axes = axesOf(testFile("axes-scrambled", dimensionOrder = listOf("longitude", "time", "latitude")))
        assertEquals(0, axes.lonPosition)
        assertEquals(1, axes.timePosition)
        assertEquals(2, axes.latPosition)
    }

    // resolveVariable tests
    @Test
    fun `resolveVariable should throw when the variable does not exist`() {
        val file = testFile("resolve-missing")
        openNetcdfDataset(file).use { ds ->
            assertFailsWith<IllegalArgumentException> {
                resolveVariable(
                    ds,
                    "this_variable_does_not_exist_and_should_throw",
                    "time",
                    "latitude",
                    "longitude",
                    file,
                )
            }
        }
    }

    @Test
    fun `resolveVariable should throw when auto-detection matches more than one variable`() {
        val file = testFile(
            "resolve-ambiguous",
            variables = listOf(
                TestVariable("first"),
                TestVariable("second"),
            ),
        )
        openNetcdfDataset(file).use { ds ->
            assertFailsWith<IllegalArgumentException> {
                resolveVariable(ds, null, "time", "latitude", "longitude", file)
            }
        }
    }

    // readPermutedSlice tests
    @Test
    fun `readPermutedSlice should reorder values to time-lat-lon regardless of the file axes order`() {
        val file = testFile("permute", dimensionOrder = listOf("longitude", "time", "latitude"))
        val (_, slice) = readAxesAndSlice(file)
        assertEquals(0.0, slice.getDouble(0))
        assertEquals(1.0, slice.getDouble(1))
        assertEquals(10.0, slice.getDouble(2))
        assertEquals(11.0, slice.getDouble(3))
    }

    @Test
    fun `readPermutedSlice should select the requested time index`() {
        val file = testFile(
            "time-index",
            timeHours = doubleArrayOf(0.0, 24.0),
            variables = listOf(
                TestVariable(rawValues = doubleArrayOf(100.0, 101.0, 102.0, 103.0, 200.0, 201.0, 202.0, 203.0)),
            ),
        )
        openNetcdfDataset(file).use { ds ->
            val axes = readFileAxes(ds, null, file)
            assertEquals(100.0, readPermutedSlice(axes, t = 0, nLat = 2, nLon = 2).getDouble(0))
            assertEquals(200.0, readPermutedSlice(axes, t = 1, nLat = 2, nLon = 2).getDouble(0))
        }
    }

    // flattenAscending tests
    @Test
    fun `flattenAscending should preserve values when both axes are ascending`() {
        val file = testFile(
            "flatten-ascending",
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (axes, slice) = readAxesAndSlice(file)
        assertContentEquals(
            doubleArrayOf(1.0, 2.0, 3.0, 4.0),
            flattenAscending(slice, 2, 2, axes.latDescending, axes.lonDescending),
        )
    }

    @Test
    fun `flattenAscending should reverse latitudes or longitudes when they are descending`() {
        // lat check
        val fileLat = testFile(
            "lat-decreasing",
            lats = doubleArrayOf(20.0, 10.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (latAxes, latSlice) = readAxesAndSlice(fileLat)
        assertContentEquals(
            doubleArrayOf(3.0, 4.0, 1.0, 2.0),
            flattenAscending(latSlice, 2, 2, latAxes.latDescending, latAxes.lonDescending),
        )
        // lon check
        val fileLon = testFile(
            "lon-decreasing",
            lons = doubleArrayOf(15.0, 5.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (lonAxes, lonSlice) = readAxesAndSlice(fileLon)
        assertContentEquals(
            doubleArrayOf(2.0, 1.0, 4.0, 3.0),
            flattenAscending(lonSlice, 2, 2, lonAxes.latDescending, lonAxes.lonDescending),
        )
    }

    // ReferenceGrid tests
    @Test
    fun `ReferenceGrid should accept a second file whose grid and variable match`() {
        val reference = ReferenceGrid(axesOf(testFile("reference-match-a")))
        val fileB = testFile("reference-match-b", timeHours = doubleArrayOf(24.0))
        reference.requireMatches(axesOf(fileB), fileB, tempDir)
    }

    @Test
    fun `ReferenceGrid should throw on a file with different latitudes or longitudes`() {
        // latitude
        val latReference = ReferenceGrid(axesOf(testFile("reference-lat-mismatch-a")))
        val fileB = testFile("reference-lat-mismatch-b", lats = doubleArrayOf(11.0, 21.0))
        assertFailsWith<IllegalArgumentException> { latReference.requireMatches(axesOf(fileB), fileB, tempDir) }
        // longitude
        val lonReference = ReferenceGrid(axesOf(testFile("reference-lon-mismatch-a")))
        val fileC = testFile("reference-lon-mismatch-b", lons = doubleArrayOf(6.0, 16.0))
        assertFailsWith<IllegalArgumentException> { lonReference.requireMatches(axesOf(fileB), fileC, tempDir) }
    }

    @Test
    fun `ReferenceGrid should throw on a file with a different variable`() {
        val reference = ReferenceGrid(
            axesOf(testFile("prima_file", variables = listOf(TestVariable("prima")))),
        )
        val fileB = testFile("seconda_file", variables = listOf(TestVariable("seconda")))
        assertFailsWith<IllegalArgumentException> { reference.requireMatches(axesOf(fileB), fileB, tempDir) }
    }

    // GRIB reading tests
    @Test
    fun `listDataFiles should find the real GRIB fixture and ignore index files`() {
        val fileNames = listDataFiles(realGribsDir).map { it.fileName.toString() }
        assertTrue(gribFileName in fileNames, "Expected $gribFileName in $fileNames")
    }

    @Test
    fun `readFileAxes should not fail on a GRIB file`() {
        val file = realGribsDir.resolve(gribFileName)
        val axes = axesOf(file)
        assertTrue(axes.latitudes.size > 0, "Expected at least one latitude, got ${axes.latitudes.size}")
        assertTrue(axes.longitudes.size > 0, "Expected at least one longitude, got ${axes.longitudes.size}")
        val timeSize = axes.timeAxis.size.toInt()
        assertTrue(timeSize > 0, "Expected at least one time instant, got $timeSize")
    }

    @Test
    fun `the GridReading pipeline should not fail on a GRIB file`() {
        val file = realGribsDir.resolve(gribFileName)
        openNetcdfDataset(file).use { ds ->
            val axes = readFileAxes(ds, null, file)
            val nLat = axes.latitudes.size
            val nLon = axes.longitudes.size
            val slice = readPermutedSlice(axes, t = 0, nLat = nLat, nLon = nLon)
            val flat = flattenAscending(slice, nLat, nLon, axes.latDescending, axes.lonDescending)
            assertEquals(nLat * nLon, flat.size)
            buildGrid(axes.latitudes, axes.longitudes, flat) // this must not throw
        }
    }
}
