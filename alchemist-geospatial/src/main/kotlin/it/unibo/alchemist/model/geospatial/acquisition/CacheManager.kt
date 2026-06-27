/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Filesystem cache of the directories produced by data providers. This is the **single** place
 * where cache atomicity lives, once for every provider. It knows nothing about HTTP or the
 * individual APIs, depending only on [CacheKey].
 *
 * This class considers **atomicity but non locking**: if two simultaneous cache-miss runs
 * for the production of data; one wins the atomic rename, the other discards its own work.
 * Wasteful in that rare case, but it never corrupts the cache.
 *
 * **Note!** The entries are **trust-based**: a present directory is assumed complete and valid: the content
 * of a cache-hit is not re-verified. Manual alteration of a cache entry is out of contract.
 *
 * @param root cache root directory (e.g. `~/.alchemist/cache/geospatial`). Created on demand.
 */
class CacheManager(private val root: Path) {

    /**
     * Temporary subdirectory, on the same filesystem as [root].
     * (required for `ATOMIC_MOVE`).
     */
    private val tmpRoot: Path = root.resolve(TEMP_SUBDIR)

    /**
     * Returns the directory associated with [key], producing it if absent.
     *
     * On **cache hit** the existing directory is returned immediately.
     * On **cache miss**, [produce] runs into a temporary directory, its non-emptiness is
     * validated, and it is then promoted to the final location with an atomic rename.
     * If a concurrent process produced the same entry in the meantime, that one is used
     * and the local work is discarded. If, for any reason, [produce] fails, the themporary
     * directory is removed (no "poisoned" entry is left in cache).
     *
     * @param key request identity (to determine the directory name).
     * @param produce action that fills the provided temporary directory with data.
     * @return the [Path] of the final cache directory, filled with data from [produce].
     *
     * @throws IllegalStateException if [produce] writes no file in the temporary directory.
     */
    fun getOrProduce(key: CacheKey, produce: (Path) -> Unit): Path {
        val finalDir = root.resolve(key.toFileName())
        // cache hit: the directory already exists
        if (Files.isDirectory(finalDir)) return finalDir

        // cache miss (also creates root if it does not exist)
        Files.createDirectories(tmpRoot)
        val temp = Files.createTempDirectory(tmpRoot, key.toFileName())
        var moved = false // becomes true if the directory gets promoted
        try {
            // tries to fill the directory with data
            produce(temp)
            check(hasData(temp)) { "Provider produced no files for '${key.toFileName()}'" }

            moved = promote(temp, finalDir)
            return finalDir
        } finally {
            // deletes the temp directory if any accident occurs
            if (!moved) temp.toFile().deleteRecursively()
        }
    }

    /**
     * Atomically promotes [temp] to [finalDir].
     *
     * @param temp the path of the temporary directory.
     * @param finalDir the path of the final directory after [temp] gets promoted.
     *
     * @return true if this call performed the move, false if a concurrent peer had already
     * produced [finalDir].
     */
    private fun promote(temp: Path, finalDir: Path): Boolean = try {
        Files.move(temp, finalDir, StandardCopyOption.ATOMIC_MOVE)
        true
    } catch (raceLost: FileSystemException) {
        if (!Files.isDirectory(finalDir)) throw raceLost
        // peer won; temp dir still exists
        false
    }

    /**
     * Checks if [dir] directory holds at least one regular file.
     *
     * @return true if [dir] holds any regular file, false otherwise.
     */
    private fun hasData(dir: Path): Boolean =
        Files.list(dir).use { entries -> entries.anyMatch { Files.isRegularFile(it) } }

    private companion object {
        private const val TEMP_SUBDIR = ".tmp"
    }
}
