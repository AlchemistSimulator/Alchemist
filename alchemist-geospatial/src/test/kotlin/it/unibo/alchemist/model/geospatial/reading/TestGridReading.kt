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
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.writeTestNetcdf
import java.nio.file.Files
import java.nio.file.Path
import ucar.ma2.Array as CdmArray
import ucar.nc2.dataset.NetcdfDatasets

class TestGridReading : StringSpec({

    val tempDir: Path = Files.createTempDirectory("netcdf-grid-reading-test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    /**
     * Opens [file], extracts [FileAxes] and closes it.
     */
    fun axesOf(file: Path, variableName: String? = null): FileAxes =
        NetcdfDatasets.openDataset(file.toString()).use { readFileAxes(it, variableName, file) }

    /**
     * Writes a minimal test NetCDF file in a subdir of [tempDir].
     */
    fun testFile(
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
    fun readAxesAndSlice(file: Path, t: Int = 0, nLat: Int = 2, nLon: Int = 2): Pair<FileAxes, CdmArray> =
        NetcdfDatasets.openDataset(file.toString()).use { ds ->
            val axes = readFileAxes(ds, null, file)
            axes to readPermutedSlice(axes, t, nLat, nLon)
        }

    // listDataFiles tests
    "listDataFiles should list regular files sorted in a deterministic order" {
        val dir = Files.createTempDirectory(tempDir, "list-sorted")
        val c = Files.createFile(dir.resolve("c.nc"))
        val a = Files.createFile(dir.resolve("a.nc"))
        val b = Files.createFile(dir.resolve("b.nc"))

        listDataFiles(dir) shouldBe listOf(a, b, c)
    }

    "listDataFiles should throw on an empty directory" {
        val dir = Files.createTempDirectory(tempDir, "list-empty")
        shouldThrow<IllegalArgumentException> { listDataFiles(dir) }
    }

    // readFileAxes tests
    "readFileAxes should be able to extract all its properties" {
        val axes = axesOf(
            testFile(
                "axes-basic",
                lats = doubleArrayOf(10.0, 20.0, 30.0),
                lons = doubleArrayOf(5.0, 15.0, 25.0, 35.0),
                timeHours = doubleArrayOf(0.0, 24.0, 48.0),
            ),
        )

        axes.latitudes shouldBe doubleArrayOf(10.0, 20.0, 30.0)
        axes.longitudes shouldBe doubleArrayOf(5.0, 15.0, 25.0, 35.0)
        axes.latDescending shouldBe false
        axes.lonDescending shouldBe false
        axes.variable.shortName shouldBe "dis24"
        axes.timePosition shouldBe 0
        axes.latPosition shouldBe 1
        axes.lonPosition shouldBe 2
        axes.timeAxis.size.toInt() shouldBe 3
    }

    "readFileAxes should detect a descending latitude/longitude axis and reverse it/them" {
        // lat check
        val latAxes = axesOf(testFile("axes-lat-desc", lats = doubleArrayOf(30.0, 20.0, 10.0)))
        latAxes.latitudes shouldBe doubleArrayOf(10.0, 20.0, 30.0)
        latAxes.latDescending shouldBe true

        // long check
        val lonAxes = axesOf(testFile("axes-lon-desc", lons = doubleArrayOf(35.0, 25.0, 15.0, 5.0)))
        lonAxes.longitudes shouldBe doubleArrayOf(5.0, 15.0, 25.0, 35.0)
        lonAxes.lonDescending shouldBe true
    }

    "readFileAxes should preserve an explicit variable name" {
        val file = testFile("axes-explicit-name", variables = listOf(TestVariable("wind_speed")))
        axesOf(file, variableName = "wind_speed").variable.shortName shouldBe "wind_speed"
    }

    "readFileAxes should compute axis positions correctly regardless of the file dimension order" {
        val axes = axesOf(testFile("axes-scrambled", dimensionOrder = listOf("longitude", "time", "latitude")))

        axes.lonPosition shouldBe 0
        axes.timePosition shouldBe 1
        axes.latPosition shouldBe 2
    }

    // resolveVariable tests
    "resolveVariable should throw when the variable does not exist" {
        val file = testFile("resolve-missing")

        NetcdfDatasets.openDataset(file.toString()).use { ds ->
            shouldThrow<IllegalArgumentException> {
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

    "resolveVariable should throw when auto-detection matches more than one variable" {
        val file = testFile(
            "resolve-ambiguous",
            variables = listOf(
                TestVariable("first"),
                TestVariable("second"),
            ),
        )

        NetcdfDatasets.openDataset(file.toString()).use { ds ->
            shouldThrow<IllegalArgumentException> {
                resolveVariable(ds, null, "time", "latitude", "longitude", file)
            }
        }
    }

    // readPermutedSlice tests
    "readPermutedSlice should reorder values to (time, lat, lon) regardless of the file axes order" {
        val file = testFile("permute", dimensionOrder = listOf("longitude", "time", "latitude"))
        val (_, slice) = readAxesAndSlice(file)

        slice.getDouble(0) shouldBe 0.0
        slice.getDouble(1) shouldBe 1.0
        slice.getDouble(2) shouldBe 10.0
        slice.getDouble(3) shouldBe 11.0
    }

    "readPermutedSlice should select the requested time index" {
        val file = testFile(
            "time-index",
            timeHours = doubleArrayOf(0.0, 24.0),
            variables = listOf(
                TestVariable(rawValues = doubleArrayOf(100.0, 101.0, 102.0, 103.0, 200.0, 201.0, 202.0, 203.0)),
            ),
        )

        NetcdfDatasets.openDataset(file.toString()).use { ds ->
            val axes = readFileAxes(ds, null, file)
            readPermutedSlice(axes, t = 0, nLat = 2, nLon = 2).getDouble(0) shouldBe 100.0
            readPermutedSlice(axes, t = 1, nLat = 2, nLon = 2).getDouble(0) shouldBe 200.0
        }
    }

    // flattenAscending tests
    "flattenAscending should preserve values when both axes are ascending" {
        val file = testFile(
            "flatten-ascending",
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (axes, slice) = readAxesAndSlice(file)

        flattenAscending(slice, 2, 2, axes.latDescending, axes.lonDescending) shouldBe
            doubleArrayOf(1.0, 2.0, 3.0, 4.0)
    }

    "flattenAscending should reverse latitudes/longitudes when they are descending" {
        // lat check
        val fileLat = testFile(
            "lat-decreasing",
            lats = doubleArrayOf(20.0, 10.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (latAxes, latSlice) = readAxesAndSlice(fileLat)
        flattenAscending(latSlice, 2, 2, latAxes.latDescending, latAxes.lonDescending) shouldBe
            doubleArrayOf(3.0, 4.0, 1.0, 2.0)

        // lon check
        val fileLon = testFile(
            "lon-decreasing",
            lons = doubleArrayOf(15.0, 5.0),
            variables = listOf(TestVariable(rawValues = doubleArrayOf(1.0, 2.0, 3.0, 4.0))),
        )
        val (lonAxes, lonSlice) = readAxesAndSlice(fileLon)
        flattenAscending(lonSlice, 2, 2, lonAxes.latDescending, lonAxes.lonDescending) shouldBe
            doubleArrayOf(2.0, 1.0, 4.0, 3.0)
    }

    // ReferenceGrid tests
    "ReferenceGrid should accept a second file whose grid and variable match" {
        val reference = ReferenceGrid(axesOf(testFile("reference-match-a")))
        val fileB = testFile("reference-match-b", timeHours = doubleArrayOf(24.0))
        reference.requireMatches(axesOf(fileB), fileB, tempDir)
    }

    "ReferenceGrid should throw on a file with different latitudes/longitudes" {
        // latitude
        val latReference = ReferenceGrid(axesOf(testFile("reference-lat-mismatch-a")))
        val fileB = testFile("reference-lat-mismatch-b", lats = doubleArrayOf(11.0, 21.0))
        shouldThrow<IllegalArgumentException> { latReference.requireMatches(axesOf(fileB), fileB, tempDir) }

        // longitude
        val lonReference = ReferenceGrid(axesOf(testFile("reference-lon-mismatch-a")))
        val fileC = testFile("reference-lon-mismatch-b", lons = doubleArrayOf(6.0, 16.0))
        shouldThrow<IllegalArgumentException> { lonReference.requireMatches(axesOf(fileB), fileC, tempDir) }
    }

    "ReferenceGrid should throw on a file with a different variable" {
        val reference = ReferenceGrid(
            axesOf(testFile("prima_file", variables = listOf(TestVariable("prima")))),
        )

        val fileB = testFile("seconda_file", variables = listOf(TestVariable("seconda")))
        shouldThrow<IllegalArgumentException> { reference.requireMatches(axesOf(fileB), fileB, tempDir) }
    }
})
