/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.slf4j.LoggerFactory

/**
 * Filesystem [CacheManager]: caches the directories produced by an
 * [ExternalDataProvider] under a common root.
 * It knows nothing about HTTP or the individual APIs, depending only on [CacheKey].
 *
 * If two concurrent runs miss the cache for the same entry, both fetch,
 * one wins the atomic rename and the other discards its own work. This
 * wastes effort in that rare case, but never corrupts the cache.
 *
 * **Note!** Entries are **trust-based**: an existing directory is assumed complete and valid,
 * and its content is not re-verified on a cache hit. Manual alteration of a cache entry is
 * out of contract.
 *
 * @param R the type of [CacheKey] accepted by this manager.
 * @param provider populates a cache entry whenever it is missing.
 * @param root path of cache root directory.
 */
class FileSystemCacheManager<in R : CacheKey>(private val provider: ExternalDataProvider<R>, private val root: Path) :
    CacheManager<R> {

    /**
     * Temporary subdirectory, on the same filesystem as [root].
     * Used as a temporary destination for produced files.
     * It gets renamed if the retrieval operation is successful.
     */
    private val tmpRoot: Path = root.resolve(TEMP_SUBDIR)

    /**
     * Returns the directory associated with [request], producing it if absent.
     *
     * On **cache hit** the existing directory is returned immediately.
     * On **cache miss**, the [provider] runs into a temporary directory, its non-emptiness is
     * validated, and it is then promoted to the final location with an atomic rename.
     * If a concurrent process produced the same entry in the meantime, that one is used
     * and the local work is discarded. If, for any reason, [provider] fails, the temporary
     * directory is removed (no "poisoned" entry is left in cache).
     *
     * @param request request identity (to determine the directory name/determine the assets to retrieve).
     * @return the [Path] of the final cache directory, filled with data.
     * @throws IllegalStateException if [provider] writes no file in the temporary directory.
     */
    override fun getOrProduce(request: R): Path {
        val finalDir = root.resolve(request.toDirectoryName())
        // cache hit: the directory already exists
        if (Files.isDirectory(finalDir)) {
            logger.info("Cache hit for '${request.toDirectoryName()}': using $finalDir")
            return finalDir
        }
        logger.info("Cache miss for '${request.toDirectoryName()}': fetching data")
        // cache miss (also creates root if it does not exist)
        Files.createDirectories(tmpRoot)
        val temp = Files.createTempDirectory(tmpRoot, request.toDirectoryName())
        var promoted = false
        try {
            // tries to fill the directory with data
            provider.fetch(request, temp)
            check(hasData(temp)) { "Provider produced no files for '${request.toDirectoryName()}'" }
            promoted = promote(temp, finalDir)
            logger.info("Asset(s) cached in $finalDir")
            return finalDir
        } finally {
            // deletes the temp directory if any accident occurs
            if (!promoted) temp.toFile().deleteRecursively()
        }
    }

    /**
     * Atomically promotes [temp] to [finalDir].
     *
     * @param temp the path of the temporary directory.
     * @param finalDir the path of the final directory after [temp] gets promoted.
     * @return true if this call performed the move, false if a concurrent peer had already
     * produced [finalDir].
     */
    private fun promote(temp: Path, finalDir: Path): Boolean = try {
        Files.move(temp, finalDir, StandardCopyOption.ATOMIC_MOVE)
        true
    } catch (raceLost: FileSystemException) {
        if (!Files.isDirectory(finalDir)) throw raceLost
        // peer won
        false
    }

    /**
     * Checks if [dir] directory holds at least one regular file.
     *
     * @return true if [dir] holds any regular file, false otherwise.
     */
    private fun hasData(dir: Path): Boolean =
        Files.list(dir).use { entries -> entries.anyMatch { Files.isRegularFile(it) } }

    /**
     * Conventional locations and internal constants for the filesystem cache.
     */
    companion object {
        /**
         * Conventional root under which callers are expected to place their own
         * cache subdirectory.
         */
        val DEFAULT_CACHE_DIRECTORY: Path = Path.of(System.getProperty("user.home"), ".alchemist", "cache")
        private const val TEMP_SUBDIR = ".tmp"
        private val logger = LoggerFactory.getLogger(FileSystemCacheManager::class.java)
    }
}
