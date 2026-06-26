/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

/**
 * Renders this `String` as a single file-system-safe path segment: every character outside `[A-Za-z0-9._-]`
 * is replaced with `_`. Distinct strings may collapse to the same output (e.g. `"a b"` and `"a_b"`).
 *
 * @return this string sanitized (i.e. all non alphatical/numerical characters replaced by `_`)
 */
internal fun String.toFileSystemSafe(): String = this.replace(Regex("[^A-Za-z0-9._-]"), "_")
