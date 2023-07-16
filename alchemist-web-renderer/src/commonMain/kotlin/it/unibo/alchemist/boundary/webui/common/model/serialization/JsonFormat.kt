/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.serialization

import it.unibo.alchemist.boundary.webui.common.model.serialization.SerializationModules.concentrationModule
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import kotlinx.serialization.json.Json

/**
 * An instance of [Json] that can be used to serialize and deserialize some concentration classes using open
 * polymorphism.
 */
val jsonFormat = Json {
    serializersModule = concentrationModule
}

/**
 * Encode the EnvironmentSurrogate to a JSON string using the [EnvironmentSurrogate.polymorphicSerializer] to serialize
 * the concentrations.
 * @param env the [EnvironmentSurrogate] to encode.
 * @param <TS> the type of concentration surrogate.
 * @param <PS> the type of [PositionSurrogate].
 * @return the JSON string.
 */
inline fun <reified TS, reified PS> Json.encodeEnvironmentSurrogate(env: EnvironmentSurrogate<TS, PS>): String
    where TS : Any, PS : PositionSurrogate = this.encodeToString(EnvironmentSurrogate.polymorphicSerializer(), env)

/**
 * Decode the JSON string to an [EnvironmentSurrogate] using the [EnvironmentSurrogate.polymorphicSerializer] to
 * deserialize the concentrations.
 * @param env the [String] to decode.
 * @return the corresponding [EnvironmentSurrogate].
 */
fun Json.decodeEnvironmentSurrogate(env: String): EnvironmentSurrogate<Any, PositionSurrogate> =
    this.decodeFromString(EnvironmentSurrogate.polymorphicSerializer(), env)
