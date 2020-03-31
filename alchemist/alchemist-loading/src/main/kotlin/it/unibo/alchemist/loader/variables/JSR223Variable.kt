package it.unibo.alchemist.loader.variables

import javax.script.Bindings
import javax.script.ScriptEngineManager
import javax.script.ScriptException

/**
 * This variable loads any [JSR-233](http://archive.fo/PGdk8) language available in the classpath.
 *
 * @param R return type of the variable
 * @constructor builds a new JSR223Variable given a language name and a script.
 *
 * @param language can be the name of the language, the file extension, or its mime type
 * @param formula the script that will get interpreted
 */
data class JSR223Variable<R>(val language: String, val formula: String) : DependentVariable<R> {

    private val engine by lazy {
        with(ScriptEngineManager()) {
            getEngineByName(language)
            ?: getEngineByExtension(language)
            ?: getEngineByMimeType(language)
            ?: throw IllegalArgumentException(
                "$language is not an available language. Your environment supports the following languages: ${
                    engineFactories.map {
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
    @Suppress("UNCHECKED_CAST")
    override fun getWith(variables: Map<String, Any>): R = try {
        engine.eval(formula, variables.asBindings()) as R
    } catch (e: ScriptException) {
        throw IllegalStateException(e)
    }

    private fun Map<String, Any>.asBindings(): Bindings =
        object : Bindings, MutableMap<String, Any> by this.toMutableMap() { }
}
