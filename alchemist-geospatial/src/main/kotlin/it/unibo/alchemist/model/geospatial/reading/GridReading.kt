/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import java.nio.file.Files
import java.nio.file.Path
import java.util.Formatter
import ucar.ma2.Array as CdmArray
import ucar.nc2.Variable
import ucar.nc2.constants.AxisType
import ucar.nc2.dataset.CoordinateAxis1D
import ucar.nc2.dataset.CoordinateAxis1DTime
import ucar.nc2.dataset.NetcdfDataset

/*
 * NetCDF/GRIB schema parsing based on NetCDF-Java.
 * These functions know how to read and validate ONE file's grid schema and compare it
 * to other files' schema.
 */

/**
 * Below this fraction of non-missing cells, a grid is considered "sparse" (see [buildGrid]).
 */
private const val SPARSE_DENSITY_THRESHOLD = 0.1

/**
 * Lists the regular files contained in [directory], sorted for a deterministic processing order.
 *
 * @param directory the directory to scan.
 * @return the sorted list of regular files in [directory].
 * @throws IllegalArgumentException if [directory] contains no regular files.
 */
internal fun listDataFiles(directory: Path): List<Path> {
    val files = Files.list(directory).use { stream ->
        stream.filter { Files.isRegularFile(it) }
            .sorted()
            .toList()
    }
    require(files.isNotEmpty()) { "No data files in $directory" }
    return files
}

/**
 * Reads and validates the schema of [file] from the already open [dataset]: locates its time,
 * latitude, and longitude axes, normalizes latitude/longitude to ascending order, and resolves
 * the data [Variable] to read (by [variableName], or by auto-detection, see [resolveVariable]).
 *
 * @param dataset the already open dataset.
 * @param variableName explicit variable name, or `null` for auto-detection.
 * @param file path of the file being read (used for error messages).
 * @return the extracted, normalized [FileAxes].
 * @throws IllegalArgumentException if any axis is missing or not 1D, if the time axis cannot be
 * built, if the variable is missing/ambiguous, or if its dimensions are not `{time, lat, lon}`.
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
        latitudes = latitudes,
        longitudes = longitudes,
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
 * Reads the raw 2D slice at time index [t] from [axes], reordered to (time, lat, lon).
 *
 * @param axes the schema of the file being read.
 * @param t the time index within [axes]'s time axis.
 * @param nLat number of latitude coordinates.
 * @param nLon number of longitude coordinates.
 * @return the rearranged slice of shape (1, [nLat], [nLon]).
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
 * Flattens a **(time, lat, lon)-ordered** [slice] of shape (1, [nLat], [nLon]) into a
 * row-major [DoubleArray] with ascending latitude/longitude, reversing axes that are descending.
 *
 * @param slice the reordered slice (see [readPermutedSlice]).
 * @param nLat number of latitude coordinates.
 * @param nLon number of longitude coordinates.
 * @param latDescending `true` if the latitude axis is descending.
 * @param lonDescending `true` if the longitude axis is descending.
 * @return a [DoubleArray] representing the values in row-major order.
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
 * Chooses the most memory-efficient [RasterGrid] representation for [measurements], based on the
 * fraction of non-missing cells: below [SPARSE_DENSITY_THRESHOLD], a sparse [MapRasterGrid] is
 * used; otherwise, a dense [ArrayRasterGrid].
 *
 * @param latitudes the grid latitudes.
 * @param longitudes the grid longitudes.
 * @param measurements cell values in row-major order; [Double.NaN] denotes missing/fill values.
 * @return the constructed [RasterGrid].
 */
internal fun buildGrid(latitudes: DoubleArray, longitudes: DoubleArray, measurements: DoubleArray): RasterGrid {
    val density = measurements.count { !it.isNaN() }.toDouble() / measurements.size
    return if (density < SPARSE_DENSITY_THRESHOLD) {
        MapRasterGrid(latitudes, longitudes, measurements)
    } else {
        ArrayRasterGrid(latitudes, longitudes, measurements)
    }
}

/**
 * Selects the variable to read from the dataset.
 *
 * If [name] is provided, looks it up by short name (the name as it appears in the file,
 * not the store catalogue name). Otherwise, auto-detects the unique 3D variable
 * whose dimensions match {[timeDimName], [latDimName], [lonDimName]}.
 * Coordinate axis variables (latitude, longitude, time themselves) are 1D and are therefore excluded
 * automatically.
 *
 * @param ds the open enhanced dataset.
 * @param name explicit variable name, or null for auto-detection.
 * @param timeDimName name of the time dimension (from the time axis).
 * @param latDimName name of the latitude dimension (from the latitude axis).
 * @param lonDimName name of the longitude dimension (from the longitude axis).
 * @param file path of the file being read (used for error messages).
 * @return the selected [Variable].
 * @throws IllegalArgumentException if the named [Variable] is not found, or if
 * auto-detection finds zero or more than one candidate.
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
 * The file schema extracted by [readFileAxes]: the file's time axis, its normalized spatial
 * axes, the resolved data [variable], and the positions of the time/lat/lon dimensions within it.
 */
internal class FileAxes(
    val timeAxis: CoordinateAxis1DTime,
    val latitudes: DoubleArray,
    val longitudes: DoubleArray,
    val latDescending: Boolean,
    val lonDescending: Boolean,
    val variable: Variable,
    val timePosition: Int,
    val latPosition: Int,
    val lonPosition: Int,
)

/**
 * Represents the spatial grid and variable established by the FIRST file of a homogeneous series (of a dataset).
 *
 * Used to validate that every subsequent file is both spatially and semantically consistent with
 * the first one.
 */
internal class ReferenceGrid(axes: FileAxes) {

    private val latitudes = axes.latitudes
    private val longitudes = axes.longitudes
    private val variableName = axes.variable.shortName

    /**
     * @param axes the schema extracted from another file.
     * @param file the path of that file (used for error messages).
     * @param directory the directory where the file is located (used for error messages).
     * @throws IllegalArgumentException if [axes] does not share this reference's spatial grid
     * or resolved variable name.
     */
    fun requireMatches(axes: FileAxes, file: Path, directory: Path) {
        // files following the first one must have the same spatial coordinates
        require(axes.latitudes.contentEquals(latitudes)) {
            "Latitude axes differ in $file from the others"
        }
        require(axes.longitudes.contentEquals(longitudes)) {
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
 * Returns this array sorted in ascending order, with whether it was originally
 * descending.
 */
private fun DoubleArray.ascendingWithDescendingFlag(): Pair<DoubleArray, Boolean> {
    val descending = first() > last()
    return (if (descending) reversedArray() else this) to descending
}
