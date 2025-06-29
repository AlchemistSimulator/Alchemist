/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class MoleculeSurrogateTest {

    private val moleculeSurrogate = MoleculeSurrogate("molecule")

    @Test
    fun `molecule surrogate should have the correct name`() {
        assertEquals("molecule", moleculeSurrogate.name, "MoleculeSurrogate.name should match constructor argument")
    }

    @Test
    fun `molecule surrogate should serialize and deserialize correctly`() {
        // Verify serialName
        val serialName = MoleculeSurrogate.serializer().descriptor.serialName
        assertEquals("Molecule", serialName, "Serializer serialName should be 'Molecule'")
        // Round-trip serialization
        val serialized = jsonFormat.encodeToString(moleculeSurrogate)
        assertEquals("\"molecule\"", serialized, "Serialized JSON should be the raw string '\"molecule\"'")
        val deserialized: MoleculeSurrogate = jsonFormat.decodeFromString(serialized)
        assertEquals(moleculeSurrogate, deserialized, "Deserialized surrogate should equal the original")
    }
}
