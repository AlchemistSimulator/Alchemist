/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

/**
 * Identity of a [LoadingDirectoryCache] entry: equal keys denote the same data, and thus the same directory.
 */
interface CachedDirectoryKey {

    /**
     * A **deterministic, file-system-safe** name for the directory holding the entry, derived *only* from what
     * determines its content, so that equal content always maps to the same name.
     * It must be a single path segment: no separators (`/`, `\`), and valid on every operating system.
     */
    val directoryName: String
}
