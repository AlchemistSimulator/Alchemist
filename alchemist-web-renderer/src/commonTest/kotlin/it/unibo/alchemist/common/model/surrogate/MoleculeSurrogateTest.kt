/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.model.serialization.jsonFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@OptIn(ExperimentalSerializationApi::class)
class MoleculeSurrogateTest : StringSpec({

    val moleculeSurrogate = MoleculeSurrogate("molecule")

    "MoleculeSurrogate should have the correct name" {
        moleculeSurrogate.name shouldBe "molecule"
    }

    "MoleculeSurrogate should be serialized and deserialized correctly" {
        MoleculeSurrogate.serializer().descriptor.serialName shouldBe "Molecule"
        val serialized = jsonFormat.encodeToString(moleculeSurrogate)
        serialized shouldBe "\"molecule\""
        val deserialized: MoleculeSurrogate = jsonFormat.decodeFromString(serialized)
        deserialized shouldBe moleculeSurrogate
    }
})
