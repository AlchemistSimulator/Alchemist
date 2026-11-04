/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.snapshots

import it.unibo.alchemist.model.geogrid.Axis
import it.unibo.alchemist.model.geogrid.GridSnapshots
import it.unibo.alchemist.model.geogrid.RasterGrid
import it.unibo.alchemist.model.geogrid.axes.ImmutableAxis
import it.unibo.alchemist.model.geogrid.rastergrids.ArrayRasterGrid
import it.unibo.alchemist.model.geogrid.rastergrids.MapRasterGrid
import java.nio.file.Files
import java.nio.file.Path
import java.util.Formatter
import java.util.TreeMap
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import org.kaikikm.threadresloader.ResourceLoader
import org.slf4j.LoggerFactory
import ucar.nc2.Variable
import ucar.nc2.constants.AxisType
import ucar.nc2.dataset.CoordinateAxis
import ucar.nc2.dataset.CoordinateAxis1D
import ucar.nc2.dataset.CoordinateAxis1DTime
import ucar.nc2.dataset.NetcdfDataset
import ucar.nc2.dataset.NetcdfDatasets

/**
 * [GridSnapshots] read at construction from a [directory] of NetCDF or GRIB files sharing the same variable and
 * latitude/longitude grid.
 * Each file must hold a `{time, latitude, longitude}` variable, with its dimensions in any order;
 * axes stored in descending order are reversed, and missing values become [Double.NaN].
 * When files share an instant, the first file in lexicographic order wins.
 *
 * @param directory the directory holding the data files, among the classpath resources or, if not there,
 * on the file system.
 * @param variable the name of the variable inside the files (e.g., `dis24`), auto-detected when `null`.
 * @throws IllegalArgumentException if the directory does not exist or has no data files, if the variable is missing
 * or ambiguous, or if the files disagree on their grid or variable.
 */
class GridsFromDirectory @JvmOverloads constructor(directory: String, variable: String? = null) : GridSnapshots {

    override val instants: List<Instant>
    private val grids: List<RasterGrid>

    init {
        // maps all file time instances to the corresponding RasterGrid, sorting them by Instant
        val map = TreeMap<Instant, RasterGrid>()
        // spatial grid and variable are established by the first file, validated against all the others
        var reference: DataFile? = null
        for (file in directory.toDirectory().dataFiles()) {
            file.openDataset().use { dataset ->
                val data = DataFile(dataset, variable)
                reference?.let(data::requireSameGridAs)
                reference = reference ?: data
                data.instants.forEachIndexed { t, instant ->
                    // there are duplicate timestamps in different files, only the first one is preserved
                    if (instant in map) {
                        logger.warn("Ignoring instant $instant of $file, already read from another file of $directory")
                    } else {
                        map[instant] = data.gridAt(t)
                    }
                }
            }
        }
        instants = map.keys.toList()
        grids = map.values.toList()
    }

    override fun grid(index: Int): RasterGrid = grids[index]

    /**
     * A data file, open as [dataset], with a variable over time, latitude, and longitude:
     * the one named [variableName] or, when `null`, the only one over these dimensions.
     *
     * @throws IllegalArgumentException if an axis is missing or not one-dimensional, or if the variable is missing,
     * ambiguous, or not over time, latitude, and longitude.
     */
    private class DataFile(private val dataset: NetcdfDataset, variableName: String?) {

        private val location: String = dataset.location

        private val timeAxis: CoordinateAxis = requireNotNull(
            dataset.findCoordinateAxis(AxisType.Time) ?: dataset.findCoordinateAxis(AxisType.RunTime),
        ) { "No time axis in $location" }

        private val latitudeAxis: CoordinateAxis1D = requireNotNull(
            dataset.findCoordinateAxis(AxisType.Lat) as? CoordinateAxis1D,
        ) { "No 1D latitude axis in $location" }

        private val longitudeAxis: CoordinateAxis1D = requireNotNull(
            dataset.findCoordinateAxis(AxisType.Lon) as? CoordinateAxis1D,
        ) { "No 1D longitude axis in $location" }

        private val latitudesDescending: Boolean = latitudeAxis.coordValues.let { it.first() > it.last() }

        private val longitudesDescending: Boolean = longitudeAxis.coordValues.let { it.first() > it.last() }

        /**
         * The names of the time, latitude, and longitude dimensions.
         */
        private val dimensions: List<String> = listOf(timeAxis, latitudeAxis, longitudeAxis)
            .map { it.dimensions.first().name }

        private val variable: Variable = resolveVariable(variableName)

        /**
         * The position in [variable] of the time, latitude, and longitude dimensions.
         */
        private val positions: IntArray = variable.dimensions.map { it.name }.let { actual ->
            require(actual.toSet() == dimensions.toSet()) {
                "Variable '${variable.shortName}' in $location has dimensions $actual, " +
                    "but ${dimensions.toSet()} was expected (in any order)."
            }
            dimensions.map(actual::indexOf).toIntArray()
        }

        /**
         * The instants of the data.
         */
        val instants: List<Instant> = Formatter().let { errors ->
            requireNotNull(CoordinateAxis1DTime.factory(dataset, timeAxis, errors)) {
                "Cannot build time axis in $location: $errors"
            }
        }.calendarDates.map { it.toDate().toInstant().toKotlinInstant() }

        /**
         * The latitudes, sorted ascending.
         */
        val latitudes: Axis = ImmutableAxis(
            latitudeAxis.coordValues.let { if (latitudesDescending) it.reversedArray() else it },
        )

        /**
         * The longitudes, sorted ascending.
         */
        val longitudes: Axis = ImmutableAxis(
            longitudeAxis.coordValues.let { if (longitudesDescending) it.reversedArray() else it },
        )

