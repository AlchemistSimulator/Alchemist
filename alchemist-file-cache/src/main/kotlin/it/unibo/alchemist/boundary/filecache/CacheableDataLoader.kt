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
 * Produces the content of a [LoadingDirectoryCache] entry: given a key, it fills a directory with files that are
 * ready to be opened, for instance by downloading them or by computing them locally.
 * The cache is unaware of how the data is obtained.
 *
 * The type parameter [K] binds each loader to its own family of keys: for instance,
 * a `CacheableDataLoader<CopernicusRequest>` rejects any other key at compile time.
 *
 * @param K the type of [CachedDirectoryKey] this loader can load.
 */
fun interface CacheableDataLoader<in K : CachedDirectoryKey> {

    /**
     * Fills [target] with the data denoted by [key].
     *
     * @param key the key identifying the data to load.
     * @param target the directory to fill with data; it exists and is writable.
     * @throws IllegalStateException if the data cannot be loaded.
     */
    fun load(key: K, target: Path)
}
