/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import it.unibo.alchemist.model.geogrid.reading.ArrayRasterGrid
import it.unibo.alchemist.model.geogrid.reading.Axis
import it.unibo.alchemist.model.geogrid.reading.MapRasterGrid
import it.unibo.alchemist.model.geogrid.reading.RasterGrid
import java.nio.file.Files
import java.nio.file.Path
import java.util.Formatter
import ucar.ma2.Array as CdmArray
import ucar.nc2.Variable
import ucar.nc2.constants.AxisType
import ucar.nc2.dataset.CoordinateAxis1D
import ucar.nc2.dataset.CoordinateAxis1DTime
import ucar.nc2.dataset.NetcdfDataset
import ucar.nc2.dataset.NetcdfDatasets

/**
 * The fraction of non-missing values below which [buildGrid] builds a sparse grid.
 */
private const val SPARSE_DENSITY_THRESHOLD = 0.1

/**
 * The system property selecting the XML parser, which must be the JDK's one for netCDF-Java to read GRIB files.
 */
private const val SAX_PARSER_FACTORY_KEY = "javax.xml.parsers.SAXParserFactory"
private const val JDK_XERCES_SAX_PARSER_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"

/**
 * The suffixes of the index files that netCDF-Java writes next to GRIB files.
 */
private val netcdfJavaIndexSuffixes = setOf(".gbx9", ".ncx4")

/**
 * Opens [file], with missing values as [Double.NaN], making sure netCDF-Java uses the JDK's XML parser meanwhile.
 *
 * NetCDF-Java's GRIB reader requires this factory, because if another SAX parser is registered system-wide,
 * then GRIB parsing fails.
 * The system property is restored to its previous value (or cleared) right after opening.
 */
internal fun openNetcdfDataset(file: Path): NetcdfDataset {
    val previous = System.getProperty(SAX_PARSER_FACTORY_KEY)
    System.setProperty(SAX_PARSER_FACTORY_KEY, JDK_XERCES_SAX_PARSER_FACTORY)
    try {
        return NetcdfDatasets.openDataset(file.toString())
    } finally {
        if (previous == null) {
            System.clearProperty(SAX_PARSER_FACTORY_KEY)
        } else {
            System.setProperty(SAX_PARSER_FACTORY_KEY, previous)
        }
    }
}

/**
 * The data files in [directory], sorted, skipping the indexes written by netCDF-Java.
 *
 * @throws IllegalArgumentException if there are none.
 */
internal fun listDataFiles(directory: Path): List<Path> {
    val files = Files.list(directory).use { stream ->
        stream.filter { Files.isRegularFile(it) }
            .filter { file -> netcdfJavaIndexSuffixes.none { suffix -> file.toString().endsWith(suffix) } }
            .sorted()
            .toList()
    }
    require(files.isNotEmpty()) { "No data files in $directory" }
    return files
}

/**
 * The [FileAxes] of [dataset], opened from [file], reading the variable [variableName] (see [resolveVariable]).
 *
 * @throws IllegalArgumentException if an axis is missing or not one-dimensional, or if the variable is missing,
 * ambiguous, or not over time, latitude, and longitude.
 */
internal fun readFileAxes(dataset: NetcdfDataset, variableName: String?, file: Path): FileAxes {
    // ensures that all {time, lat, lon} axes are present
    val rawTimeAxis = requireNotNull(
        dataset.findCoordinateAxis(AxisType.Time) ?: dataset.findCoordinateAxis(AxisType.RunTime),
    ) {
        "No time axis in $file"
    }
    val latAxis = requireNotNull(dataset.findCoordinateAxis(AxisType.Lat) as? CoordinateAxis1D) {
        "No 1D latitude axis in $file"
    }
    val lonAxis = requireNotNull(dataset.findCoordinateAxis(AxisType.Lon) as? CoordinateAxis1D) {
        "No 1D longitude axis in $file"
    }
    val errMsg = Formatter()
    // constructs a CF-aware time axis
    val timeAxis = requireNotNull(CoordinateAxis1DTime.factory(dataset, rawTimeAxis, errMsg)) {
        "Cannot build time axis in $file: $errMsg"
    }
    // normalizes latitude/longitude to ascending order
    val (latitudes, latDescending) = latAxis.coordValues.ascendingWithDescendingFlag()
    val (longitudes, lonDescending) = lonAxis.coordValues.ascendingWithDescendingFlag()
    // derives the dimension names needed to find the variable and to validate its shape
    val timeDimName = rawTimeAxis.dimensions.first().name
    val latDimName = latAxis.dimensions.first().name
    val lonDimName = lonAxis.dimensions.first().name
    /*
     * finds the variable to read, either by name or by auto-detection
     * of the only variable whose dimensions are {time, lat, lon}.
     */
    val variable = resolveVariable(dataset, variableName, timeDimName, latDimName, lonDimName, file)
    // verifies that the variable has exactly the three expected dimensions, in any order
    val actualDims = variable.dimensions.map { it.name }
    val expectedDims = setOf(timeDimName, latDimName, lonDimName)
    require(actualDims.toSet() == expectedDims) {
        "Variable '${variable.shortName}' in $file has dimensions $actualDims, " +
            "but $expectedDims was expected (in any order)."
    }
    return FileAxes(
        timeAxis = timeAxis,
        latitudes = Axis(latitudes),
        longitudes = Axis(longitudes),
        latDescending = latDescending,
        lonDescending = lonDescending,
        variable = variable,
        // position of each axis in the file
        timePosition = actualDims.indexOf(timeDimName),
        latPosition = actualDims.indexOf(latDimName),
        lonPosition = actualDims.indexOf(lonDimName),
    )
}

