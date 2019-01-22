package it.unibo.alchemist.loader.variables

import groovy.util.Eval
import java.lang.IllegalArgumentException

/*
 * A variable written as Groovy script
 */
class GroovyVariable<R>(formula: String): ScriptVariable<R>(formula) {
    override fun interpret(s: String?): R {
        try {
            return Eval.me(s) as R
        } catch (e: Exception) {
            throw IllegalStateException("«$s» is not a valid Groovy script", e)
        }
    }
}