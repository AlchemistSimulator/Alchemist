/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.nio.file.Path

/**
 * Common contract for an external data source: given a typed request, it generates a
 * local directory of files that are ready to be opened. Ensures that the rest of the system
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
interface ExternalDataProvider<in R : CacheKey> {

    /**
     * Obtains the local directory (cache entry) corresponding to [request],
     * generating it if it does not exist (e.g., by downloading resources).
     *
     * @param request the request identifying the data to obtain.
     * @return the [Path] of a **directory** containing the ready-to-open files.
     */
    fun fetch(request: R): Path
}
