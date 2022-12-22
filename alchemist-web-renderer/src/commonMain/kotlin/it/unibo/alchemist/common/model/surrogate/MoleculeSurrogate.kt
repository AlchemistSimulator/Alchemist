/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Surrogate class for the [it.unibo.alchemist.model.interfaces.Molecule] interface.
 * @param name the name of the molecule.
 */
@Serializable(with = MoleculeSurrogateSerializer::class)
data class MoleculeSurrogate(val name: String)

/**
 * Custom serializer to map a molecule to a string, as in JSON non-primitive types aren't allowed in [Map] key position.
 */
object MoleculeSurrogateSerializer : KSerializer<MoleculeSurrogate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Molecule", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MoleculeSurrogate) {
        encoder.encodeSerializableValue(String.serializer(), value.name)
    }

    override fun deserialize(decoder: Decoder): MoleculeSurrogate {
        val string = decoder.decodeSerializableValue(String.serializer())
        return MoleculeSurrogate(string)
    }
}
