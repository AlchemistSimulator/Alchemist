/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RenderModeTest : StringSpec({
    "RenderMode are just CLIENT, SERVER and AUTO" {
        val renderModes = RenderMode.values()
        renderModes.size shouldBe 3
        renderModes.map { it }.containsAll(listOf(RenderMode.CLIENT, RenderMode.SERVER, RenderMode.AUTO)) shouldBe true
    }
})