/**
 * The values at the [t]-th instant of the file described by [axes], as they are stored,
 * with dimensions ordered as time, latitude ([nLat] values), longitude ([nLon] values).
 */
internal fun readPermutedSlice(axes: FileAxes, t: Int, nLat: Int, nLon: Int): CdmArray {
    // origin/shape are positional relative to the variable's own axis order in the file
    val origin = IntArray(3).also { array ->
        array[axes.timePosition] = t
        array[axes.latPosition] = 0
        array[axes.lonPosition] = 0
    }
    val shape = IntArray(3).also { array ->
        array[axes.timePosition] = 1
        array[axes.latPosition] = nLat
        array[axes.lonPosition] = nLon
    }
    // reorders the read slice to (time, lat, lon)
    return axes.variable
        .read(origin, shape)
        .permute(intArrayOf(axes.timePosition, axes.latPosition, axes.lonPosition))
        .copy()
}

/**
 * The values of [slice] (see [readPermutedSlice]) row by row, as [RasterGrid] expects them,
 * reversing the latitudes if [latDescending] and the longitudes if [lonDescending].
 */
internal fun flattenAscending(
    slice: CdmArray,
    nLat: Int,
    nLon: Int,
    latDescending: Boolean,
    lonDescending: Boolean,
): DoubleArray = DoubleArray(nLat * nLon).also { arr ->
    for (idx in arr.indices) {
        val iLat = idx / nLon
        val iLon = idx % nLon
        val srcLat = if (latDescending) nLat - 1 - iLat else iLat
        val srcLon = if (lonDescending) nLon - 1 - iLon else iLon
        arr[idx] = slice.getDouble(srcLat * nLon + srcLon)
    }
}

/**
 * A [RasterGrid] of [measurements]: a [MapRasterGrid] if they are mostly missing, an [ArrayRasterGrid] otherwise.
 */
internal fun buildGrid(latitudes: Axis, longitudes: Axis, measurements: DoubleArray): RasterGrid {
    val density = measurements.count { !it.isNaN() }.toDouble() / measurements.size
    return if (density < SPARSE_DENSITY_THRESHOLD) {
        MapRasterGrid(latitudes, longitudes, measurements)
    } else {
        ArrayRasterGrid(latitudes, longitudes, measurements)
    }
}

/**
 * The variable of [ds] named [name] or, when `null`, the only one over [timeDimName], [latDimName], and [lonDimName].
 *
 * @throws IllegalArgumentException if there is no such variable, or more than one.
 */
internal fun resolveVariable(
    ds: NetcdfDataset,
    name: String?,
    timeDimName: String,
    latDimName: String,
    lonDimName: String,
    file: Path,
): Variable {
    if (name != null) {
        return requireNotNull(ds.findVariable(name)) {
            "Variable '$name' not found in $file. " +
                "Available: ${ds.variables.map { it.shortName }}"
        }
    }
    val targetDims = setOf(timeDimName, latDimName, lonDimName)
    // 3D variables matching {latitude, longitude, time}
    val candidates = ds.variables.filter { v ->
        v.dimensions.size == 3 &&
            v.dimensions.map { it.name }.toSet() == targetDims
    }
    require(candidates.isNotEmpty()) {
        "No variable with dimensions $targetDims found in $file"
    }
    require(candidates.size == 1) {
        "Multiple candidate variables with dimensions $targetDims in $file. " +
            "The variables that can be used are: ${candidates.map { it.shortName }}. Specify the variable explicitly."
    }
    return candidates.single()
}

/**
 * How a file stores its data: the axes (latitudes and longitudes sorted ascending, whether they were stored
 * descending), the [variable], and the position of each dimension in it.
 */
internal class FileAxes(
    val timeAxis: CoordinateAxis1DTime,
    val latitudes: Axis,
    val longitudes: Axis,
    val latDescending: Boolean,
    val lonDescending: Boolean,
    val variable: Variable,
    val timePosition: Int,
    val latPosition: Int,
    val lonPosition: Int,
)

/**
 * The grid and variable of the first file of a directory, which the others must match.
 */
internal class ReferenceGrid(axes: FileAxes) {

    private val latitudes = axes.latitudes
    private val longitudes = axes.longitudes
    private val variableName = axes.variable.shortName

    /**
     * Checks that [axes], read from [file] in [directory], have the same grid and variable.
     *
     * @throws IllegalArgumentException otherwise.
     */
    fun requireMatches(axes: FileAxes, file: Path, directory: Path) {
        // files following the first one must have the same spatial coordinates
        require(axes.latitudes == latitudes) {
            "Latitude axes differ in $file from the others"
        }
        require(axes.longitudes == longitudes) {
            "Longitude axes differ in $file from the others"
        }
        // subsequent files must resolve to the same variable
        require(axes.variable.shortName == variableName) {
            "Variable name differs across files: '$variableName' vs '${axes.variable.shortName}' " +
                "in $file. All files in $directory must contain the same variable."
        }
    }
}

/**
 * This array in ascending order, and whether it was descending.
 */
private fun DoubleArray.ascendingWithDescendingFlag(): Pair<DoubleArray, Boolean> {
    val descending = first() > last()
    return (if (descending) reversedArray() else this) to descending
}
