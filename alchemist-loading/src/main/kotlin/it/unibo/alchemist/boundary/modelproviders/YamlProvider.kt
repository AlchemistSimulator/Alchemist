/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelproviders

import it.unibo.alchemist.boundary.AlchemistModelProvider
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Loads YAML files via SnakeYAML.
 */
object YamlProvider : AlchemistModelProvider {

    private val loaderOptions: LoaderOptions = LoaderOptions().withMaxAliasesForCollections()

    override val fileExtensions: Regex = "[yY][aA]?[mM][lL]".toRegex()

    override fun from(input: String): Map<String, Any> =
        Yaml(loaderOptions).load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: Reader): Map<String, Any> =
        Yaml(loaderOptions).load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: InputStream): Map<String, Any> =
        Yaml(loaderOptions).load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: URL): Map<String, Any> =
        Yaml(loaderOptions).load<Map<String, Any>>(input.openStream()).checkNotNull(input)

    private inline fun <reified T> T?.checkNotNull(input: Any): T {
        requireNotNull(this) {
            "The Alchemist YAML parser for $input could not load anything: maybe the YAML resource is an empty file?"
        }
        return this
    }

    private fun LoaderOptions.withMaxAliasesForCollections(maxAliases: Int = Int.MAX_VALUE) = apply {
        this.maxAliasesForCollections = maxAliases
    }
}
