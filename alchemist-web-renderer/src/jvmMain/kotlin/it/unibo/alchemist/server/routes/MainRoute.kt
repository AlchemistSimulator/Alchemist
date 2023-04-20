/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.server.application.call
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import it.unibo.alchemist.server.utility.Response
import it.unibo.alchemist.server.utility.Response.Companion.respond
import org.kaikikm.threadresloader.ResourceLoader.getResource

/**
 * Route of type GET that sends the index page to the client.
 * The HTTP [Response] sent to the client can be of type:
 * - 200 (OK) the index.html resource is correctly sent to the client;
 * - 404 (Not Found) the index.html resource was not found.
 */
fun Route.mainRoute() {
    get("/") {
        getResource("index.html")
            ?.let { resource -> call.respondText(resource.readText(), ContentType.Text.Html) }
            ?: respond(Response(NotFound, "Main index.html is missing."))
    }

    staticResources("/", "")
}
