/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webgui.monitor

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import it.unibo.alchemist.boundary.server.modules.installModule
import it.unibo.alchemist.boundary.server.modules.routingModule
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import korlibs.io.async.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import it.unibo.alchemist.boundary.launchers.GraphQLServer as GraphQLServer

/**
 * Represents a GraphQL server with a web-based user interface monitor.
 * This class extends the GraphQLServer class and provides functionality to start and stop a web server
 * client-side alongside the GraphQL server.
 *
 * @param T The type of elements in the environment.
 * @param P The type of positions associated with elements in the environment.
 * @param environment The environment to monitor using the web UI.
 */
class WebUIMonitor<T, P : Position<out P>> (
    environment: Environment<T, P>,
) : GraphQLServer<T, P>(environment) {
    private val serverDispatcher: CoroutineDispatcher = Dispatchers.Default
    private lateinit var webServer: ApplicationEngine

    override fun initialized(environment: Environment<Any, Nothing>) {
        super.initialized(environment)
        webServer = startWebServer()
        launch(serverDispatcher) {
            webServer.start(wait = false)
        }
    }

    override fun finished(environment: Environment<Any, Nothing>, time: Time, step: Long) {
        this.finished(environment, time, step)
        if (teardownOnSimulationTermination) {
            webServer.stop()
        }
    }

    private fun startWebServer(): ApplicationEngine =
        embeddedServer(
            Netty,
            port = 9090,
            module = {
                installModule()
                routingModule()
            },
        )
}
