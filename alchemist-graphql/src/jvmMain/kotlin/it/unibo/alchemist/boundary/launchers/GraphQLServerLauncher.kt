/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launchers

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.graphql.monitor.EnvironmentSubscriptionMonitor
import it.unibo.alchemist.boundary.graphql.server.attributes.SimulationAttributeKey
import it.unibo.alchemist.boundary.graphql.server.modules.graphQLModule
import it.unibo.alchemist.boundary.graphql.server.modules.graphQLRoutingModule
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A launcher that starts a GraphQL server for exposing the simulation.
 */
class GraphQLServerLauncher @JvmOverloads constructor(
    private val port: Int = 8081,
    private val host: String = "127.0.0.1",
) : SimulationLauncher() {
    override fun launch(loader: Loader) {
        val simulation: Simulation<Any, Nothing> = prepareSimulation(loader, emptyMap<String, Any>())
        simulation.addOutputMonitor(EnvironmentSubscriptionMonitor())
        startServer(simulation)
    }

    private fun <T, P : Position<out P>> startServer(
        simulation: Simulation<T, P>,
        serverDispatcher: CoroutineDispatcher = Dispatchers.Default,
    ) {
        return runBlocking {
            val server = makeServer(simulation)
            launch(serverDispatcher) {
                server.start(wait = true)
            }
            simulation.run()
        }
    }

    private fun<T, P : Position<out P>> makeServer(simulation: Simulation<T, P>) =
        embeddedServer(
            Netty,
            port = port,
            host = host,
            module = {
                attributes.put(SimulationAttributeKey, simulation)
                graphQLModule()
                graphQLRoutingModule()
            },
        )
}
