/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.serialization

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate
import kotlinx.serialization.PolymorphicSerializer

class JsonFormatTest : StringSpec({

    "Polymorphic serialization and deserialization should work for EmptyConcentration" {
        val concentration: Any = EmptyConcentrationSurrogate
        val serialized = jsonFormat.encodeToString(PolymorphicSerializer(Any::class), concentration)
        serialized.contains("type") shouldBe true
        val deserialized = jsonFormat.decodeFromString(PolymorphicSerializer(Any::class), serialized)
        deserialized shouldBe EmptyConcentrationSurrogate
    }
})
