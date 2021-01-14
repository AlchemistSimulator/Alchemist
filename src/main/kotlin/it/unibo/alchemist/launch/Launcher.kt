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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
            val priority = validation.priority
            when (priority) {
                is Priority.High -> logger.warn("{} selected because {}", this, priority.reason)
                is Priority.Fallback -> logger.warn("{} selected because {}", this, priority.warning)
            }
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

    companion object {
        protected val logger: Logger = LoggerFactory.getLogger(Launcher::class.java)
    }
}

/**
 * Result of the validation of a launcher.
 */
sealed class Validation {
    /**
     * The [Launcher] can run with some [priority].
     */
    data class OK(val priority: Priority = Priority.Normal) : Validation()

    /**
     * The [Launcher] can't run and provides a [reason].
     */
    data class Invalid(val reason: String) : Validation()
}

/**
 * Defines the likelihood that the configuration is compatible with a [Launcher].
 */
sealed class Priority : Comparable<Priority> {

    /**
     * The loader overrides the behaviour of the loaders provided by default, for the same options.
     * It must specify a [reason].
     */
    data class High(val reason: String) : Priority()

    /**
     * Default priority, to be returned when the requested options fit the expected configuration.
     */
    object Normal : Priority()

    /**
     * A low priority, indicating that the [Launcher] may run, but it will ignore some options,
     * and other launchers will be preferred. It must emit a [warning].
     */
    data class Fallback(val warning: String) : Priority()

    override fun compareTo(other: Priority): Int = when {
        this::class == other::class -> 0
        this is High -> 1
        this is Fallback -> -1
        other is High -> -1
        other is Fallback -> 1
        else -> throw IllegalStateException(
            """
            Comparison of $this and $other failed.
            There is a bug in alchemist. Please open an issue with this stacktrace.
            """.trimIndent()
        )
    }

    override fun toString() = this::class.simpleName!!
}
