/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.launchers

import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.stop
import io.ktor.server.netty.Netty
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.graphql.monitor.EnvironmentSubscriptionMonitor
import it.unibo.alchemist.boundary.graphql.server.modules.graphQLModule
import it.unibo.alchemist.boundary.graphql.server.modules.graphQLRoutingModule
import it.unibo.alchemist.boundary.graphql.utils.DefaultGraphQLSettings
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore

/**
 * An [OutputMonitor] observing the [environment] through a GraphQL server listening on [host]:[port].
 * The server is started in a new coroutine on the [serverDispatcher] dispatcher.
 * By default, the server is stopped after the simulation terminates.
 * This behavior can be changed by setting [teardownOnSimulationTermination] to false.
 */
open class GraphQLServer<T, P : Position<out P>> @JvmOverloads constructor(
    val environment: Environment<T, P>,
    val host: String = DefaultGraphQLSettings.DEFAULT_HOST,
    val port: Int = DefaultGraphQLSettings.DEFAULT_PORT,
    val teardownOnSimulationTermination: Boolean = true,
    private val serverDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : OutputMonitor<Any, Nothing> {

    private val subscriptionMonitor = EnvironmentSubscriptionMonitor<Any, Nothing>()
    private lateinit var server: ApplicationEngine

    override fun initialized(environment: Environment<Any, Nothing>) {
        environment.simulation.addOutputMonitor(subscriptionMonitor)
        server = makeServer()
        val mutex = java.util.concurrent.Semaphore(0)
        Thread(
            {
                runBlocking {
                    launch(serverDispatcher) {
                        mutex.release()
                        server.start(wait = true)
                    }
                }
            },
            "alchemist-graphql-server@$host:$port",
        ).start()
        mutex.acquireUninterruptibly()
    }

    override fun finished(environment: Environment<Any, Nothing>, time: Time, step: Long) {
        if (teardownOnSimulationTermination) {
            server.stop()
        }
    }

    private fun makeServer(): ApplicationEngine =
        embeddedServer(
            Netty,
            port = port,
            host = host,
            module = {
                graphQLModule(this@GraphQLServer.environment)
                graphQLRoutingModule()
            },
        )
}
