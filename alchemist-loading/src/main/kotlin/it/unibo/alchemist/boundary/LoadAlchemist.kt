/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import it.unibo.alchemist.boundary.loader.SimulationModel
import it.unibo.alchemist.util.ClassPathScanner
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL
import kotlin.reflect.jvm.jvmName

/**
 * Loads Alchemist simulations from a variety of resources.
 */
object LoadAlchemist {

    private val packageExtractor = Regex("""((?:\w|\.)+)\.\w+$""")
    private inline fun <reified T> extractPackageFrom() = packageExtractor
        .matchEntire(T::class.jvmName)
        ?.let { it.groupValues[1] }
        ?: error("Cannot extract package from ${T::class.jvmName}")

    /**
     * Load from an [input] [String].
     */
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(input))

    /**
     * Load from a [reader].
     */
    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(reader))

    /**
     * Load from an [InputStream].
     */
    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(stream))

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider) = SimulationModel.fromMap(model.from(url))

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL) = from(url, modelForExtension(url.path.takeLastWhile { it != '.' }))

    /**
     * Load from a [file].
     */
    @JvmStatic
    fun from(file: File) = from(file.inputStream(), modelForExtension(file.extension))

    /**
     * Load from a [string].
     */
    @JvmStatic
    fun from(string: String) = from(File(string))

    @JvmStatic
    private fun modelForExtension(extension: String) = ClassPathScanner
        .subTypesOf<AlchemistModelProvider>(extractPackageFrom<LoadAlchemist>())
        .mapNotNull { it.kotlin.objectInstance }
        .filter { it.fileExtensions.matches(extension) }
        .also { require(it.size == 1) { "None or conflicting loaders for extension $extension: $it" } }
        .first()
}
