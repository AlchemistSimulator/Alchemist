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
 * Provides cached directories of data identified by deterministic [CacheKey] requests.
 *
 * Implementations guarantee that the returned directory exists and holds the data
 * associated with the request, producing it if it is not available yet. How the data is
 * obtained, is not part of this contract.
 *
 * @param R the type of [CacheKey] accepted by this manager.
 *
 * @see CacheKey
 */
interface CacheManager<in R : CacheKey> {
    /**
     * Returns the directory holding the data associated with [request], producing it if absent.
     *
     * @param request the request identifying the cache entry.
     * @return the path to the directory holding the data.
     */
    fun getOrProduce(request: R): Path
}
