/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Provider interface for loading Alchemist simulations from various input sources.
 */
interface AlchemistLoaderProvider : Extensions {

    /**
     * Creates a [Loader] from a string input.
     *
     * @param input The input string containing the simulation configuration.
     * @return A [Loader] instance.
     */
    fun from(input: String): Loader

    /**
     * Creates a [Loader] from a [Reader] input.
     *
     * @param input The reader containing the simulation configuration.
     * @return A [Loader] instance.
     */
    fun from(input: Reader): Loader = from(input.readText())

    /**
     * Creates a [Loader] from an [InputStream] input.
     *
     * @param input The input stream containing the simulation configuration.
     * @return A [Loader] instance.
     */
    fun from(input: InputStream): Loader = from(input.reader())

    /**
     * Creates a [Loader] from a [URL] input.
     *
     * @param input The URL pointing to the simulation configuration.
     * @return A [Loader] instance.
     */
    fun from(input: URL): Loader = from(input.openStream())
}
