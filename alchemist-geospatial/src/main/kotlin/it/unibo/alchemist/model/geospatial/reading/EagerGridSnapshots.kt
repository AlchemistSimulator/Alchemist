/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.reading

import it.unibo.alchemist.model.geospatial.utils.FileAxes
import it.unibo.alchemist.model.geospatial.utils.ReferenceGrid
import it.unibo.alchemist.model.geospatial.utils.buildGrid
import it.unibo.alchemist.model.geospatial.utils.flattenAscending
import it.unibo.alchemist.model.geospatial.utils.listDataFiles
import it.unibo.alchemist.model.geospatial.utils.readFileAxes
import it.unibo.alchemist.model.geospatial.utils.readPermutedSlice
import java.nio.file.Path
import java.util.TreeMap
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import org.slf4j.LoggerFactory
import ucar.nc2.dataset.NetcdfDatasets

/**
 * Eager [GridSnapshots] implementation. This implementation depends on NetCDF-Java.
 *
 * Reads a directory of **homogeneous** data files (same variable, same spatial grid)
 * and exposes them as a single time-ordered slice series.
 * All data is loaded into memory at construction time; file handles are closed before
 * the constructor returns.
 * If two files share the same real-world instants, then only the grid associated with the file
 * having the lower lexicographic order will be evaluated.
 *
 * ### Supported datasets
 * Supports datasets readable by NetCDF-Java whose selected variable is a 2D
 * regular latitude/longitude field associated with one temporal dimension and whose
 * coordinate axes are 1D.
 * The dataset does not need to follow a specific convention (e.g, Climate and Forecast),
 * what matters is that the relevant variable exposes the standard geophysical metadata
 * in a format that NetCDF-Java can interpret.
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
 * @param directory directory of spatially homogeneous data files (NetCDFs/GRIBs).
 * @param variableName name of the variable as it appears in the file (e.g. `"dis24"`),
 * NOT the variable name shown in the Copernicus store. If `null`, auto-detected from the file.
 * @throws IllegalArgumentException if the directory is empty; if the variable is missing
 * or ambiguous; if files have mismatched spatial axes; if the variable dimensions are not
 * `{time, lat, lon}`.
 */
class EagerGridSnapshots(directory: Path, variableName: String? = null) : GridSnapshots {

    override val instants: List<Instant>
    private val grids: List<RasterGrid>

    init {
        // maps all file time instances to the corresponding RasterGrid, sorting them by Instant
        val map = TreeMap<Instant, RasterGrid>()
        // spatial grid and variable are established by the first file, validated against all the others
        var reference: ReferenceGrid? = null
        for (file in listDataFiles(directory)) {
            /*
             * opens the file in "enhanced mode": all fill values are replaced with NaN
             * and expects dimensions to be properly tagged.
             */
            NetcdfDatasets.openDataset(file.toString()).use { ds ->
                val axes = readFileAxes(ds, variableName, file)
                reference = reference?.also { it.requireMatches(axes, file, directory) } ?: ReferenceGrid(axes)
                readTimestepsFrom(map, axes, directory)
            }
        }
        instants = map.keys.toList()
        grids = map.values.toList()
    }

    /**
     * Reads every real-world instant of [axes] and inserts the resulting [RasterGrid] slices into [map],
     * keyed by their real-world [Instant].
     *
     * @param map the map, **shared** across all files in [directory].
     * @param axes the schema of the file currently being read.
     * @param directory the directory of the file (used for error messages).
     * @throws IllegalArgumentException if a timestamp already present in [map] is found again.
     */
    private fun readTimestepsFrom(map: TreeMap<Instant, RasterGrid>, axes: FileAxes, directory: Path) {
        val nLat = axes.latitudes.size
        val nLon = axes.longitudes.size
        for (t in 0 until axes.timeAxis.size.toInt()) {
            // converts a CalendarDate to an Instant
            val instant = axes.timeAxis.getCalendarDate(t).toDate().toInstant().toKotlinInstant()
            // there are duplicate timestamps in different files, only the first one is preserved
            if (!map.containsKey(instant)) {
                val slice = readPermutedSlice(axes, t, nLat, nLon)
                val measurements = flattenAscending(slice, nLat, nLon, axes.latDescending, axes.lonDescending)
                map[instant] = buildGrid(axes.latitudes, axes.longitudes, measurements)
            } else {
                logger.warn(
                    "Two different files in $directory share the same real-world instant ($instant)." +
                        "Ignoring the last one.",
                )
            }
        }
    }

    override fun grid(index: Int): RasterGrid = grids[index]

    private companion object {
        private const val serialVersionUID = 1L
        private val logger = LoggerFactory.getLogger(EagerGridSnapshots::class.java)
    }
}
