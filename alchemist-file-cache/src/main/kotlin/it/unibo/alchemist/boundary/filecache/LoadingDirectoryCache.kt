/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

import java.nio.file.Path

/**
 * A cache of directories, each one identified by a [CachedDirectoryKey], that loads missing entries on access,
 * in the spirit of a `LoadingCache`.
 *
 * Implementations guarantee that the returned directory exists and holds the data associated with the key, loading
 * it if it is not available yet. How the data is loaded is not part of this contract.
 *
 * @param K the type of [CachedDirectoryKey] accepted by this cache.
 */
fun interface LoadingDirectoryCache<in K : CachedDirectoryKey> {
    /**
     * Returns the directory holding the data associated with [key], loading it if absent.
     *
     * @param key the key identifying the cache entry.
     * @return the path to the directory holding the data.
     */
    operator fun get(key: K): Path
}
