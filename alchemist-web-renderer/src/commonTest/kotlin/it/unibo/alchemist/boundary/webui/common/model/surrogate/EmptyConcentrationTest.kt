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

@OptIn(ExperimentalSerializationApi::class)
class EmptyConcentrationTest {

    @Test
    fun `EmptyConcentration should be serialized correctly`() {
        val emptyConcentration = EmptyConcentrationSurrogate
        // Check serializer name
        assertEquals(
            "EmptyConcentration",
            EmptyConcentrationSurrogate.serializer().descriptor.serialName,
            "Serializer serialName should be 'EmptyConcentration'",
        )
        // Perform round-trip serialization
        val ser = jsonFormat.encodeToString(emptyConcentration)
        val des: EmptyConcentrationSurrogate = jsonFormat.decodeFromString(ser)
        assertEquals(
            emptyConcentration,
            des,
            "Deserialized object should equal the original EmptyConcentrationSurrogate",
        )
    }
}
