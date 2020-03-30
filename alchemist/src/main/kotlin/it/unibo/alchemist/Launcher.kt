/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist

interface Launcher : (AlchemistExecutionOptions) -> Unit {

    val name: String

    fun validate(currentOptions: AlchemistExecutionOptions): Validation

    override fun invoke(parameters: AlchemistExecutionOptions) {
        val validation = validate(parameters)
        if (validation is Validation.OK) {
            launch(parameters)
        } else {
            throw IllegalStateException(
                "Cannot launch $name without a suitable configuration file: $validation"
            )
        }
    }

    fun launch(parameters: AlchemistExecutionOptions): Unit

    fun incompatibleWith(option: String) = Validation.Invalid("$name is not compatible with $option")

    fun requires(option: String) = Validation.Invalid("$name requires $option")
}

sealed class Validation {
    object OK : Validation()
    data class Invalid(val reason: String) : Validation()
}
