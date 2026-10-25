/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps

import it.unibo.alchemist.boundary.FileFormatException
import it.unibo.alchemist.model.maps.GPSTrace
import java.io.IOException
import java.net.URL
import kotlinx.collections.immutable.ImmutableSet

/**
 * Strategy to read [GPSTrace]s from file.
 */
interface GPSFileLoader {
    /**
     * Returns all the file extensions supported by this loader.
     */
    val supportedExtensions: ImmutableSet<String>

    /**
     * Reads all the [GPSTrace]s contained in the file at the given [url].
     *
     * @throws IOException in case of I/O errors
     * @throws FileFormatException if the content is not in a format supported by this loader
     */
    fun readTrace(url: URL): List<GPSTrace>
}
