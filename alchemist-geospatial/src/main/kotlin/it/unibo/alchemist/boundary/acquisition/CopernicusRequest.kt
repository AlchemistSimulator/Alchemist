/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import it.unibo.alchemist.boundary.filecache.CachedDirectoryKey
import it.unibo.alchemist.boundary.utils.CanonicalJson
import java.net.URI
import java.security.MessageDigest

/**
 * A request to an ECMWF api-friendly datastore (CDS / EWDS / ADS): carries the request **identity** and **what to
 * download**.
 *
 * @property dataset dataset identifier (e.g. `"cems-glofas-historical"`).
 * @property inputs the **opaque** request map: the selection (variables, dates, area, format, ...)
 * of parameters used for the request to the datastore. It is intentionally untyped because the fields
 * vary widely per dataset.
 * @throws IllegalArgumentException if the [endpoint] is neither the `https` API of a Copernicus data store
 * (`https://<store>.copernicus.eu/api`) nor an address of the loopback interface `127.0.0.1`, or has a query,
 * a fragment, or user information, or if the [dataset] is not a single path segment made of letters, digits,
 * `.`, `_`, and `-`, starting with a letter or a digit.
 */
data class CopernicusRequest(val endpoint: String, val dataset: String, val inputs: Map<String, Any>) :
    CachedDirectoryKey {

    init {
        // the parsed parts get checked, as the token is sent to the endpoint
        val uri = URI.create(endpoint)
        val isCopernicusStore = uri.scheme == "https" &&
            uri.host?.endsWith(COPERNICUS_HOST_SUFFIX) == true &&
            uri.path == COPERNICUS_API_PATH
        val isLoopback = uri.scheme in setOf("http", "https") && uri.host == LOOPBACK_ADDRESS
        require(
            (isCopernicusStore || isLoopback) &&
                uri.rawQuery == null &&
                uri.rawFragment == null &&
                uri.rawUserInfo == null,
        ) {
            "The endpoint must be https://<store>$COPERNICUS_HOST_SUFFIX$COPERNICUS_API_PATH or point to " +
                "$LOOPBACK_ADDRESS, with no query, fragment, or user information, but was '$endpoint'"
        }
        // the dataset becomes a segment of the path of the requests
        require(SAFE_DATASET.matches(dataset)) {
            "The dataset must be made of letters, digits, '.', '_', and '-', starting with a letter or a digit, " +
                "but was '$dataset'"
        }
    }

    /**
     * A deterministic directory name: a human-readable sanitized prefix from [dataset],
     * followed by a truncation of the SHA-256 hash of the canonical `(dataset, inputs)` pair. The
     * prefix aids the human reading the cache directory; the hash provides collision resistance.
     */
    override val directoryName: String = run {
        val canonical = CanonicalJson.encode(
            mapOf(
                "endpoint" to endpoint,
                "dataset" to dataset,
                "inputs" to inputs,
            ),
        )
        "${dataset.toFileSystemSafe()}_${sha256Hex(canonical).take(HASH_PREFIX_LENGTH)}"
    }

    private companion object {

        /**
         * Number of leading hex characters of the SHA-256 digest kept in the folder name.
         * Used to provide collision-safety for a personal cache while keeping the name short.
         */
        private const val HASH_PREFIX_LENGTH = 16

        /**
         * The path of the API of the Copernicus data stores.
         */
        private const val COPERNICUS_API_PATH = "/api"

        /**
         * How the hosts of the Copernicus data stores end.
         */
        private const val COPERNICUS_HOST_SUFFIX = ".copernicus.eu"

        /**
         * The loopback address, accepted with any path for testing.
         */
        private const val LOOPBACK_ADDRESS = "127.0.0.1"

        /**
         * The dataset identifiers accepted, safe as segments of a URL path.
         */
        private val SAFE_DATASET = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

        /**
         * Returns the SHA-256 digest of [str] (UTF-8) as a lowercase hex string.
         *
         * @param str the UTF-8 string to hash.
         * @return the digest of SHA-256 over [str].
         */
        private fun sha256Hex(str: String): String = MessageDigest.getInstance("SHA-256")
            .digest(str.toByteArray(Charsets.UTF_8))
            .toHexString()
    }
}

/**
 * Renders this `String` as a single file-system-safe path segment: every character outside `[A-Za-z0-9._-]`
 * is replaced with `_`. Distinct strings may collapse to the same output (e.g. `"a b"` and `"a_b"`).
 *
 * @return this string sanitized (i.e. all non-alphabetical/numerical characters replaced by `_`)
 */
internal fun String.toFileSystemSafe(): String = this.replace(Regex("[^A-Za-z0-9._-]"), "_")
