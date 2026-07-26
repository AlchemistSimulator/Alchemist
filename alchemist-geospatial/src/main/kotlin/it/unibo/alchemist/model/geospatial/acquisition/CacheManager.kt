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
 * Manages disk-based cache directories identified by deterministic [CacheKey] requests.
 *
 * When a directory is requested via [getOrProduce], the manager resolves its path.
 * If the directory is missing or empty, it automatically fetches and populates the
 * data using the configured [provider].
 *
 * @param R the type of [CacheKey] accepted by this manager.
 *
 * @see ExternalDataProvider
 * @see CacheKey
 */
interface CacheManager<in R : CacheKey> {

    /**
     * The provider responsible for populating the directory associated with the cache entry in
     * [CacheManager.getOrProduce], in the event that it does not exist or is empty.
     */
    val provider: ExternalDataProvider<R>

    /**
     * Calculates and returns the directory where the data was cached via the [request].
     *
     * @param request the request used to calculate the deterministic directory path.
     * @return the path to the directory containing the data.
     */
    fun getOrProduce(request: R): Path
}
