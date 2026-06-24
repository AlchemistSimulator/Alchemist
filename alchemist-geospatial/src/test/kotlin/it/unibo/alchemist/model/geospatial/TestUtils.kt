/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial

import it.unibo.alchemist.model.geospatial.reading.CdmTimedGrid
import java.nio.file.Path
import ucar.ma2.Array as UcarArray
import ucar.ma2.ArrayDouble
import ucar.ma2.ArrayFloat
import ucar.ma2.DataType
import ucar.nc2.Attribute
import ucar.nc2.write.NetcdfFormatWriter

/**
 * Writes a minimal CF-compliant NetCDF-3 file to [path] for use in geospatial module tests.
 *
 * The file contains:
 * - a `time` axis with units "hours since [timeEpoch]" and values [timeHours].
 * - a `latitude` axis (CF axis Y) with values [lats]. May be descending to test normalization.
 * - a `longitude` axis (CF axis X) with values [lons].
 * - a single data variable named [variableName] with `_FillValue` set to [fillValue].
 *
 * @param path destination file. The parent directory must already exist.
 * @param lats latitude values in degrees, stored in the file as-is. Pass a descending array
 * to test axis normalization in [CdmTimedGrid].
 * @param lons longitude values in degrees, stored in the file as-is.
 * @param timeHours time offsets in hours from [timeEpoch], one value per time step.
 * @param rawValues flat `time * lat * lon` array in row-major order, or `null` to use the
 * default pattern `iLat * 10 + iLon` repeated across all time steps. If provided, its size
 * must equal `timeHours.size * lats.size * lons.size`.
 * @param fillValue written as the `_FillValue` attribute on the data variable. When the file
 * is opened in CDM enhanced mode, cells matching this value are replaced with [Double.NaN].
 * @param variableName short name of the data variable as it appears in the file (e.g. `"dis24"`).
 * @param timeEpoch reference date for the CF `units` attribute, in the format
 * `"yyyy-MM-dd HH:mm"` (e.g. `"2024-01-01 00:00"`). Values in [timeHours] are interpreted
 * as hours elapsed since this date.
 */
internal fun writeTestNetcdf(
    path: Path,
    lats: FloatArray,
    lons: FloatArray,
    timeHours: DoubleArray,
    rawValues: FloatArray? = null,
    fillValue: Float = -9999f,
    variableName: String = "dis24",
    timeEpoch: String = "2024-01-01 00:00",
) {
    // defines the schema on the builder
    val builder = NetcdfFormatWriter.createNewNetcdf3(path.toString())

    builder.addDimension("time", timeHours.size)
    builder.addDimension("latitude", lats.size)
    builder.addDimension("longitude", lons.size)

    // CF compliant axis
    builder.addVariable("time", DataType.DOUBLE, "time").apply {
        addAttribute(Attribute("units", "hours since $timeEpoch"))
        addAttribute(Attribute("calendar", "standard"))
        addAttribute(Attribute("axis", "T"))
    }
    builder.addVariable("latitude", DataType.FLOAT, "latitude").apply {
        addAttribute(Attribute("units", "degrees_north"))
        addAttribute(Attribute("axis", "Y"))
    }
    builder.addVariable("longitude", DataType.FLOAT, "longitude").apply {
        addAttribute(Attribute("units", "degrees_east"))
        addAttribute(Attribute("axis", "X"))
    }
    builder.addVariable(variableName, DataType.FLOAT, "time latitude longitude").apply {
        addAttribute(Attribute("_FillValue", fillValue))
    }

    // creates the file and enters write mode
    builder.build().use { writer ->
        val timeArr = ArrayDouble.D1(timeHours.size)
        timeHours.forEachIndexed { i, v -> timeArr.set(i, v) }
        writer.write("time", timeArr)

        val latArr = ArrayFloat.D1(lats.size)
        lats.forEachIndexed { i, v -> latArr.set(i, v) }
        writer.write("latitude", latArr)

        val lonArr = ArrayFloat.D1(lons.size)
        lons.forEachIndexed { i, v -> lonArr.set(i, v) }
        writer.write("longitude", lonArr)

        val flat = rawValues ?: FloatArray(timeHours.size * lats.size * lons.size) { idx ->
            val iLat = (idx / lons.size) % lats.size
            val iLon = idx % lons.size
            (iLat * 10 + iLon).toFloat()
        }
        writer.write(
            variableName,
            UcarArray.factory(
                DataType.FLOAT,
                intArrayOf(timeHours.size, lats.size, lons.size),
                flat,
            ),
        )
    }
}
