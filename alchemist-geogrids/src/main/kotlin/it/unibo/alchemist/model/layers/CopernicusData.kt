/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.boundary.copernicus.CopernicusDataStoreProvider
import it.unibo.alchemist.boundary.copernicus.CopernicusRequest
import it.unibo.alchemist.boundary.filecache.FileSystemDirectoryCache
import it.unibo.alchemist.boundary.utils.CdsApiRc
import it.unibo.alchemist.boundary.utils.CopernicusInputs
import it.unibo.alchemist.model.geogrid.reading.EagerGridSnapshots
import it.unibo.alchemist.model.geogrid.reading.GridSnapshots
import it.unibo.alchemist.model.geogrid.utils.pathOrClasspathResource
import java.nio.file.Path
import kotlin.time.Duration

/**
 * The data backing a [GriddedGeoLayer], fetched from the Copernicus-family data store (CDS / EWDS / ADS)
 * at [endpoint], authenticating with the token found in [cdsApiRcFile], and cached locally in [cacheDirectory].
 *
 * The data is read **synchronously at construction**:
 * on a cache hit no network call and no credential is needed;
 * on a cache miss, this performs blocking network I/O.
 *
 * @param endpoint base URL of the data store (e.g. `"https://ewds.climate.copernicus.eu/api"`).
 * @param dataset dataset identifier (e.g. `"cems-glofas-historical"`).
 * @param inputsFile path, or name among the classpath resources, of a JSON file holding the opaque request map
 * for [dataset] at [endpoint] (variables, dates, area, type, ...), passed verbatim to the data store.
 * @param variable variable name inside the downloaded files. Auto-detected when `null`.
 * @param cacheDirectory path of the root of the local cache, by default [DEFAULT_CACHE_DIRECTORY].
 * @param cdsApiRcFile path of a `.cdsapirc`-formatted file holding the API token,
 * by default in the user's home directory.
 * @param providerTimeout how long to wait for the data store to prepare the data, as an ISO-8601 duration,
 * by default 30 minutes.
 * @throws IllegalArgumentException if [providerTimeout] is not a valid positive ISO-8601 duration;
 * if [endpoint] or [dataset] are not valid; if any path is malformed;
 * if [inputsFile] is neither an existing path nor a classpath resource.
 * @throws IllegalStateException if the token cannot be read; if the remote job fails or times out;
 * if the downloaded asset fails its integrity check.
 * @see GriddedGeoFiles for data files already available locally.
 */
class CopernicusData @JvmOverloads constructor(
    endpoint: String,
    dataset: String,
    inputsFile: String,
    variable: String? = null,
    cacheDirectory: String = DEFAULT_CACHE_DIRECTORY.toString(),
    cdsApiRcFile: String = CdsApiRc.DEFAULT_LOCATION.toString(),
    providerTimeout: String = CopernicusDataStoreProvider.DEFAULT_TIMEOUT.toIsoString(),
) : GridSnapshots by EagerGridSnapshots(
    download(
        CopernicusRequest(
            endpoint.trim().trimEnd('/'),
            dataset,
            CopernicusInputs.read(pathOrClasspathResource(inputsFile)),
        ),
        Path.of(cacheDirectory),
        Path.of(cdsApiRcFile),
        Duration.parseIsoString(providerTimeout),
    ),
    variable,
) {

    /**
     * Default location of the cache.
     */
    companion object {
        /**
         * The default root of the cache of the downloaded data,
         * in Alchemist's user cache directory (e.g., `~/.cache/alchemist/copernicus` on Linux).
         */
        val DEFAULT_CACHE_DIRECTORY: Path = FileSystemDirectoryCache.DEFAULT_CACHE_DIRECTORY.resolve("copernicus")

        /**
         * Ensures the data denoted by [request] is present in the local cache rooted at [cacheDirectory],
         * downloading it on a cache miss, and returns the directory it lives in.
         * The token is read from [cdsApiRcFile] lazily, so a cache hit needs no credentials.
         */
        private fun download(
            request: CopernicusRequest,
            cacheDirectory: Path,
            cdsApiRcFile: Path,
            providerTimeout: Duration,
        ): Path {
            val provider = CopernicusDataStoreProvider(timeout = providerTimeout) {
                CdsApiRc.readToken(cdsApiRcFile)
            }
            return FileSystemDirectoryCache(provider, cacheDirectory)[request]
        }
    }
}
