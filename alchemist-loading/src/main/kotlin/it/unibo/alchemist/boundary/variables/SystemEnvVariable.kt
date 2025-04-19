/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.Variable
import java.io.Serializable
import java.util.stream.Stream

/**
 * A variable that retrieves its value from a system environment variable.
 *
 * @param name the name of the environment variable
 * @param defaultValue the default value to return if the environment variable is not set
 */
class SystemEnvVariable(private val name: String, private val defaultValue: Serializable?) : Variable<Serializable> {
    constructor(name: String) : this(name, null)

    override fun getDefault(): Serializable {
        val envValue = System.getenv(name)
        return envValue?.let { convertToValue(it) }
            ?: defaultValue
            ?: error("Environment variable '$name' is not set and no default value is provided.")
    }

    override fun stream(): Stream<Serializable> = Stream.of(getDefault())

    private fun convertToValue(value: String): Serializable = when {
        value.toBooleanStrictOrNull() != null -> value.toBoolean()
        value.toIntOrNull() != null -> value.toInt()
        value.toDoubleOrNull() != null -> value.toDouble()
        else -> value
    }
}
