/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model.surrogate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.serialization.jsonFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class EmptyConcentrationTest : StringSpec({

    val emptyConcentration = EmptyConcentrationSurrogate

    "EmptyConcentration should be serialized correctly" {
        EmptyConcentrationSurrogate.serializer().descriptor.serialName shouldBe "EmptyConcentration"
        val ser = jsonFormat.encodeToString(emptyConcentration)
        val des: EmptyConcentrationSurrogate = jsonFormat.decodeFromString(ser)
        des shouldBe emptyConcentration
    }
})
