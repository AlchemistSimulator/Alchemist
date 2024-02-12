/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.server.modules

import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import it.unibo.alchemist.boundary.server.routes.mainRoute

fun Application.routingModule() {
    routing {
        mainRoute()
    }
}
