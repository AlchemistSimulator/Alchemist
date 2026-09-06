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
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory

/**
 * Filesystem [CacheManager]: caches the directories produced by an
 * [ExternalDataProvider] under a common root.
 * It knows nothing about HTTP or the individual APIs, depending only on [CacheKey].
 *
 * Within a JVM, an entry is fetched at most once: callers asking for the
 * same entry at the same time wait for the first one and then find it in cache.
 * Different entries can still be fetched in parallel, and managers
 * sharing a root wait for each other too.
 *
 * Separate processes do not: both fetch, one wins the atomic move and the
 * other throws its own copy away. This wastes work in that rare case, but
 * never corrupts the cache.
 *
 * **Note!** Entries are **trust-based**: an existing directory is assumed to be
 * complete and valid, and its content is not re-verified on a cache hit.
 * Manual alteration of a cache entry is out of contract.
 *
 * @param R the type of [CacheKey] accepted by this manager.
 * @param provider populates a cache entry whenever it is missing.
 * @param root path of cache root directory.
 */
class FileSystemCacheManager<in R : CacheKey>(private val provider: ExternalDataProvider<R>, root: Path) :
    CacheManager<R> {

    /**
     * Cache root directory, normalized so that equivalent paths written
     * differently address the same cache.
     */
    private val root: Path = root.toAbsolutePath().normalize()

    /**
     * A temporary directory where the provider can create files. If the operation is successful, its contents
     * are moved to the final location. See [promote].
     */
    private val tmpRoot: Path = this.root.resolve(TEMP_SUBDIR)

    /**
     * Returns the directory associated with [request], producing it if absent.
     *
     * On **cache hit** the existing directory is returned immediately.
     * On **cache miss** the entry is produced at most once: concurrent callers
     * requesting the same entry block until the first one is done, then they observe
     * a cache hit.
     *
     * @param request request identity (to determine the directory name/determine the assets to retrieve).
     * @return the [Path] of the final cache directory, filled with data.
     * @throws IllegalStateException if [provider] writes no file in the temporary directory.
     */
    override fun getOrProduce(request: R): Path {
        val cacheKey = request.toDirectoryName()
        val finalDir = root.resolve(cacheKey)
        // cache hit: the directory already exists
        if (Files.isDirectory(finalDir)) {
            logger.info("Cache hit for '$cacheKey': using $finalDir")
            return finalDir
        }
        // the path gets recreated on every execution, so it is not used as the identity
        val monitor = entryLocks.computeIfAbsent(finalDir) { Any() }
        logger.debug("Cache miss for '$cacheKey': acquiring its monitor")
        // wait until the monitor becomes available
        return synchronized(monitor) {
            // a peer may have produced the entry while this caller was waiting the monitor
            if (Files.isDirectory(finalDir)) {
                logger.info("Entry '$cacheKey' has been produced by a concurrent peer: using $finalDir")
                finalDir
            } else {
                produce(request, cacheKey, finalDir)
            }
        }
    }

    /**
     * Fills [finalDir] with the assets described by [request].
     *
     * The [provider] runs into a temporary directory, its non-emptiness is validated, and it is
     * then promoted to the final location with an atomic move operation. If a concurrent process
     * produced the same entry in the meantime, that one is used and the local work is discarded.
     * If, for any reason, [provider] fails, the temporary directory is removed (no "poisoned"
     * entry is left in cache).
     *
     * **Note:** callers are expected to hold the monitor for the entry.
     *
     * @param request request identity, forwarded to the [provider].
     * @param cacheKey the directory name [request] maps to, used for logging.
     * @param finalDir the destination of the produced assets.
     * @return [finalDir], filled with data.
     * @throws IllegalStateException if [provider] writes no file in the temporary directory.
     */
    private fun produce(request: R, cacheKey: String, finalDir: Path): Path {
        logger.info("Cache miss for '$cacheKey': fetching data")
        // also creates the root directory if it does not exist
        Files.createDirectories(tmpRoot)
        val temp = Files.createTempDirectory(tmpRoot, cacheKey)
        var promoted = false
        try {
            // tries to fill the directory with data
            provider.fetch(request, temp)
            check(hasData(temp)) { "Provider produced no files for '$cacheKey'" }
            promoted = promote(temp, finalDir)
            if (promoted) {
                logger.info("Asset(s) cached in $finalDir")
            }
            return finalDir
        } finally {
            // deletes the temp directory if any accident occurred
            if (!promoted) {
                logger.debug("Discarding the temporary directory $temp")
                if (!temp.toFile().deleteRecursively()) {
                    logger.warn("Could not fully delete the temporary directory $temp")
                }
            }
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
        // another process won
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

        /**
         * A map that tracks the monitors for cache entry generation.
         */
        private val entryLocks = ConcurrentHashMap<Path, Any>()
        private const val TEMP_SUBDIR = ".tmp"
        private val logger = LoggerFactory.getLogger(FileSystemCacheManager::class.java)
    }
}
