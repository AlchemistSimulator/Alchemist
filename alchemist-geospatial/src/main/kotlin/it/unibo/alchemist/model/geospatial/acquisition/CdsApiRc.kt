/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads **only the token** from a `.cdsapirc` file (the format used by ECMWF's official client,
 * with `url:` and `key:` lines) See [how-to-api](https://cds.climate.copernicus.eu/how-to-api).
 *
 * Only `key` is read, because the base URL is a layer parameter
 * (`endpoint`): a single `.cdsapirc` has one `url` and cannot serve two data stores (CDS and EWDS),
 * whereas the **same token** (unified ECMWF identity) is valid on both.
 */
internal object CdsApiRc {

    private const val KEY_FIELD = "key"

    /**
     * Extracts the token from the `key:` line of the file. Each line is split on its **first** `:`,
     * so any `:` inside the token itself is preserved.
     *
     * @param path path to a `.cdsapirc`-formatted file (already resolved).
     * @return the token to send in the `PRIVATE-TOKEN` header.
     * @throws IllegalStateException if the file has no `key:` line.
     */
    fun readToken(path: Path): String = Files.readAllLines(path).firstNotNullOfOrNull { line ->
        val separator = line.indexOf(':')
        if (separator > 0 && line.take(separator).trim() == KEY_FIELD) {
            line.substring(separator + 1).trim()
        } else {
            null
        }
    } ?: error("No 'key:' line in $path")
}
