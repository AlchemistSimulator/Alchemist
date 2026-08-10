/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import java.nio.file.InvalidPathException
import java.nio.file.Path

/**
 * Expands a leading `~` in the `String` into the current user's home directory.
 *
 * Expansion applies to a lone `~` and to paths starting with `~/` or `~\`. Everything
 * else is converted verbatim.
 *
 * The result is not normalized and not checked for existence
 *
 * @return the corresponding [Path], with the home directory substituted when applicable.
 * @throws InvalidPathException if this `String` cannot be converted to a [Path] on the current filesystem.
 */
internal fun String.expandUser(): Path = when {
    this == "~" -> Path.of(System.getProperty("user.home"))
    this.startsWith("~/") || this.startsWith("~\\") ->
        Path.of(System.getProperty("user.home")).resolve(this.substring(2))
    else -> Path.of(this)
}
