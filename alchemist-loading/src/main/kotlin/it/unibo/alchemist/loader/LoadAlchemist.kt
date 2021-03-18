/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.ClassPathScanner
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Loads Alchemist simulations from a variety of resources.
 */
object LoadAlchemist {
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(input))

    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(reader))

    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(stream))

    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(url))

    @JvmStatic
    fun from(url: URL) = from(url, modelForExtension(url.path.takeLastWhile { it != '.' }))

    @JvmStatic
    fun from(file: File) = from(file.inputStream(), modelForExtension(file.extension))

    @JvmStatic
    fun from(file: String) = from(File(file))

    @JvmStatic
    private fun modelForExtension(extension: String) = ClassPathScanner
        .subTypesOf<AlchemistModelProvider>("it.unibo.alchemist.loader.providers")
        .mapNotNull { it.kotlin.objectInstance }
        .filter { it.fileExtensions.matches(extension) }
        .also { require(it.size == 1) { "None or conflicting loaders for extension $extension: $it" } }
        .first()
}
