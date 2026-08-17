/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import java.nio.file.Path

/**
 * Common contract for an external data source: given a typed request, it fills a
 * directory with files that are ready to be opened. Ensures that the rest of the system
 * is unaware of the specifics of each individual provider API.
 *
 * [R] is associated with [CacheKey], so caching is part of the contract; it is not
 * an implementation detail: every request must be cacheable.
 *
 * The type parameter [R] binds each provider to its own family of requests. For
 * example, an `ExternalDataProvider<CopernicusRequest>` rejects a `BBBikeRequest` at
 * compile time.
 *
 * @param R the consumed request type, bound to [CacheKey] so that its result
 * is always cacheable.
 */
fun interface ExternalDataProvider<in R : CacheKey> {

    /**
     * Fills [targetDir] with the data denoted by [request] (e.g., by downloading resources).
     *
     * @param request the request identifying the data to obtain.
     * @param targetDir the directory to fill with data; it must exist and be writable.
     *
     * @throws IllegalStateException if the data cannot be produced.
     */
    fun fetch(request: R, targetDir: Path)
}
