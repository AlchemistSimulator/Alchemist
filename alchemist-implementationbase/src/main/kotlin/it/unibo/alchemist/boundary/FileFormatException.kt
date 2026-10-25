/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import java.net.URL

/**
 * Signals that the content found at [source] is not valid [expectedFormat] data,
 * e.g., a file with a `.gpx` extension that does not contain a valid GPX document.
 * The original failure, if any, is available as [cause].
 */
class FileFormatException @JvmOverloads constructor(
    val expectedFormat: String,
    val source: URL,
    cause: Throwable? = null,
) : RuntimeException(describe(expectedFormat, source), cause) {
    private companion object {
        private fun describe(expectedFormat: String, source: URL): String = buildString {
            append("Invalid content in ")
            append(source)
            append(": expected a valid ")
            append(expectedFormat)
            append(" file")
        }
    }
}
