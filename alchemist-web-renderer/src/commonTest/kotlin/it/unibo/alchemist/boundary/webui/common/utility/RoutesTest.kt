/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.webui.common.utility.Routes.ENVIRONMENT_CLIENT_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.ENVIRONMENT_SERVER_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_PAUSE_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_PLAY_PATH
import it.unibo.alchemist.boundary.webui.common.utility.Routes.SIMULATION_STATUS_PATH

class RoutesTest : StringSpec({
    "All the routes strings should be correct" {
        ENVIRONMENT_CLIENT_PATH shouldBe "/environment/client"
        ENVIRONMENT_SERVER_PATH shouldBe "/environment/server"
        SIMULATION_STATUS_PATH shouldBe "/simulation/status"
        SIMULATION_PLAY_PATH shouldBe "/simulation/play"
        SIMULATION_PAUSE_PATH shouldBe "/simulation/pause"
    }
})
