/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.modules

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import it.unibo.alchemist.server.routes.EnvironmentRoute.environmentClientMode
import it.unibo.alchemist.server.routes.EnvironmentRoute.environmentServerMode
import it.unibo.alchemist.server.routes.SimulationRoute.simulationActionPause
import it.unibo.alchemist.server.routes.SimulationRoute.simulationActionPlay
import it.unibo.alchemist.server.routes.SimulationRoute.simulationStatus
import it.unibo.alchemist.server.routes.mainRoute

/**
 * Ktor module that adds all the routing configuration to an application.
 * @see <a href="https://ktor.io/docs/modules.html">Ktor Modules</a>
 */
fun Application.routingModule() {
    routing {
        mainRoute()
        simulationStatus()
        simulationActionPlay()
        simulationActionPause()
        environmentServerMode()
        environmentClientMode()
    }
}
