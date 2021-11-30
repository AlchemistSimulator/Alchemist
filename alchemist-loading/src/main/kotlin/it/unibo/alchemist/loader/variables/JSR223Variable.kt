package it.unibo.alchemist.loader.variables

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * This variable loads any [JSR-233](http://archive.fo/PGdk8) language available in the classpath.
 *
 * @constructor builds a new JSR223Variable given a language name and a script.
 *
 * @param language can be the name of the language, the file extension, or its mime type
 * @param formula the script that will get interpreted
 */
data class JSR223Variable(
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
                    engineFactories
                        .map {
                            " - ${it.languageName}, " +
                                "aka ${it.extensions + it.mimeTypes} " +
                                "(${it.languageVersion} on ${it.engineName} ${it.engineVersion})"
                        }
                        .joinToString(separator = System.lineSeparator(), prefix = System.lineSeparator())
                    }"
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
    @ExperimentalTime
    override fun getWith(variables: Map<String, Any?>): Any? = try {
        synchronized(engine) {
            runCatching {
                runBlocking {
                    withTimeout(timeout) {
                        engine.eval(formula, variables.asBindings())
                    }
                }
            }.getOrElse {
                throw java.lang.IllegalStateException(
                    "The evaluation of the $language script took more than ${timeout}ms, this is usually a sign that " +
                        "something is looping. Either fix the script, " +
                        "or allow for a longer time with the 'timeout:' key\n" +
                        """
                        |script:
                        |
                        |${formula.lines().joinToString("\n|")}
                        |context: $variables
                        """.trimMargin()
                )
            }
        }
    } catch (e: ScriptException) {
        throw IllegalStateException("Unable to evaluate $formula with bindings: $variables", e)
    }

    private fun Map<String, Any?>.asBindings(): Bindings =
        object : Bindings, MutableMap<String, Any?> by this.toMutableMap() { }
}
