/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.DependentVariable
import java.io.Serializable

/**
 * A variable that retrieves its value from a system environment variable.
 *
 * @param name the name of the environment variable
 * @param defaultValue the default value to return if the environment variable is not set
 */
class SystemEnvVariable @JvmOverloads constructor(
    private val name: String,
    private val defaultValue: Serializable? = null,
) : DependentVariable<Serializable> {

    override fun getWith(variables: MutableMap<String, Any>?): Serializable = when (val value = System.getenv(name)) {
        null -> defaultValue ?: error("Environment variable '$name' is not set and no default value is provided.")
        else -> {
            converters.mapNotNull { it(value) }.firstOrNull() ?: value
        }
    }

    private companion object {
        val converters: Sequence<(String) -> Serializable?> = sequenceOf(
            String::toBooleanStrictOrNull,
            String::toIntOrNull,
            String::toDoubleOrNull,
            String::toLongOrNull,
            String::toFloatOrNull,
            String::toBigIntegerOrNull,
            String::toBigDecimalOrNull,
        )
    }
}
