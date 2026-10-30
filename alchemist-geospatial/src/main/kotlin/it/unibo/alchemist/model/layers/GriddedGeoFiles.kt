/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.geospatial.reading.EagerGridSnapshots
import it.unibo.alchemist.model.geospatial.reading.GridSnapshots
import it.unibo.alchemist.model.geospatial.utils.pathOrClasspathResource

/**
 * The data backing a [GriddedGeoLayer], read from a local [directory] of NetCDF or GRIB files,
 * from any provider: no network I/O, no cache, no credentials.
 *
 * The data is read **synchronously at construction**.
 *
 * @param directory path of a directory holding one or more homogeneous data files (same
 * variable and spatial grid), or, if no such path exists, name of such a directory among the classpath resources.
 * @param variable variable name inside the files (e.g. `"dis24"`, the GRIB shortName, not the
 * catalogue name). Auto-detected as the unique `(time, lat, lon)` variable when `null`.
 * @throws IllegalArgumentException if [directory] does not hold readable, homogeneous data files,
 * or if it is neither an existing path nor a classpath resource.
 */
class GriddedGeoFiles @JvmOverloads constructor(directory: String, variable: String? = null) :
    GridSnapshots by EagerGridSnapshots(pathOrClasspathResource(directory), variable)
