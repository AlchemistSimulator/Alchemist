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
 * Engine output replay strategy modes.
 * @property code mode code
 */
enum class OutputReplayStrategy(val code: String) {

    /**
     *  Launch simulation in aggregate output mode.
     */
    AGGREGATE("aggregate"),

    /**
     *  Launch simulation in replay output mode.
     */
    REPLAY("replay"),
}

/**
 * EngineMode deserializer using code.
 */
class OutputReplayStrategyDeserializer : StdDeserializer<OutputReplayStrategy>(OutputReplayStrategy::class.java) {
    /**
     * deserialize.
     */
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext?): OutputReplayStrategy {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val value = node.textValue()
        val match = OutputReplayStrategy.values().find { it.code == value }
        if (match != null) {
            return match
        } else {
            throw IllegalArgumentException("Unknown OutputReplayStrategy value $value, allowed: [${OutputReplayStrategy.values()}]")
        }
    }
}
