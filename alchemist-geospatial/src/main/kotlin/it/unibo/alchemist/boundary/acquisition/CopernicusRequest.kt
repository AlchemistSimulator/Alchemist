/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

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
 * @throws IllegalArgumentException if the [endpoint] does not represent a valid Copernicus endpoint
 * or the loopback address.
 */
data class CopernicusRequest(val endpoint: String, val dataset: String, val inputs: Map<String, Any>) : CacheKey {

    init {
        URI.create(endpoint).also {
            require(
                it.isAbsolute &&
                    it.scheme in setOf("http", "https") &&
                    (
                        endpoint.endsWith(COPERNICUS_STORES_ENDPOINT_END) ||
                            it.host == "127.0.0.1"
                        ),
            ) {
                "The endpoint must be an absolute http/https URL ending with " +
                    "'$COPERNICUS_STORES_ENDPOINT_END' or point to 127.0.0.1, but was '$endpoint'"
            }
        }
    }

    /**
     * @return a deterministic directory name: a human-readable sanitized prefix from [dataset],
     * followed by a truncation of the SHA-256 hash of the canonical `(dataset, inputs)` pair. The
     * prefix aids the human reading the cache directory; the hash provides collision resistance.
     */
    override fun toDirectoryName(): String {
        val canonical = CanonicalJson.encode(
            mapOf(
                "endpoint" to endpoint,
                "dataset" to dataset,
                "inputs" to inputs,
            ),
        )
        return "${dataset.toFileSystemSafe()}_${sha256Hex(canonical).take(HASH_PREFIX_LENGTH)}"
    }

    private companion object {

        /**
         * Number of leading hex characters of the SHA-256 digest kept in the folder name.
         * Used to provide collision-safety for a personal cache while keeping the name short.
         */
        private const val HASH_PREFIX_LENGTH = 16

        /**
         * How Copernicus data store endpoints must end.
         */
        private const val COPERNICUS_STORES_ENDPOINT_END = ".copernicus.eu/api"

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
