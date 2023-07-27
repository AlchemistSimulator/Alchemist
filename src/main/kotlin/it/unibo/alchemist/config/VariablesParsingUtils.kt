/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.config

/**
 * Utility object with method extensions that help with parsing nullable [Any] to a type.
 */
object VariablesParsingUtils {

    /**
     * Parse String.
     */
    fun Any?.parseString(): String? {
        return this?.toString()
    }

    /**
     * Parse Int.
     */
    fun Any?.parseInt(): Int? {
        return this.parseString()?.toInt()
    }

    /**
     * Parse Double.
     */
    fun Any?.parseDouble(): Double? {
        return this.parseString()?.toDouble()
    }

    /**
     * Parse Boolean.
     */
    fun Any?.parseBoolean(): Boolean? {
        return this.parseString()?.toBoolean()
    }

    /**
     * Parse type given a mapping function.
     *
     * @property parser mapping of String -> T
     */
    fun <T> Any?.parse(parser: (String) -> T): T? {
        val res = this.parseString()
        return if (res != null) {
            parser(res)
        } else {
            null
        }
    }

    /**
     * Parse Map of String to Any.
     */
    @Suppress("UNCHECKED_CAST")
    fun Any?.parseMap(): Map<String, Any?>? {
        return if (this is Map<*, *>) {
            this as Map<String, Any>
        } else {
            null
        }
    }

    /**
     * Parse List of Strings.
     */
    fun Any?.parseStringList(): List<String>? {
        return if (this is List<*>) {
            this.map { it.toString() }
        } else {
            null
        }
    }
}
