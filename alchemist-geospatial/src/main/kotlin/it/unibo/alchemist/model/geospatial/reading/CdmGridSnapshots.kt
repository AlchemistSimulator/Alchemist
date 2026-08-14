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
import java.util.TreeMap
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import ucar.nc2.Variable
import ucar.nc2.constants.AxisType
import ucar.nc2.dataset.CoordinateAxis1D
import ucar.nc2.dataset.CoordinateAxis1DTime
import ucar.nc2.dataset.NetcdfDataset
import ucar.nc2.dataset.NetcdfDatasets

/**
 * Eager [GridSnapshots] implementation. This implementation depends on
 * [NetCDF-Java](https://docs.unidata.ucar.edu/netcdf-java/current/javadoc/index.html).
 *
 * Reads a directory of **homogeneous** data files (same variable, same spatial grid,
 * disjoint temporal coverage) and exposes them as a single time-ordered slice series.
 * All data is loaded into memory at construction time; file handles are closed before
 * the constructor returns.
 *
 * ### Supported datasets
 * Supports datasets readable by NetCDF-Java whose selected variable is a 3D
 * regular latitude/longitude field with one temporal dimension and whose
 * coordinate axes are 1D.
 * The dataset does not need to follow a specific convention, what matters is that the
 * relevant variable exposes the standard geophysical metadata `units` (e.g,
 * `degree_north`/`degree_east`) on which NetCDF relies to interpret the
 * axes as time, latitude, and longitude
 *
 * ### Grid normalization
 * Latitude and longitude axes are normalized to ascending order regardless of the direction
 * stored in the file. Spatial homogeneity across files is validated eagerly.
 *
 * ### Dimension order
 * The selected variable's three dimensions may appear in **any** order in the files:
 * they are matched by name against the detected time/latitude/longitude axes and reordered
 * internally (via [ucar.ma2.Array.permute]). What is **not** tolerated is a variable with
 * more or fewer than these three dimensions.
 *
 * ### Data representation
 * The floating-point values that are read are represented as [Double] and missing/fill
 * values are replaced by [Double.NaN].
 *
 * The variable to read is selected by [variableName], or auto-detected as the unique
 * `{time, lat, lon}` variable in the file.
 *
 * @param directory directory of homogeneous spatial data files (NetCDFs/GRIBs).
 * @param variableName name of the variable as it appears in the file (e.g. `"dis24"`),
 * NOT the variable name shown in the Copernicus store. If `null`, auto-detected from the file.
 *
 * @throws IllegalArgumentException if the directory is empty; if the variable is missing
 * or ambiguous; if files have mismatched spatial axes; if the variable dimensions are not
 * `{time, lat, lon}`; or if two files share a timestamp (i.e. the temporal coverages are not disjoint).
 */
class CdmGridSnapshots(directory: Path, variableName: String? = null) : GridSnapshots {

    override val instants: List<Instant>
    private val grids: List<RasterGrid>

