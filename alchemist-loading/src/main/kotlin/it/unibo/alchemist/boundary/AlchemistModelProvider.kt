/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
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
 * Translates inputs to a Map representing the Alchemist model.
 */
interface AlchemistModelProvider {

    /**
     * A [Regex] matching the file extensions supported by this provider.
     */
    val fileExtensions: Regex

    /**
     * Reads [input] from a [String].
     */
    fun from(input: String): Map<String, *>

    /**
     * Reads [input] from a [Reader].
     */
    fun from(input: Reader): Map<String, *> = from(input.readText())

    /**
     * Reads [input] from an [InputStream].
     */
    fun from(input: InputStream): Map<String, *> = from(input.reader())

    /**
     * Reads [input] from a [URL].
     */
    fun from(input: URL): Map<String, *> = from(input.openStream())
}
