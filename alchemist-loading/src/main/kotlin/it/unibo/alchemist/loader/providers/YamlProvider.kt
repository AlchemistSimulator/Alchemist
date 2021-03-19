/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.providers

import it.unibo.alchemist.loader.AlchemistModelProvider
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Loads YAML files via SnakeYAML.
 */
object YamlProvider : AlchemistModelProvider {

    override val fileExtensions: Regex = "[yY][aA]?[mM][lL]".toRegex()

    override fun from(input: String): Map<String, Any> = Yaml().load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: Reader): Map<String, Any> = Yaml().load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: InputStream): Map<String, Any> = Yaml().load<Map<String, Any>>(input).checkNotNull(input)

    override fun from(input: URL): Map<String, Any> =
        Yaml().load<Map<String, Any>>(input.openStream()).checkNotNull(input)

    private inline fun <reified T> T?.checkNotNull(input: Any): T {
        require(this != null) {
            "The Alchemist YAML parser for $input could not load anything: maybe the YAML resource is an empty file?"
        }
        return this
    }
}