    init {
        // lists the files in the directory, which must not be empty
        val files = Files.list(directory).use { stream ->
            stream.filter { Files.isRegularFile(it) }
                .sorted()
                .toList()
        }
        require(files.isNotEmpty()) { "No data files in $directory" }

        // maps all file time instances to the corresponding RasterGrid, sorting them by Instant
        val map = TreeMap<Instant, RasterGrid>()

        /**
         * spatial reference established by the first file.
         * All subsequent files must have the same coordinates, same grid,
         * same number of points.
         */
        var refLats: DoubleArray? = null
        var refLons: DoubleArray? = null
        var refVariableName: String? = null

        for (file in files) {
            /*
             * opens the file in "enhanced mode": all fill values are replaced with NaN
             * and expects dimensions to be properly tagged.
             */
            NetcdfDatasets.openDataset(file.toString()).use { ds ->
                // ensures that all axes are present (time, lat, lon)
                val rawTimeAxis = requireNotNull(ds.findCoordinateAxis(AxisType.Time)) {
                    "No time axis in $file"
                }
                val latAxis = requireNotNull(ds.findCoordinateAxis(AxisType.Lat) as? CoordinateAxis1D) {
                    "No 1D latitude axis in $file"
                }
                val lonAxis = requireNotNull(ds.findCoordinateAxis(AxisType.Lon) as? CoordinateAxis1D) {
                    "No 1D longitude axis in $file"
                }
                val errMsg = Formatter()

                // constructs a CF-aware time axis
                val timeAxis = requireNotNull(CoordinateAxis1DTime.factory(ds, rawTimeAxis, errMsg)) {
                    "Cannot build time axis in $file: $errMsg"
                }

                val rawLats: DoubleArray = latAxis.coordValues
                val rawLons: DoubleArray = lonAxis.coordValues

                // determines whether an axis is descending (RasterGrid contract requires them to be).
                val latDesc = rawLats.first() > rawLats.last()
                val lonDesc = rawLons.first() > rawLons.last()
                val lats = if (latDesc) rawLats.reversedArray() else rawLats
                val lons = if (lonDesc) rawLons.reversedArray() else rawLons

                if (refLats == null) {
                    // the first file sets the reference grid
                    refLats = lats
                    refLons = lons
                } else {
                    // subsequent files must have the same spatial coordinates as the first one
                    require(lats.contentEquals(refLats)) {
                        "Latitude axes differ in $file vs previous files"
                    }
                    require(lons.contentEquals(refLons)) {
                        "Longitude axes differ in $file vs previous files"
                    }
                }

                val nLat = lats.size
                val nLon = lons.size

                // derives the dimension names needed to find the variable and to validate its shape
                val timeDimName = rawTimeAxis.dimensions.first().name
                val latDimName = latAxis.dimensions.first().name
                val lonDimName = lonAxis.dimensions.first().name

                /*
                 * finds the variable to read, either by name or by auto-detection
                 * of the only variable whose dimensions are {time, lat, lon}.
                 */
                val variable = resolveVariable(ds, variableName, timeDimName, latDimName, lonDimName, file)

                if (refVariableName == null) {
                    // the first file sets the reference variable
                    refVariableName = variable.shortName
                } else {
                    // subsequent files (relevant mainly for auto-detection) must resolve to the same variable
                    require(variable.shortName == refVariableName) {
                        "Variable name differs across files: '$refVariableName' vs '${variable.shortName}' " +
                            "in $file. All files in $directory must contain the same variable."
                    }
                }

                /*
                 * verifies that the variable has exactly the three expected dimensions, in any order:
                 * CF does not mandate a fixed dimension order, and Copernicus/ECMWF's own documentation
                 * states that dimension order is not guaranteed to be stable across datasets.
                 * An unexpected extra dimension (e.g, z-axis) is NOT tolerated and fails.
                 */
                val actualDims = variable.dimensions.map { it.name }
                val expectedDims = setOf(timeDimName, latDimName, lonDimName)
                require(actualDims.toSet() == expectedDims) {
                    "Variable '${variable.shortName}' in $file has dimensions $actualDims, " +
                        "but $expectedDims was expected (in any order)."
                }

                // positions of each axis within this file's own dimension order
                val timePos = actualDims.indexOf(timeDimName)
                val latPos = actualDims.indexOf(latDimName)
                val lonPos = actualDims.indexOf(lonDimName)

                for (t in 0 until timeAxis.size.toInt()) {
                    // converts a CalendarDate CF-aware to an Instant
                    val instant = (timeAxis.getCalendarDate(t).toDate().toInstant()).toKotlinInstant()

                    /*
                     * if there are duplicate timestamps across different files, then there are overlapping
                     * time ranges in the request (configuration error).
                     */
                    require(!map.containsKey(instant)) {
                        "Duplicate timestamp $instant in $directory | " +
                            "check that files have disjoint temporal coverage."
                    }

                    // origin/shape are positional relative to the variable's own axis order in the file
                    val origin = IntArray(3).also { array ->
                        array[timePos] = t
                        array[latPos] = 0
                        array[lonPos] = 0
                    }
                    val shape = IntArray(3).also { array ->
                        array[timePos] = 1
                        array[latPos] = nLat
                        array[lonPos] = nLon
                    }

                    // reorders the read slice to canonical (time, lat, lon), regardless of read order
                    val orderedData = variable
                        .read(origin, shape)
                        .permute(intArrayOf(timePos, latPos, lonPos))
                        .copy()

                    var nanCount = 0

                    /*
                     * constructs the double array in row-major order, with ascending normalized axes.
                     * If the index was descending, then srcLat/srcLon are reversed so that iLat=0
                     * corresponds to the lowest latitude.
                     */
                    val measurements = DoubleArray(nLat * nLon).also { arr ->
                        for (idx in arr.indices) {
                            val iLat = idx / nLon
                            val iLon = idx % nLon
                            val srcLat = if (latDesc) (nLat - 1 - iLat) else iLat
                            val srcLon = if (lonDesc) (nLon - 1 - iLon) else iLon
                            arr[idx] = orderedData.getDouble(srcLat * nLon + srcLon)

                            if (arr[idx].isNaN()) nanCount++
                        }
                    }

                    /*
                     * computes the fraction of NON-missing cells to pick the most efficient
                     * grid implementation.
                     */
                    val density = (measurements.size - nanCount).toDouble() / measurements.size

                    map[instant] = if (density < SPARSE_DENSITY_THRESHOLD) {
                        MapRasterGrid(lats, lons, measurements)
                    } else {
                        ArrayRasterGrid(lats, lons, measurements)
                    }
                }
            }
        }

        instants = map.keys.toList()
        grids = map.values.toList()
    }

    override fun grid(index: Int): RasterGrid = grids[index]

    /**
     * Default values and helper factory logic for [CdmGridSnapshots].
     */
    companion object {

        /**
         * Below this fraction of non-missing cells, a grid is considered "sparse".
         */
        private const val SPARSE_DENSITY_THRESHOLD = 0.1

        private const val serialVersionUID = 1L

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
         *
         * @throws IllegalArgumentException if the named [Variable] is not found, or if
         * auto-detection finds zero or more than one candidate.
         */
        private fun resolveVariable(
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
                "Multiple candidate variables with dimensions $targetDims in $file: " +
                    "${candidates.map { it.shortName }}. Specify variableName explicitly."
            }

            return candidates.single()
        }
    }
}