        /**
         * The name of the variable.
         */
        val variableName: String = variable.shortName

        /**
         * The grid at the [t]-th instant: a [MapRasterGrid] if its values are mostly missing,
         * an [ArrayRasterGrid] otherwise.
         */
        fun gridAt(t: Int): RasterGrid {
            val sizes = intArrayOf(1, latitudes.size, longitudes.size)
            val origin = IntArray(sizes.size).also { it[positions.first()] = t }
            val shape = IntArray(sizes.size).also { shape ->
                positions.forEachIndexed { dimension, position -> shape[position] = sizes[dimension] }
            }
            // reorders the slice to (time, latitude, longitude)
            val slice = variable.read(origin, shape).permute(positions).copy()
            val (_, nLat, nLon) = sizes
            val values = DoubleArray(nLat * nLon) { index ->
                val latitude = (index / nLon).let { if (latitudesDescending) nLat - 1 - it else it }
                val longitude = (index % nLon).let { if (longitudesDescending) nLon - 1 - it else it }
                slice.getDouble(latitude * nLon + longitude)
            }
            val density = values.count { !it.isNaN() }.toDouble() / values.size
            return if (density < SPARSE_DENSITY_THRESHOLD) {
                MapRasterGrid(latitudes, longitudes, values)
            } else {
                ArrayRasterGrid(latitudes, longitudes, values)
            }
        }

        /**
         * Checks that this file has the same grid and variable as [reference].
         *
         * @throws IllegalArgumentException otherwise.
         */
        fun requireSameGridAs(reference: DataFile) {
            require(latitudes == reference.latitudes) { "Latitude axes differ in $location from the others" }
            require(longitudes == reference.longitudes) { "Longitude axes differ in $location from the others" }
            require(variableName == reference.variableName) {
                "Variable name differs across files: '${reference.variableName}' vs '$variableName' in $location. " +
                    "All files must contain the same variable."
            }
        }

        private fun resolveVariable(name: String?): Variable = when (name) {
            null -> {
                val candidates = dataset.variables.filter { variable ->
                    variable.dimensions.size == dimensions.size &&
                        variable.dimensions.map { it.name }.toSet() == dimensions.toSet()
                }
                require(candidates.isNotEmpty()) { "No variable with dimensions $dimensions found in $location" }
                require(candidates.size == 1) {
                    "Multiple candidate variables with dimensions $dimensions in $location. " +
                        "The variables that can be used are: ${candidates.map { it.shortName }}. " +
                        "Specify the variable explicitly."
                }
                candidates.single()
            }
            else -> requireNotNull(dataset.findVariable(name)) {
                "Variable '$name' not found in $location. Available: ${dataset.variables.map { it.shortName }}"
            }
        }
    }

    private companion object {
        /**
         * The system property selecting the XML parser, which must be the JDK's one for netCDF-Java to read GRIB files.
         */
        private const val SAX_PARSER_FACTORY_KEY = "javax.xml.parsers.SAXParserFactory"
        private const val JDK_XERCES_SAX_PARSER_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"

        /**
         * The fraction of non-missing values below which [DataFile.gridAt] builds a sparse grid.
         */
        private const val SPARSE_DENSITY_THRESHOLD = 0.1

        private val logger = LoggerFactory.getLogger(GridsFromDirectory::class.java)

        /**
         * Guards the [SAX_PARSER_FACTORY_KEY] property, which [openDataset] changes for the whole JVM.
         */
        private val saxParserFactoryLock = Any()

        /**
         * The suffixes of the index files that netCDF-Java writes next to GRIB files.
         */
        private val netcdfJavaIndexSuffixes = setOf(".gbx9", ".ncx4")

        /**
         * The data files in this directory, sorted, skipping the indexes written by netCDF-Java.
         *
         * @throws IllegalArgumentException if there are none.
         */
        private fun Path.dataFiles(): List<Path> {
            val files = Files.list(this).use { stream ->
                stream.filter { Files.isRegularFile(it) }
                    .filter { file -> netcdfJavaIndexSuffixes.none { file.toString().endsWith(it) } }
                    .sorted()
                    .toList()
            }
            require(files.isNotEmpty()) { "No data files in $this" }
            return files
        }

        /**
         * The directory among the classpath resources with this name or, if there is none, at this path.
         *
         * @throws IllegalArgumentException if the resource is not a directory on the file system, e.g., inside a jar,
         * as netCDF-Java reads only files.
         */
        private fun String.toDirectory(): Path = ResourceLoader.getResource(this)?.let { resource ->
            require(resource.protocol == "file") {
                "The classpath resource '$this' is at $resource, but data files must be on the file system"
            }
            Path.of(resource.toURI())
        } ?: Path.of(this)

        /**
         * Opens this file, with missing values as [Double.NaN].
         * NetCDF-Java reads GRIB files only with the JDK's XML parser, which is selected meanwhile,
         * and then the previous one is restored.
         * Files are opened one at a time, so that simulations running in parallel do not restore the parser
         * while another one is still opening a file.
         */
        private fun Path.openDataset(): NetcdfDataset = synchronized(saxParserFactoryLock) {
            val previous = System.getProperty(SAX_PARSER_FACTORY_KEY)
            System.setProperty(SAX_PARSER_FACTORY_KEY, JDK_XERCES_SAX_PARSER_FACTORY)
            try {
                NetcdfDatasets.openDataset(toString())
            } finally {
                if (previous == null) {
                    System.clearProperty(SAX_PARSER_FACTORY_KEY)
                } else {
                    System.setProperty(SAX_PARSER_FACTORY_KEY, previous)
                }
            }
        }
    }
}
