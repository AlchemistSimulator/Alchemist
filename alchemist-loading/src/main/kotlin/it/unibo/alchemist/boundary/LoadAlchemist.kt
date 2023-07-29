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
import it.unibo.alchemist.model.util.VariablesOverrider
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
     * Load from an [input] [String] with overrides.
     */
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider, overrides: List<String>) =
        SimulationModel.fromMap(
            applyOverrides(model.from(input), overrides),
        )

    /**
     * Load from an [input] [String].
     */
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider) =
        from(input, model, emptyList())

    /**
     * Load from a [reader] with overrides.
     */
    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider, overrides: List<String>) =
        SimulationModel.fromMap(
            applyOverrides(model.from(reader), overrides),
        )

    /**
     * Load from a [reader].
     */
    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider) =
        from(reader, model, emptyList())

    /**
     * Load from an [InputStream] with overrides.
     */
    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider, overrides: List<String>) =
        SimulationModel.fromMap(
            applyOverrides(model.from(stream), overrides),
        )

    /**
     * Load from an [InputStream].
     */
    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider) =
        from(stream, model, emptyList())

    /**
     * Load from an [url] with overrides.
     */
    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider, overrides: List<String>) =
        SimulationModel.fromMap(applyOverrides(model.from(url), overrides))

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider) =
        from(url, model, emptyList())

    /**
     * Load from an [url] with overrides.
     */
    @JvmStatic
    fun from(url: URL, overrides: List<String>) =
        from(url, modelForExtension(url.path.takeLastWhile { it != '.' }), overrides)

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL) =
        from(url, emptyList())

    /**
     * Load from a [file] with overrides.
     */
    @JvmStatic
    fun from(file: File, overrides: List<String>) =
        from(file.inputStream(), modelForExtension(file.extension), overrides)

    /**
     * Load from a [file].
     */
    @JvmStatic
    fun from(file: File) = from(file.inputStream(), modelForExtension(file.extension))

    /**
     * Load from a [string] with overrides.
     */
    @JvmStatic
    fun from(string: String, overrides: List<String>) = from(File(string), overrides)

    /**
     * Load from a [string].
     */
    @JvmStatic
    fun from(string: String) = from(string, emptyList())

    @JvmStatic
    private fun modelForExtension(extension: String) = ClassPathScanner
        .subTypesOf<AlchemistModelProvider>(extractPackageFrom<LoadAlchemist>())
        .mapNotNull { it.kotlin.objectInstance }
        .filter { it.fileExtensions.matches(extension) }
        .also { require(it.size == 1) { "None or conflicting loaders for extension $extension: $it" } }
        .first()

    @JvmStatic
    private fun applyOverrides(map: Map<String, *>, overrides: List<String>): Map<String, *> {
        return VariablesOverrider.applyOverrides(map, overrides)
    }
}
