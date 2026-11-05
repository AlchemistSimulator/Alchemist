/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * The name of the data file written by the loaders in tests.
 */
internal const val DATA_FILE = "data.nc"

/**
 * The content of the [DATA_FILE] written by [main].
 */
internal const val OTHER_PROCESS_DATA = "loaded by another process"

private const val POLLING_INTERVAL_MILLIS = 10L

/**
 * Waits as long as [condition] holds.
 */
internal tailrec fun awaitWhile(condition: () -> Boolean) {
    if (condition()) {
        Thread.sleep(POLLING_INTERVAL_MILLIS)
        awaitWhile(condition)
    }
}

/**
 * Entry point of a separate process loading, in the [FileSystemDirectoryCache] rooted in `args[0]`,
 * the entry named `args[1]`.
 * While loading, it creates the file `args[2]`, then waits for the file `args[3]` to exist
 * before writing [OTHER_PROCESS_DATA] in the entry.
 */
fun main(args: Array<String>) {
    val (root, directoryName, loading, release) = args
    val key = object : CachedDirectoryKey {
        override val directoryName = directoryName
    }
    val loader = CacheableDataLoader<CachedDirectoryKey> { _, target ->
        Path(loading).createFile()
        awaitWhile { !Path(release).exists() }
        target.resolve(DATA_FILE).writeText(OTHER_PROCESS_DATA)
    }
    FileSystemDirectoryCache(loader, Path(root))[key]
}
