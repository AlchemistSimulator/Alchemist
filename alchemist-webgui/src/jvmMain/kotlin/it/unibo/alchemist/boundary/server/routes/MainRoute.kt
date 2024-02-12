/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.server.routes

import com.google.common.io.Resources
import com.google.common.io.Resources.getResource
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.mainRoute() {
    get("/") {
        val resource = Resources.getResource("index.html")
        call.respondText(resource.readText(), ContentType.Text.Html)
    }

    staticResources("/", "")
}
