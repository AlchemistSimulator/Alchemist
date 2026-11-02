/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import it.unibo.alchemist.boundary.filecache.CachedDirectoryKey
import java.net.URI
import java.security.MessageDigest

/**
 * A request of data to a Copernicus data store.
 *
 * @property endpoint the URL of the API of the data store (e.g., `https://ewds.climate.copernicus.eu/api`).
 * @property dataset the dataset (e.g., `cems-glofas-historical`).
 * @property inputs what to download (variables, dates, area, format...), passed to the data store as it is.
 * @throws IllegalArgumentException if [endpoint] is not `https://<store>.copernicus.eu/api` or `127.0.0.1`,
 * or has a query, a fragment, or user information; or if [dataset] is not made of letters, digits, `.`, `_`,
 * and `-`, starting with a letter or a digit.
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
     * The [dataset], followed by a hash of the whole request.
     */
    override val directoryName: String = run {
        val canonical = CanonicalJson.encode(
            mapOf(
                "endpoint" to endpoint,
                "dataset" to dataset,
                "inputs" to inputs,
            ),
        )
        "${dataset}_${sha256Hex(canonical).take(HASH_PREFIX_LENGTH)}"
    }

    private companion object {

        /**
         * How many hexadecimal digits of the hash go in the [directoryName].
         */
        const val HASH_PREFIX_LENGTH = 16

        /**
         * The path of the API of the Copernicus data stores.
         */
        const val COPERNICUS_API_PATH = "/api"

        /**
         * How the hosts of the Copernicus data stores end.
         */
        const val COPERNICUS_HOST_SUFFIX = ".copernicus.eu"

        /**
         * The loopback address, accepted with any path for testing.
         */
        const val LOOPBACK_ADDRESS = "127.0.0.1"

        /**
         * The dataset identifiers accepted, safe as segments of a URL path.
         */
        val SAFE_DATASET = Regex("[A-Za-z0-9][A-Za-z0-9._-]*")

        /**
         * The SHA-256 digest of [str], in lowercase hexadecimal digits.
         */
        fun sha256Hex(str: String): String = MessageDigest.getInstance("SHA-256")
            .digest(str.toByteArray(Charsets.UTF_8))
            .toHexString()
    }
}
