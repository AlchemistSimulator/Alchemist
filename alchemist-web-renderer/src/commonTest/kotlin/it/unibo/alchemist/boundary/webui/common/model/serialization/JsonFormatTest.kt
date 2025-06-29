/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.serialization

import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.PolymorphicSerializer

class JsonFormatTest {

    @Test
    fun `polymorphic serialization and deserialization should work for EmptyConcentrationSurrogate`() {
        val concentration: Any = EmptyConcentrationSurrogate
        val serializer = PolymorphicSerializer(Any::class)
        val serialized = jsonFormat.encodeToString(serializer, concentration)
        assertTrue(
            serialized.contains("type"),
            "Expected serialized JSON to include a \"type\" discriminator, but was: $serialized",
        )
        val deserialized = jsonFormat.decodeFromString(serializer, serialized)
        assertEquals(
            EmptyConcentrationSurrogate,
            deserialized,
            "Expected deserialized object to equal EmptyConcentrationSurrogate",
        )
    }
}
