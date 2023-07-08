/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Log verbosity configuration.
 *
 * @property code verbosity code
 * @property logLevel logging level mapping
 */
enum class Verbosity(val code: String, val logLevel: Level) {

    /**
     * Debug.
     */
    DEBUG("debug", Level.DEBUG),

    /**
     * INFO.
     */
    INFO("info", Level.INFO),

    /**
     * WARN.
     */
    WARN("warn", Level.WARN),

    /**
     * ERROR.
     */
    ERROR("error", Level.ERROR),

    /**
     * ALL.
     */
    ALL("all", Level.ALL),

    /**
     * OFF.
     */
    OFF("off", Level.OFF),
}

/**
 * Verbosity deserializer using code.
 */
class VerbosityDeserializer : StdDeserializer<Verbosity>(Verbosity::class.java) {
    /**
     * deserialize.
     */
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext?): Verbosity {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val value = node.textValue()
        val match = Verbosity.values().find { it.code == value }
        if (match != null) {
            return match
        } else {
            throw IllegalArgumentException("Unknown Verbosity value $value, allowed: [${Verbosity.values()}]")
        }
    }
}
