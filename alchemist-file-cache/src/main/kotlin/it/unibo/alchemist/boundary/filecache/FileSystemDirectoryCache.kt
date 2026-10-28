/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import net.harawata.appdirs.AppDirsFactory
import org.slf4j.LoggerFactory

/**
 * A [LoadingDirectoryCache] storing its entries as directories under a common [root] on the local file system,
 * loading the missing ones through a [CacheableDataLoader].
 * It knows nothing about how data is obtained, depending only on [CachedDirectoryKey].
 *
 * Within a JVM, an entry is loaded at most once: callers asking for the same entry at the same time wait for the
 * first one and then find it in cache.
 * Different entries can still be loaded in parallel, and caches sharing a root wait for each other too.
 *
 * Separate processes do not: both load, one wins the atomic move and the other throws its own copy away.
 * This wastes work in that rare case, but never corrupts the cache.
 *
 * **Note!** Entries are **trust-based**: an existing directory is assumed to be complete and valid,
 * and its content is not re-verified on a cache hit.
 * Manual alteration of a cache entry is out of contract.
 *
 * @param K the type of [CachedDirectoryKey] accepted by this cache.
 * @param loader fills a cache entry whenever it is missing.
 * @param root path of the cache root directory.
 */
class FileSystemDirectoryCache<in K : CachedDirectoryKey>(private val loader: CacheableDataLoader<K>, root: Path) :
    LoadingDirectoryCache<K> {

    /**
     * Cache root directory, normalized so that equivalent paths written
     * differently address the same cache.
     */
    private val root: Path = root.toAbsolutePath().normalize()

    /**
     * The directory holding the temporary directories where the [loader] creates files.
     * Once loaded successfully, a temporary directory is moved to its final location: see [promote].
     */
    private val tmpRoot: Path = this.root.resolve(TEMP_SUBDIR)

    /**
     * Returns the directory associated with [key], producing it if absent.
     *
     * On **cache hit** the existing directory is returned immediately.
     * On **cache miss** the entry is loaded at most once: concurrent callers
     * requesting the same entry block until the first one is done, then they observe
     * a cache hit.
     *
     * @param key the entry identity, determining both the directory name and the data to load.
     * @return the [Path] of the final cache directory, filled with data.
     * @throws IllegalStateException if the [loader] fails or writes no file.
     */
    override operator fun get(key: K): Path {
        val directoryName = key.directoryName
        val finalDir = root.resolve(directoryName)
        // cache hit: the directory already exists
        if (Files.isDirectory(finalDir)) {
            logger.info("Cache hit for '$directoryName': using $finalDir")
            return finalDir
        }
        // the path gets recreated on every execution, so it is not used as the identity
        val monitor = entryLocks.computeIfAbsent(finalDir) { Any() }
        logger.debug("Cache miss for '$directoryName': acquiring its monitor")
        // wait until the monitor becomes available
        return synchronized(monitor) {
            // a peer may have loaded the entry while this caller was waiting for the monitor
            if (Files.isDirectory(finalDir)) {
                logger.info("Entry '$directoryName' has been loaded by a concurrent peer: using $finalDir")
                finalDir
            } else {
                load(key, directoryName, finalDir)
            }
        }
    }

    /**
     * Fills [finalDir] with the data denoted by [key].
     *
     * The [loader] runs into a temporary directory, its non-emptiness is validated, and it is
     * then promoted to the final location with an atomic move operation. If a concurrent process
     * loaded the same entry in the meantime, that one is used and the local work is discarded.
     * If, for any reason, the [loader] fails, the temporary directory is removed (no "poisoned"
     * entry is left in cache).
     *
     * **Note:** callers are expected to hold the monitor for the entry.
     *
     * @param key the entry identity, forwarded to the [loader].
     * @param directoryName the directory name [key] maps to.
     * @param finalDir the destination of the loaded data.
     * @return [finalDir], filled with data.
     * @throws IllegalStateException if the [loader] fails or writes no file.
     */
    private fun load(key: K, directoryName: String, finalDir: Path): Path {
        logger.info("Cache miss for '$directoryName': loading data")
        // also creates the root directory if it does not exist
        Files.createDirectories(tmpRoot)
        val temp = Files.createTempDirectory(tmpRoot, directoryName)
        var promoted = false
        try {
            // tries to fill the directory with data
            loader.load(key, temp)
            check(hasData(temp)) { "The loader produced no files for '$directoryName'" }
            promoted = promote(temp, finalDir)
            if (promoted) {
                logger.info("Data cached in $finalDir")
            }
            return finalDir
        } finally {
            // deletes the temp directory if any accident occurred
            if (!promoted) {
                logger.debug("Discarding the temporary directory {}", temp)
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
     * loaded [finalDir].
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
     * Checks whether the [dir] directory holds at least one regular file.
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
         * Conventional root under which callers are expected to place their own cache subdirectory:
         * Alchemist's user cache directory, as conventional for the current operating system
         * (e.g., `~/.cache/alchemist` on Linux).
         */
        val DEFAULT_CACHE_DIRECTORY: Path = Path.of(
            AppDirsFactory.getInstance().getUserCacheDir("alchemist", null, "it.unibo"),
        )

        /**
         * The monitors guarding the loading of each cache entry.
         */
        private val entryLocks = ConcurrentHashMap<Path, Any>()
        private const val TEMP_SUBDIR = ".tmp"
        private val logger = LoggerFactory.getLogger(FileSystemDirectoryCache::class.java)
    }
}
