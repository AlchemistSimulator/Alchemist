/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.utility.Routes.environmentClientPath
import it.unibo.alchemist.common.utility.Routes.environmentServerPath
import it.unibo.alchemist.common.utility.Routes.simulationPausePath
import it.unibo.alchemist.common.utility.Routes.simulationPlayPath
import it.unibo.alchemist.common.utility.Routes.simulationStatusPath

class RoutesTest : StringSpec({
    "All the routes strings should be correct" {
        environmentClientPath shouldBe "/environment/client"
        environmentServerPath shouldBe "/environment/server"
        simulationStatusPath shouldBe "/simulation/status"
        simulationPlayPath shouldBe "/simulation/play"
        simulationPausePath shouldBe "/simulation/pause"
    }
})
