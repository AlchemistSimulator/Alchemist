/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.variables

import groovy.util.Eval

/*
 * A variable written as Groovy script
 */
class GroovyVariable<R>(formula: String) : ScriptVariable<R>(formula) {
    override fun interpret(s: String): R {
        try {
            @Suppress("UNCHECKED_CAST")
            return Eval.me(s) as R
        } catch (e: Exception) {
            throw IllegalStateException("«$s» is not a valid Groovy script", e)
        }
    }
}