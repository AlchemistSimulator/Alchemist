/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.snapshots

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.Strictness
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import it.unibo.alchemist.boundary.copernicus.CanonicalJson
import it.unibo.alchemist.boundary.copernicus.CopernicusDataStoreProvider
import it.unibo.alchemist.boundary.copernicus.CopernicusRequest
import it.unibo.alchemist.boundary.filecache.FileSystemDirectoryCache
import it.unibo.alchemist.model.geogrid.GridSnapshots
import it.unibo.alchemist.model.geogrid.layers.GriddedGeoLayer
import it.unibo.alchemist.util.KClasses.nonNullSimpleName
import java.net.URL
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import kotlin.time.Duration
import org.kaikikm.threadresloader.ResourceLoader

/**
 * The data of a [GriddedGeoLayer], downloaded at construction from a Copernicus data store,
 * unless already in the local cache.
 * See [GridsFromDirectory] for data already available locally.
 *
 * @param endpoint the URL of the API of the data store (e.g., `https://ewds.climate.copernicus.eu/api`).
 * @param dataset the dataset (e.g., `cems-glofas-historical`).
 * @param inputs what to download (variables, dates, area...), as a JSON object: either a mapping,
 * a JSON file (among the classpath resources or, if not there, on the file system), or a JSON string.
 * Requests with a `type` field need a file or a string, as the Alchemist loader reserves `type` in mappings.
 * @param variable the name of the variable inside the downloaded files, auto-detected when `null`.
 * @param cacheDirectory the directory of the cache.
 * @param cdsApiRcFile the `.cdsapirc` file with the API token, read only when downloading.
 * @param providerTimeout how long the data store can take to prepare the data, as an ISO-8601 duration.
 * @throws IllegalArgumentException if a parameter is not valid, including [inputs] not holding a JSON object.
 * @throws IllegalStateException if the token cannot be read, or the download fails.
 */
class CopernicusData @JvmOverloads constructor(
    endpoint: String,
    dataset: String,
    inputs: Any,
    variable: String? = null,
    cacheDirectory: String = DEFAULT_CACHE_DIRECTORY.toString(),
    cdsApiRcFile: String = DEFAULT_CDSAPIRC_LOCATION.toString(),
    providerTimeout: String = CopernicusDataStoreProvider.DEFAULT_TIMEOUT.toIsoString(),
) : GridSnapshots by GridsFromDirectory(
    download(
        CopernicusRequest(
            endpoint.trim().trimEnd('/'),
            dataset,
            inputs.toRequestInputs(),
        ),
        Path.of(cacheDirectory),
        Path.of(cdsApiRcFile),
        Duration.parseIsoString(providerTimeout),
    ).toString(),
    variable,
) {

    /**
     * Default locations, and readers of the inputs and of the token.
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

        // strict when reading text, and keeping integers integral when building the request
        private val gson: Gson = GsonBuilder()
            .setStrictness(Strictness.STRICT)
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create()

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
         * The token in the `key:` line of this [`.cdsapirc`](https://cds.climate.copernicus.eu/how-to-api) file.
         *
         * @throws IllegalStateException if the file does not exist, has no `key:` line, or its key is empty.
         */
        private fun Path.readToken(): String {
            check(isRegularFile()) { "No .cdsapirc file at $this: create one with the API key of your account" }
            return readLines()
                .map { it.split(':', limit = 2) }
                .firstOrNull { it.size == 2 && it[0].trim() == KEY_FIELD }
                ?.get(1)
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: error("No non-empty 'key:' line in $this")
        }

        /**
         * These inputs of a request as a JSON object, with nested objects as [Map]s and arrays as [List]s.
         * They can be a [Map], a JSON file among the classpath resources or on the file system, or a JSON string.
         *
         * @throws IllegalArgumentException if these inputs are none of them, or do not hold a JSON object.
         */
        private fun Any.toRequestInputs(): Map<String, Any> {
            val file = (this as? String)?.toExistingFile()
            val (source, json) = when {
                file != null -> "File $file" to file.readText().parseJson { "File $file does not hold valid JSON" }
                this is String ->
                    "String '$this', not an existing file," to
                        parseJson { "'$this' is neither an existing file nor a valid JSON string" }
                this is Map<*, *> -> "Mapping $this" to CanonicalJson.toJson(this, "inputs")
                else -> throw IllegalArgumentException(
                    "Inputs must be a Map, a JSON file, or a JSON string, but are a ${
                        this::class.nonNullSimpleName
                    }: $this",
                )
            }
            require(json.isJsonObject) { "$source holds valid JSON, but not a JSON object: $json" }
            return gson.fromJson(json, mapType)
        }

        /**
         * This string as JSON, or a failure with the message [failure] if it is not valid JSON.
         */
        private inline fun String.parseJson(failure: () -> String): JsonElement = try {
            // null for an empty string, which is no JSON at all
            gson.fromJson(this, JsonElement::class.java)
        } catch (e: JsonSyntaxException) {
            throw IllegalArgumentException("${failure()}: ${e.message}", e)
        }.let { requireNotNull(it) { "${failure()}: no content" } }

        /**
         * The file among the classpath resources with this name or, if there is none, at this path;
         * `null` if there is no such file.
         */
        private fun String.toExistingFile(): URL? = takeIf { it.isNotBlank() }?.let { name ->
            ResourceLoader.getResource(name)
                // directories on the classpath are not files
                ?.takeUnless { it.protocol == "file" && !Path.of(it.toURI()).isRegularFile() }
                ?: try {
                    Path.of(name).takeIf { it.isRegularFile() }?.toUri()?.toURL()
                } catch (ignored: InvalidPathException) {
                    // e.g., a JSON string on Windows, where quotes and colons cannot be in paths
                    null
                }
        }
    }
}
