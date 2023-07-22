/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.config

object VariablesParsingUtils {

    fun Any?.parseString(): String? {
        return this?.toString()
    }

    fun Any?.parseInt(): Int? {
        return this.parseString()?.toInt()
    }

    fun Any?.parseDouble(): Double? {
        return this.parseString()?.toDouble()
    }

    fun Any?.parseBoolean(): Boolean? {
        return this.parseString()?.toBoolean()
    }

    fun <T> Any?.parse(parser: (String) -> T): T? {
        val res = this.parseString()
        return if (res != null) {
            parser(res)
        } else {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun Any?.parseMap(): Map<String, Any?>? {
        return if (this is Map<*, *>) {
            this as Map<String, Any>
        } else {
            null
        }
    }

    fun Any?.parseStringList(): List<String>? {
        return if (this is List<*>) {
            this.map { it.toString() }
        } else {
            null
        }
    }
}
