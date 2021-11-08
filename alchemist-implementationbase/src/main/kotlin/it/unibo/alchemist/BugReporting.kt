/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist

object BugReporting {

    /**
     * This function throws exception and generates useful debug information,
     * as well as instructions on how to report the bug.
     */
    @JvmStatic
    fun reportBug(
        message: String,
        debugInformation: Map<String, Any?>
    ): Nothing {
        throw IllegalStateException(
            """
                $message
                
                This is most likely a bug in in Alchemist. Please, open a report at:
                    --> https://github.com/AlchemistSimulator/Alchemist/issues/new/choose
                attaching the following information and the full stacktrace:
                
                ${debugInformation.debugReport()}
            """.trimIndent()
        )
    }

    private fun Map<String, Any?>.debugReport(): String = asIterable()
        .joinToString(System.lineSeparator()) { (name, value) ->
            "$name => ${value?.let { "$it --- type: ${it::class.simpleName}" } ?: "null" }"
        }
}
