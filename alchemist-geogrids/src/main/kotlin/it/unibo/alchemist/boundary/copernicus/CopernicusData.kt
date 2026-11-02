/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import it.unibo.alchemist.boundary.filecache.FileSystemDirectoryCache
import it.unibo.alchemist.model.geogrid.reading.EagerGridSnapshots
import it.unibo.alchemist.model.geogrid.reading.GridSnapshots
import it.unibo.alchemist.model.geogrid.utils.pathOrClasspathResource
import it.unibo.alchemist.model.layers.GriddedGeoFiles
import it.unibo.alchemist.model.layers.GriddedGeoLayer
import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.time.Duration

/**
 * The data of a [GriddedGeoLayer], downloaded at construction from a Copernicus data store,
 * unless already in the local cache.
 * See [GriddedGeoFiles] for data already available locally.
 *
 * @param endpoint the URL of the API of the data store (e.g., `https://ewds.climate.copernicus.eu/api`).
 * @param dataset the dataset (e.g., `cems-glofas-historical`).
 * @param inputsFile a JSON file, on the file system or among the classpath resources, with what to download
 * (variables, dates, area...). It is a file rather than an inline mapping because some datasets need a `type`
 * field, which the Alchemist loader reserves.
 * @param variable the name of the variable inside the downloaded files, auto-detected when `null`.
 * @param cacheDirectory the directory of the cache.
 * @param cdsApiRcFile the `.cdsapirc` file with the API token, read only when downloading.
 * @param providerTimeout how long the data store can take to prepare the data, as an ISO-8601 duration.
 * @throws IllegalArgumentException if a parameter is not valid, or if [inputsFile] does not exist.
 * @throws IllegalStateException if the token cannot be read, or the download fails.
 */
class CopernicusData @JvmOverloads constructor(
    endpoint: String,
    dataset: String,
    inputsFile: String,
    variable: String? = null,
    cacheDirectory: String = DEFAULT_CACHE_DIRECTORY.toString(),
    cdsApiRcFile: String = DEFAULT_CDSAPIRC_LOCATION.toString(),
    providerTimeout: String = CopernicusDataStoreProvider.DEFAULT_TIMEOUT.toIsoString(),
) : GridSnapshots by EagerGridSnapshots(
    download(
        CopernicusRequest(
            endpoint.trim().trimEnd('/'),
            dataset,
            pathOrClasspathResource(inputsFile).readJsonObject(),
        ),
        Path.of(cacheDirectory),
        Path.of(cdsApiRcFile),
        Duration.parseIsoString(providerTimeout),
    ),
    variable,
) {

    /**
     * Default locations, and readers of the local files.
     */
    companion object {
        private const val KEY_FIELD = "key"

        /**
         * The default directory of the cache (e.g., `~/.cache/alchemist/copernicus` on Linux).
         */
        val DEFAULT_CACHE_DIRECTORY: Path = FileSystemDirectoryCache.DEFAULT_CACHE_DIRECTORY.resolve("copernicus")

        /**
         * The default `.cdsapirc` file, in the user's home directory, as for ECMWF's official client.
         */
        val DEFAULT_CDSAPIRC_LOCATION: Path = Path.of(System.getProperty("user.home"), ".cdsapirc")

        private val gson = Gson()

        private val mapType = object : TypeToken<Map<String, Any>>() {}.type

        /**
         * The directory of the data of [request] in [cacheDirectory], downloading it if missing.
         */
        private fun download(
            request: CopernicusRequest,
            cacheDirectory: Path,
            cdsApiRcFile: Path,
            providerTimeout: Duration,
        ): Path {
            val provider = CopernicusDataStoreProvider(timeout = providerTimeout) {
                cdsApiRcFile.readToken()
            }
            return FileSystemDirectoryCache(provider, cacheDirectory)[request]
        }

        /**
         * This file as a JSON object, with nested objects as [Map]s and arrays as [List]s.
         *
         * @throws JsonSyntaxException if the file does not hold a JSON object.
         */
        internal fun Path.readJsonObject(): Map<String, Any> = gson.fromJson(readText(), mapType)

        /**
         * The token in the `key:` line of this [`.cdsapirc`](https://cds.climate.copernicus.eu/how-to-api) file.
         *
         * @throws IllegalStateException if the file has no `key:` line.
         */
        internal fun Path.readToken(): String = readLines().firstNotNullOfOrNull { line ->
            val separator = line.indexOf(':')
            when {
                separator > 0 && line.take(separator).trim() == KEY_FIELD -> line.substring(separator + 1).trim()
                else -> null
            }
        } ?: error("No 'key:' line in $this")
    }
}
