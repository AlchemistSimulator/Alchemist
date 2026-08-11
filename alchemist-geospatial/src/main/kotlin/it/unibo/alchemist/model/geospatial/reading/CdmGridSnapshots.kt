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
 * - **Formats:** supports NetCDF, GRIB1, GRIB2 and any other format recognized by
 * the NetCDF-Java Common Data Model (CDM) that maps to CF conventions.
 * - **CF Compliance**: the implementation relies on the **CF (Climate and Forecast) conventions**.
 * Files must contain properly tagged coordinate axes (recognized as Time, Latitude, and
 * Longitude by the CDM) and parseable CF-compliant time units (e.g., "hours since 1900-01-01"),
 * or use a convention that [ucar.nc2] can convert to the CF convention.
 * - **Dimensions**: strictly limited to {time, lat, lon} data. Datasets
 * with other dimensions (e.g. z-axis) are not supported.
 * - **Data unpacking**: missing/fill values are replaced by `nulls`.
 *
 * ### Grid normalization
 * Latitude and longitude axes are normalized to ascending order regardless of the direction
 * stored in the file (e.g., GloFAS data descending from +89.95 to -59.95 will be reversed
 * internally to fulfill the [RasterGrid] contract). Spatial homogeneity across files is
 * validated eagerly.
 *
 * The variable to read is selected by [variableName], or auto-detected as the unique
 * `(time, lat, lon)` variable in the file.
 *
 * @param directory directory of homogeneous spatial data files (NetCDFs/GRIBs).
 * @param variableName name of the variable as it appears in the file (e.g. `"dis24"`),
 * not the CDS catalogue name. If `null`, auto-detected from the file.
 *
 * @throws IllegalArgumentException if the directory is empty; if the variable is missing
 * or ambiguous; if files have mismatched spatial axes; if the dimension order is not
 * `(time, lat, lon)`; or if two files share a timestamp (i.e. the temporal coverages are not disjoint).
 */
class CdmGridSnapshots(directory: Path, variableName: String? = null) : GridSnapshots {

    override val instants: List<Instant>
    private val grids: List<RasterGrid>

    init {
        // lists the files in the directory, which must not be empty
        val files = Files.list(directory)
            .filter { Files.isRegularFile(it) }
            .sorted()
            .toList()
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

        for (file in files) {
            /*
             * opens the file in "enhanced mode": every fill value get replaced with NaN
             * and expects dimensions to be properly tagged.
             */
            NetcdfDatasets.openDataset(file.toString()).use { ds ->
                // ensures that all axis are present (time, lat, lon)
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

                /*
                 * constructs a CF-aware timeline.
                 * Interprets units as "hours since 1900-0-01" and non-Gregorian calendars.
                 */
                val timeAxis = requireNotNull(CoordinateAxis1DTime.factory(ds, rawTimeAxis, errMsg)) {
                    "Cannot build time axis in $file: $errMsg"
                }

                val rawLats: DoubleArray = latAxis.coordValues
                val rawLons: DoubleArray = lonAxis.coordValues

                /*
                 * determines whether an axis is descending (e.g. GloFAS lat: +89.95 to -59.95).
                 * RasterGrid contract requires them to be ascending.
                 */
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

                // derives the dimension names needed to find the variable and to validate the order of the dimensions
                val timeDimName = rawTimeAxis.dimensions.first().name
                val latDimName = latAxis.dimensions.first().name
                val lonDimName = lonAxis.dimensions.first().name

                /*
                 * finds the variable to read, either by name or by auto-detection
                 * of the only variable with dimensions (time, lat, lon).
                 */
                val variable = resolveVariable(ds, variableName, timeDimName, latDimName, lonDimName, file)

                /*
                 * verifies that the order is (time, lat, lon), as specified by CF convention.
                 * A different order would produce incorrect values.
                 */
                val dimNames = variable.dimensions.map { it.name }
                require(dimNames == listOf(timeDimName, latDimName, lonDimName)) {
                    "Unexpected dimension order in $file: $dimNames. " +
                        "Expected [$timeDimName, $latDimName, $lonDimName] (CF convention)."
                }

                val nTime = timeAxis.size.toInt()
                for (t in 0 until nTime) {
                    // converts a CalendarDate CF-Aware to an Instant
                    val instant = (timeAxis.getCalendarDate(t).toDate().toInstant()).toKotlinInstant()

                    /*
                     * If there are duplicate timestamps across different files, then there are overlapping
                     * time ranges in the cdsRequest. This is a configuration error.
                     */
                    require(!map.containsKey(instant)) {
                        "Duplicate timestamp $instant in $directory | " +
                            "check that files have disjoint temporal coverage"
                    }

                    val rawData = variable.read(
                        // temporal origin
                        intArrayOf(t, 0, 0),
                        // spatial shape
                        intArrayOf(1, nLat, nLon),
                    )

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
                            arr[idx] = rawData.getDouble(srcLat * nLon + srcLon)

                            if (arr[idx].isNaN()) nanCount++
                        }
                    }

                    /*
                     * Computes the fraction of NON-missing cells to pick the most efficient
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

    /**
     * Returns the spatial slice at the given index.
     *
     * @param index 0-based index, aligned with [instants].
     * @return the [RasterGrid] for that instant.
     */
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
         * whose dimensions exactly match ([timeDimName], [latDimName], [lonDimName]).
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
         * @throws IllegalArgumentException if the named variable is not found, or if
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
