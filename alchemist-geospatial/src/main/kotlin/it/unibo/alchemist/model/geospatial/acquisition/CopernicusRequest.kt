/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.security.MessageDigest

/**
 * A request to an ECMWF data store (CDS or EWDS): carries the request **identity** and **what to
 * download**.
 *
 * @property dataset dataset identifier (e.g. `"cems-glofas-historical"`).
 * @property inputs the **opaque** request map: the selection (variables, dates, area, format, ...)
 * of parameters used for the request to the datastore. It is intentionally untyped because the fields
 * vary widely per dataset (ERA5 uses `year/month/day`, GloFAS uses `hyear/hmonth/hday/system_version/...`);
 * modeling them as fixed fields would be brittle.
 */
data class CopernicusRequest(val dataset: String, val inputs: Map<String, Any>) : CacheKey {

    /**
     * @return a deterministic directory name: a human-readable sanitized prefix from [dataset],
     * followed by a truncation of the SHA-256 hash of the canonical `(dataset, inputs)` pair. The
     * prefix aids the human reading the cache directory; the hash provides collision resistance.
     */
    override fun toFileName(): String {
        val canonical = CanonicalJson.encode(
            mapOf(
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
         * Returns the SHA-256 digest of [str] (UTF-8) as a lowercase hex string.
         *
         * @param str the UTF-8 string to hash.
         * @return the digest of SHA-256 over [str].
         */
        private fun sha256Hex(str: String): String = MessageDigest.getInstance("SHA-256")
            .digest(str.toByteArray(Charsets.UTF_8))
            /*
             * 0 = padding with zeros instead of spaces.
             * ("05" and "5" are different values in ECMWF API)
             * 2 = at least two digits.
             * x = all hex are represented in lowercase.
             */
            .joinToString("") { "%02x".format(it) }
    }
}
