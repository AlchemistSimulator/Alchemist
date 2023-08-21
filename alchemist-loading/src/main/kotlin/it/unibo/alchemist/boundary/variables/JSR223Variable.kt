/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.loader.syntax.DocumentRoot
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.script.SimpleBindings
import kotlin.reflect.jvm.jvmName

/**
 * This variable loads any [JSR-233](https://archive.is/PGdk8) language available in the classpath.
 *
 * @constructor builds a new JSR223Variable given a language name and a script.
 *
 * @param language can be the name of the language, the file extension, or its mime type
 * @param formula the script that will get interpreted
 * @param timeout how long should the interpreter be allowed to compute before giving up, in ms. Defaults to 1000ms.
 */
data class JSR223Variable @JvmOverloads constructor(
    val language: String,
    val formula: String,
    val timeout: Long = 1000,
) : DependentVariable<Any?> {

    private val engine by lazy {
        with(ScriptEngineManager()) {
            getEngineByName(language)
                ?: getEngineByExtension(language)
                ?: getEngineByMimeType(language)
                ?: throw IllegalArgumentException(
                    "$language is not an available language. Your environment supports the following languages: ${
                        engineFactories.joinToString(
                            separator = System.lineSeparator(),
                            prefix = System.lineSeparator(),
                        ) {
                            " - ${it.languageName}, " +
                                "aka ${it.extensions + it.mimeTypes} " +
                                "(${it.languageVersion} on ${it.engineName} ${it.engineVersion})"
                        }
                    }",
                )
        }
    }

    /**
     * Given the current controlled variables, computes the current values for
     * this variable.
     *
     * @param variables
     * a mapping between variable names and values
     * @return the value for this value
     * @throws IllegalStateException
     * if the value can not be computed, e.g. because there are
     * unassigned required variables
     */
    override fun getWith(variables: Map<String, Any?>): Any? = synchronized(engine) {
        runCatching {
            runBlocking {
                withTimeout(timeout) {
                    engine.eval(formula, variables.asBindings())
                }
            }
        }.getOrElse { cause ->
            val whatHappened = "A $language script evaluation failed"
            val whyHappened = when (cause) {
                is ScriptException -> "due to an error in the script: ${cause.message}"
                is TimeoutCancellationException -> """
                    because it reached its ${timeout}ms timeout.
                    This is usually a sign that something is looping.
                    Either make the script run faster, or allow for a longer time by specifiying a different
                    `${DocumentRoot.DependentVariable.timeout}`.
                """.trimIndent().replace(Regex("\\R"), "")
                else -> """
                    |for a reason unknown to Alchemist. 
                    |${cause::class.jvmName}: ${cause.message}"
                """.trimMargin()
            }
            val inspection = "context: $variables\nscript:\n$formula"
            throw IllegalArgumentException("$whatHappened $whyHappened\n$inspection", cause)
        }
    }

    private fun Map<String, Any?>.asBindings(): Bindings = SimpleBindings(toMutableMap())
}
