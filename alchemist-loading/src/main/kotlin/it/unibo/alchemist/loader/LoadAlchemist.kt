/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.loader.m2m.SimulationModel
import it.unibo.alchemist.util.ClassPathScanner
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Loads Alchemist simulations from a variety of resources.
 */
object LoadAlchemist {

    /**
     * Load from an [input] [String].
     */
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(input), overrides),
        )

    /**
     * Load from a [reader].
     */
    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(reader), overrides),
        )

    /**
     * Load from an [InputStream].
     */
    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(stream), overrides),
        )

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(applyOverrides(model.from(url), overrides))

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, overrides: List<String> = emptyList()) =
        from(url, modelForExtension(url.path.takeLastWhile { it != '.' }), overrides)

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
        .subTypesOf<AlchemistModelProvider>("it.unibo.alchemist.loader.providers")
        .mapNotNull { it.kotlin.objectInstance }
        .filter { it.fileExtensions.matches(extension) }
        .also { require(it.size == 1) { "None or conflicting loaders for extension $extension: $it" } }
        .first()

    @JvmStatic
    private fun applyOverrides(map: Map<String, *>, overrides: List<String>): Map<String, *> {
        return VariablesOverrider.applyOverrides(map, overrides)
    }
}
