/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.server.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.model.surrogate.EmptyConcentrationSurrogate
import it.unibo.alchemist.boundary.webui.server.surrogates.utility.ToConcentrationSurrogate.toEmptyConcentration

class ToConcentrationSurrogateTest : StringSpec({

    "toEmptyConcentration should always return an EmptyConcentrationSurrogate" {
        toEmptyConcentration("any") shouldBe EmptyConcentrationSurrogate
        toEmptyConcentration(2.0) shouldBe EmptyConcentrationSurrogate
        toEmptyConcentration(listOf("1", 2, 3.0)) shouldBe EmptyConcentrationSurrogate
        toEmptyConcentration(mapOf(1 to "2")) shouldBe EmptyConcentrationSurrogate
    }
})
