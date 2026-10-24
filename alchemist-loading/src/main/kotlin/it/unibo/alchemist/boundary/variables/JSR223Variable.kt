/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.loader.syntax.AlchemistYamlSyntax
import java.lang.reflect.Modifier
import java.util.AbstractMap.SimpleImmutableEntry
import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import javax.script.SimpleBindings
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.jvmName
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * This variable loads any [JSR-233](https://archive.is/PGdk8) language available in the classpath.
 *
 * @constructor builds a new JSR223Variable given a language name and a script.
 * @property language the language name, file extension, or mime type
 * @property formula the script that will get interpreted
 * @property timeout how long should the interpreter be allowed to compute before giving up, in ms. Defaults to 1000ms
 */
data class JSR223Variable
@JvmOverloads
constructor(
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

    private val isKotlin by lazy { engine.factory.languageName.equals("kotlin", ignoreCase = true) }

    private val isScala by lazy { engine.factory.languageName == "Scala" }

    /*
     * The Scala 3 engine ignores JSR-223 bindings: they are handed over through a Bindings object living in the REPL,
     * and exposed to the script as AnyRef values, as the Scala 2 engine did.
     */
    private val scalaBindings by lazy {
        engine.eval("val alchemistBindings = new javax.script.SimpleBindings(); alchemistBindings") as Bindings
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
                withTimeout(timeout.milliseconds) {
                    if (isScala) evalScala(variables) else engine.eval(formula, variables.asBindings())
                }
            }
        }.getOrElse { cause ->
            val whatHappened = "A $language script evaluation failed"
            val whyHappened = when (cause) {
                is ScriptException -> "due to an error in the script: ${cause.message}"
                is TimeoutCancellationException ->
                    """
                        because it reached its ${timeout}ms timeout.
                        This is usually a sign that something is looping.
                        Either make the script run faster, or allow for a longer time by specifiying a different
                        `${AlchemistYamlSyntax.DependentVariable.timeout}`.
                    """.trimIndent().replace(Regex("\\R"), "")
                else ->
                    """
                |for a reason unknown to Alchemist. 
                |${cause::class.jvmName}: ${cause.message}"
                    """.trimMargin()
            }
            val inspection = "context: $variables\nscript:\n$formula"
            throw IllegalArgumentException("$whatHappened $whyHappened\n$inspection", cause)
        }
    }

    private fun evalScala(variables: Map<String, Any?>): Any? {
        scalaBindings.clear()
        scalaBindings.putAll(variables)
        val definitions = variables.keys.joinToString("") { "val `$it`: AnyRef = alchemistBindings.get(\"$it\")\n" }
        /*
         * The Scala 3 engine does not throw ScriptExceptions: a script that does not compile surfaces as a missing
         * wrapper class (or a null result), and a script failing at runtime as an InvocationTargetException.
         * The result is boxed to tell failures apart from null values.
         */
        val result =
            try {
                engine.eval("Array[Any]({\n$definitions$formula\n})")
            } catch (failure: ReflectiveOperationException) {
                throw ScriptException(failure)
            }
        val boxed = result as? Array<*> ?: throw ScriptException("The Scala script does not compile")
        return boxed.single()
    }

    /*
     * Java modifiers are checked first, so that Kotlin reflection never inspects synthetic classes (e.g., lambdas).
     * Kotlin internal classes, such as EmptyList, are public in bytecode, and are spotted through their metadata.
     */
    private val Class<*>.isAccessibleFromScripts: Boolean get() =
        Modifier.isPublic(modifiers) &&
            !isSynthetic &&
            !isAnonymousClass &&
            !isLocalClass &&
            kotlin.visibility == KVisibility.PUBLIC &&
            enclosingClass?.isAccessibleFromScripts != false

    private fun Map<String, Any?>.asBindings(): Bindings = SimpleBindings(
        /*
         * Remove this workaround as soon as https://youtrack.jetbrains.com/issue/KT-90641 is resolved upstream
         * and the patched version of Kotlin is integrated in Alchemist.
         */
        when {
            isKotlin -> mapValuesTo(mutableMapOf()) { (_, value) -> value.withAccessibleClass() }
            else -> toMutableMap()
        },
    )

    /*
     * Since 2.4.21, the Kotlin engine declares every binding with its runtime class as type, and fails to compile any
     * script if that class is not accessible, as is the case for the results of listOf, emptyList, or List.of.
     * Collections and map entries with inaccessible classes are thus copied into public classes, see:
     * https://youtrack.jetbrains.com/issue/KT-90641
     *
     * This workaround MUST be removed as soon as the issue is resolved upstream.
     */
    private fun Any?.withAccessibleClass(): Any? = when {
        this == null || this::class.java.isAccessibleFromScripts -> this
        else -> copiedIntoAccessibleClass()
    }

    private fun Any.copiedIntoAccessibleClass(): Any = when (this) {
        is Map<*, *> -> LinkedHashMap(this)
        is List<*> -> ArrayList(this)
        is Set<*> -> LinkedHashSet(this)
        is Collection<*> -> ArrayList(this)
        is Map.Entry<*, *> -> SimpleImmutableEntry(this)
        else -> this
    }
}
