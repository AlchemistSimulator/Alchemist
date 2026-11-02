/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.reading

import it.unibo.alchemist.model.geogrid.utils.FileAxes
import it.unibo.alchemist.model.geogrid.utils.ReferenceGrid
import it.unibo.alchemist.model.geogrid.utils.buildGrid
import it.unibo.alchemist.model.geogrid.utils.flattenAscending
import it.unibo.alchemist.model.geogrid.utils.listDataFiles
import it.unibo.alchemist.model.geogrid.utils.openNetcdfDataset
import it.unibo.alchemist.model.geogrid.utils.readFileAxes
import it.unibo.alchemist.model.geogrid.utils.readPermutedSlice
import java.nio.file.Path
import java.util.TreeMap
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import org.slf4j.LoggerFactory

/**
 * [GridSnapshots] read at construction from a [directory] of NetCDF or GRIB files sharing the same variable and
 * latitude/longitude grid.
 * Each file must hold a `{time, latitude, longitude}` variable, with its dimensions in any order;
 * axes stored in descending order are reversed, and missing values become [Double.NaN].
 * When files share an instant, the first file in lexicographic order wins.
 *
 * @param directory the directory holding the data files.
 * @param variableName the name of the variable inside the files (e.g., `dis24`), auto-detected when `null`.
 * @throws IllegalArgumentException if the directory has no data files, if the variable is missing or ambiguous,
 * or if the files disagree on their grid or variable.
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
            openNetcdfDataset(file).use { ds ->
                val axes = readFileAxes(ds, variableName, file)
                reference = reference?.also { it.requireMatches(axes, file, directory) } ?: ReferenceGrid(axes)
                readTimestepsFrom(map, axes, directory)
            }
        }
        instants = map.keys.toList()
        grids = map.values.toList()
    }

    /**
     * Adds to [map] the grid of every instant of the file described by [axes], unless [map] already has one.
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
                        "Duplicate istants are ignored.",
                )
            }
        }
    }

    override fun grid(index: Int): RasterGrid = grids[index]

    private companion object {
        private val logger = LoggerFactory.getLogger(EagerGridSnapshots::class.java)
    }
}
