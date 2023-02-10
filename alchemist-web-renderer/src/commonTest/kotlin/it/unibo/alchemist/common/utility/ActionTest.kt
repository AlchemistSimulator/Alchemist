/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ActionTest : StringSpec({
    "SimulationAction are PLAY and PAUSE" {
        val actions = Action.values()
        actions.size shouldBe 2
        actions.map { it }.containsAll(listOf(Action.PLAY, Action.PAUSE)) shouldBe true
    }
})
