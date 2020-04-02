/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.launch

import it.unibo.alchemist.AlchemistExecutionOptions

/**
 * An entity with a [name] that can take responsibility for performing an Alchemist run, given the current
 * [AlchemistExecutionOptions].
 */
interface Launcher : (AlchemistExecutionOptions) -> Unit {

    /**
     * Launcher name.
     */
    val name: String

    /**
     * Given the [currentOptions], decides whether or not this [Launcher] is executable.
     */
    fun validate(currentOptions: AlchemistExecutionOptions): Validation

    /**
     * Actual execution. Implementors are **not** supposed to override this behaviour, although they can.
     * The default implementation performs validation, and if successful calls [launch].
     * Otherwise, throws an [IllegalStateException].
     */
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

    /**
     * Actually gets the job done by performing the requested operations.
     */
    fun launch(parameters: AlchemistExecutionOptions)
}

/**
 * Result of the validation of a launcher.
 */
sealed class Validation {
    /**
     * The [Launcher] can run.
     */
    object OK : Validation()

    /**
     * The [Launcher] can't run and provides a [reason].
     */
    data class Invalid(val reason: String) : Validation()
}
