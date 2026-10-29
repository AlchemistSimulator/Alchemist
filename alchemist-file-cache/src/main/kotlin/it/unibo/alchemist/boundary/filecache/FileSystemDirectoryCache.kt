/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.nio.channels.FileLockInterruptionException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import net.harawata.appdirs.AppDirsFactory
import org.slf4j.LoggerFactory

/**
 * A [LoadingDirectoryCache] storing its entries as directories under a common [root] on the local file system,
 * loading the missing ones through a [CacheableDataLoader].
 * It knows nothing about how data is obtained, depending only on [CachedDirectoryKey].
 *
 * Each entry is loaded at most once, even across processes sharing the same root: callers asking for an entry
 * that is being loaded wait for the loading to complete and then find it in cache.
 * Within a JVM, they synchronize on a monitor per entry; across processes, on an operating system lock
 * of a file per entry, in the `.locks` directory under [root].
 * Different entries can still be loaded in parallel.
 *
 * Loaded entries are published with an atomic move, so they are visible either completely or not at all.
 * If file locks are not supported (as on some network file systems), processes do not wait for each other:
 * all of them load the entry, the first one to publish it wins, and the others discard their own copy.
 * This wastes work, but never corrupts the cache.
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
     * Once loaded successfully, a temporary directory is moved to its final location: see [load].
     */
    private val temporaryRoot: Path = this.root.resolve(TEMP_SUBDIR)

    /**
     * The directory holding the lock files, one per entry, that processes sharing [root] lock while loading.
     */
    private val locksRoot: Path = this.root.resolve(LOCKS_SUBDIR)

    /**
     * Returns the directory associated with [key], producing it if absent.
     *
     * On **cache hit** the existing directory is returned immediately.
     * On **cache miss** the entry is loaded at most once: concurrent callers, in this or in other processes,
     * requesting the same entry block until the first one is done, then they observe a cache hit.
     *
     * @param key the entry identity, determining both the directory name and the data to load.
     * @return the [Path] of the final cache directory, filled with data.
     * @throws IllegalArgumentException if the [CachedDirectoryKey.directoryName] of [key] is not a single path
     * segment, or starts with `.`.
     * @throws IllegalStateException if the [loader] fails or writes no file.
     */
    override operator fun get(key: K): Path {
        val directoryName = key.directoryName
        val resolvedDirectory = root.resolve(directoryName)
        // names starting with '.' belong to the cache, and catch "", ".", and ".." too
        require(directoryName.isNotEmpty() && !directoryName.startsWith('.') && resolvedDirectory.parent == root) {
            "Invalid cache directory name '$directoryName': it must be a single path segment not starting with '.'"
        }
        // cache hit: the directory already exists
        if (Files.isDirectory(resolvedDirectory)) {
            logger.info("Cache hit for '$directoryName': using $resolvedDirectory")
            return resolvedDirectory
        }
        // the path gets recreated on every execution, so it is not used as the identity
        val monitor = entryLocks.computeIfAbsent(resolvedDirectory) { Any() }
        logger.debug("Cache miss for '$directoryName': acquiring its monitor")
        /*
         * The monitor comes first: a JVM cannot hold overlapping locks of the same file,
         * so threads of the same JVM must not compete for the file lock.
         */
        return synchronized(monitor) {
            resolvedDirectory.readyOrNull ?: withFileLock(directoryName) {
                resolvedDirectory.readyOrNull ?: load(key, directoryName, resolvedDirectory)
            }
        }
    }

    /**
     * Runs [action] holding the operating system lock of the file associated with the entry named [directoryName],
     * so that processes sharing [root] do not load the same entry at the same time.
     * If the file system does not support locks, [action] runs anyway: the atomic promotion keeps the cache
     * consistent, at the cost of possibly loading the entry more than once.
     */
    private fun <R> withFileLock(directoryName: String, action: () -> R): R {
        Files.createDirectories(locksRoot)
        val lockFile = locksRoot.resolve("$directoryName.lock")
        return FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { channel ->
            logger.debug("Acquiring the file lock {}", lockFile)
            runCatching { channel.lock() }
                .onFailure { failure ->
                    if (failure.doesNotMeanThatLockingIsUnsupported) {
                        throw failure
                    }
                }
                .getOrNull()
                .use { action() }
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
     * **Note:** callers are expected to hold the monitor and the file lock for the entry.
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
        Files.createDirectories(temporaryRoot)
        val temp = Files.createTempDirectory(temporaryRoot, directoryName)
        try {
            // tries to fill the directory with data
            loader.load(key, temp)
            check(hasData(temp)) { "The loader produced no files for '$directoryName'" }
            runCatching { Files.move(temp, finalDir, StandardCopyOption.ATOMIC_MOVE) }
                .onSuccess { logger.info("Data cached in $finalDir") }
                .onFailure {
                    val anotherProcessWon = it is FileSystemException && Files.isDirectory(finalDir)
                    when {
                        anotherProcessWon -> logger.info("Another process has already cached the data in $finalDir")
                        else -> throw IllegalStateException(it)
                    }
                }
            return finalDir
        } finally {
            // deletes the temp directory if any accident occurred
            if (Files.exists(temp)) {
                logger.debug("Discarding the temporary directory {}", temp)
                if (!temp.toFile().deleteRecursively()) {
                    logger.warn("Could not fully delete the temporary directory $temp")
                }
            }
        }
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
        private const val LOCKS_SUBDIR = ".locks"
        private val logger = LoggerFactory.getLogger(FileSystemDirectoryCache::class.java)

        private val Path.readyOrNull: Path? get() = takeIf { Files.isDirectory(it) }

        // Some file systems do not support locks, and throw an IOException that is neither a
        // ClosedChannelException nor a FileLockInterruptionException. In that case, we cannot wait for a lock.
        private val Throwable.doesNotMeanThatLockingIsUnsupported get() =
            this !is IOException || this is ClosedChannelException || this is FileLockInterruptionException
    }
}
