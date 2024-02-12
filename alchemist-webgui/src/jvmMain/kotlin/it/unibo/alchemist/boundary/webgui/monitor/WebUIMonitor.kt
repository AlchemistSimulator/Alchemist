/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webgui.monitor

import com.google.common.io.Resources
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import korlibs.io.async.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import it.unibo.alchemist.boundary.launchers.GraphQLServer as GraphQLServer

/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class WebUIMonitor<T, P : Position<out P>> (
    environment: Environment<T, P>,
) : GraphQLServer<T, P>(environment) {
    private val serverDispatcher: CoroutineDispatcher = Dispatchers.Default

    override fun initialized(environment: Environment<Any, Nothing>) {
        super.initialized(environment)
        startWebServer()
    }

    private fun startWebServer() {
        launch(serverDispatcher) {
            embeddedServer(Netty, port = 9090) {
                install(CORS) {
                    allowSameOrigin
                    allowCredentials = true
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    anyHost()
                }

                routing {
                    get("/") {
                        val resource = Resources.getResource("index.html")
                        call.respondText(resource.readText(), ContentType.Text.Html)
                    }

                    staticResources("/", "")
                }
            }.start(wait = false)
        }
    }
}
