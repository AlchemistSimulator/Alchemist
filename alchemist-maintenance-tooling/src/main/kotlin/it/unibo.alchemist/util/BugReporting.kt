/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

/**
 * Entrypoint for printing meaningful debug information in case of failed internal consistency checks.
 */
object BugReporting {

    /**
     * This function throws exception and generates useful debug information,
     * as well as instructions on how to report the bug.
     */
    @JvmStatic
    @JvmOverloads
    fun reportBug(
        message: String,
        debugInformation: Map<String, Any?> = mapOf()
    ): Nothing {
        error(
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
