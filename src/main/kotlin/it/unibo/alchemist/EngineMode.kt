/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Engine modes.
 * @property code mode code
 */
enum class EngineMode(val code: String) {

    /**
     * Launch simulation is single-threaded deterministic mode
     */
    DETERMINISTIC("deterministic"),

    /**
     *  Launch simulation in fixed batch size mode.
     */
    BATCH_FIXED("batchFixed"),

    /**
     *  Launch simulation in epsilon batch mode.
     */
    EPSILON("batchEpsilon"),
}

/**
 * EngineMode deserializer using code.
 */
class EngineModeDeserializer : StdDeserializer<EngineMode>(EngineMode::class.java) {
    /**
     * deserialize.
     */
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext?): EngineMode {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val value = node.textValue()
        val match = EngineMode.values().find { it.code == value }
        if (match != null) {
            return match
        } else {
            throw IllegalArgumentException("Unknown EngineMode value $value, allowed: [${EngineMode.values()}]")
        }
    }
}
